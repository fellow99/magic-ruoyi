# 认证模块规格文档（001-auth/spec.md）

> magic-ruoyi 认证模块。定义系统全部认证策略、登录流程、验证码机制与注册流程。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 模块概述

认证模块负责处理用户身份验证的全部场景，包括登录、注册、退出、验证码生成与校验。模块采用**策略模式**实现 5 种认证方式，通过 `grantType` 参数动态路由到对应策略实现。

### 1.1 技术栈

| 组件 | 技术选型 |
|------|----------|
| 认证框架 | Sa-Token + JWT |
| 会话存储 | Redis（分布式会话） |
| 密码加密 | BCrypt |
| 验证码 | Hutool Captcha（图形验证码） |
| 短信服务 | SMS4J |
| 邮件服务 | Spring Mail |
| 社交登录 | JustAuth（me.zhyd.oauth） |
| 小程序登录 | JustAuth（AuthWechatMiniProgramRequest） |

### 1.2 核心设计模式

**策略模式（Strategy Pattern）**：

```
IAuthStrategy (接口)
├── passwordAuthStrategy  → 密码认证
├── smsAuthStrategy       → 短信验证码认证
├── emailAuthStrategy     → 邮箱验证码认证
├── socialAuthStrategy    → 第三方社交登录
└── xcxAuthStrategy       → 微信小程序登录
```

策略路由逻辑：`AuthController.login()` 接收请求后，解析 `grantType` 参数，拼接 `BASE_NAME` 得到 Bean 名称（如 `passwordAuthStrategy`），通过 Spring 容器获取对应策略实例并执行登录。

---

## 2. 认证策略

### 2.1 密码认证（password）

**Bean 名称**: `passwordAuthStrategy`

**流程**:
1. 解析请求体为 `PasswordLoginBody`
2. 校验图形验证码（若启用）
3. 在租户上下文中按用户名查询用户
4. 校验用户状态（是否停用）
5. BCrypt 校验密码
6. 构建 `LoginUser` 对象
7. 调用 `LoginHelper.login()` 生成 JWT Token
8. 返回 `LoginVo`（含 access_token、过期时间、clientId）

**验证码校验**:
- Redis Key: `captcha_code_key:{uuid}`
- 校验成功后立即删除 Key（一次性使用）
- 验证码过期抛出 `CaptchaExpireException`
- 验证码错误抛出 `CaptchaException`
- 失败时记录登录日志

### 2.2 短信验证码认证（sms）

**Bean 名称**: `smsAuthStrategy`

**流程**:
1. 解析请求体为 `SmsLoginBody`
2. 在租户上下文中按手机号查询用户
3. 校验用户状态
4. 校验短信验证码（Redis 中比对）
5. 构建 `LoginUser` 并生成 Token

**验证码校验**:
- Redis Key: `captcha_code_key:{phonenumber}`
- 验证码为空时抛出 `CaptchaExpireException`
- 不匹配时返回 false，触发登录失败记录

### 2.3 邮箱验证码认证（email）

**Bean 名称**: `emailAuthStrategy`

**流程**:
1. 解析请求体为 `EmailLoginBody`
2. 在租户上下文中按邮箱查询用户
3. 校验用户状态
4. 校验邮箱验证码（Redis 中比对）
5. 构建 `LoginUser` 并生成 Token

**验证码校验**:
- Redis Key: `captcha_code_key:{email}`
- 逻辑与短信验证码相同

### 2.4 第三方社交登录（social）

**Bean 名称**: `socialAuthStrategy`

**流程**:
1. 解析请求体为 `SocialLoginBody`
2. 调用 JustAuth 完成第三方授权验证
3. 按 `source + uuid` 查询已绑定的社交账号
4. 校验用户是否已绑定第三方账号
5. 若启用多租户，校验该租户是否有对应社交绑定
6. 按 userId 查询用户信息
7. 构建 `LoginUser` 并生成 Token

**前置条件**: 用户必须先在已登录状态下完成第三方账号绑定（`POST /auth/social/callback`），否则无法通过社交登录。

**支持的社交平台**: WeChat、MaxKey、TopIAM、Gitee、GitHub（由前端登录页面按钮决定）

### 2.5 微信小程序登录（xcx）

**Bean 名称**: `xcxAuthStrategy`

**流程**:
1. 解析请求体为 `XcxLoginBody`
2. 使用 `appid` + `xcxCode` 调用微信登录凭证校验
3. 获取 `openid` 和 `unionId`
4. 按 openid 查询绑定用户（当前代码为 TODO 占位）
5. 构建 `XcxLoginUser`（扩展类型，含 openid 字段）
6. 生成 Token，返回含 openid 的 `LoginVo`

**注意**: 当前实现中 `loadUserByOpenid()` 方法为占位代码，需自行实现用户查询/创建逻辑。

---

## 3. 登录流程（通用）

```
客户端                          服务端
  |                               |
  |  1. GET /auth/code            |
  |  -------------------------->  |  生成图形验证码，存入 Redis
  |  <--------------------------  |  返回 img(base64) + uuid
  |                               |
  |  2. GET /auth/tenant/list     |
  |  -------------------------->  |  查询租户列表
  |  <--------------------------  |  返回租户下拉数据
  |                               |
  |  3. POST /auth/login          |
  |  (RSA 加密请求体)              |  校验 client、租户
  |  -------------------------->  |  按 grantType 路由到策略
  |                               |  执行认证逻辑
  |                               |  生成 JWT Token
  |  <--------------------------  |  返回 access_token + expireIn
  |                               |
  |  4. SSE 推送（5 秒后）         |  推送欢迎消息
```

### 3.1 登录前置校验

1. **Client 校验**: 检查 `clientId` 是否存在，且 `grantType` 在该 client 的授权类型列表中
2. **Client 状态**: 检查 client 是否启用
3. **租户校验**: 检查租户是否存在、是否过期、是否被停用

### 3.2 Token 配置

Token 参数由 `SysClientVo` 控制:

| 参数 | 说明 |
|------|------|
| deviceType | 设备类型（PC/APP/小程序） |
| timeout | Token 总有效期（秒） |
| activeTimeout | Token 活跃有效期（秒），超时需重新登录 |
| clientId | 客户端 ID，存入 Token 额外信息 |

### 3.3 登录后行为

- 5 秒后通过 SSE 推送欢迎消息
- 记录登录成功日志
- 若勾选"记住密码"，前端将 tenantId、username、password 存入 localStorage

---

## 4. 验证码机制

### 4.1 图形验证码

**接口**: `GET /auth/code`

**实现**: `CaptchaController.getCode()`

**特性**:
- 支持两种类型: 数学运算（math）和随机字符（random）
- 数学验证码使用 SpEL 表达式计算结果
- 图片尺寸: 160x60，字体: Arial Bold 45
- 样式: 波浪+扭曲（WaveAndCircleCaptcha）
- Redis Key: `captcha_code_key:{uuid}`
- 有效期: `Constants.CAPTCHA_EXPIRATION` 分钟
- 限流: IP 级别，60 秒内最多 10 次

**开关**: 由 `captchaProperties.enable` 控制，关闭时返回 `captchaEnabled: false`

### 4.2 短信验证码

**接口**: `GET /resource/sms/code?phonenumber={phone}`

**实现**: `CaptchaController.smsCode()`

**特性**:
- 4 位随机数字
- Redis Key: `captcha_code_key:{phonenumber}`
- 有效期: `Constants.CAPTCHA_EXPIRATION` 分钟
- 限流: 手机号级别，60 秒内最多 1 次
- 使用 SMS4J 发送，配置标识为 `config1`
- 模板 ID 需自行配置

### 4.3 邮箱验证码

**接口**: `GET /resource/email/code?email={email}`

**实现**: `CaptchaController.emailCode()`

**特性**:
- 4 位随机数字
- Redis Key: `captcha_code_key:{email}`
- 有效期: `Constants.CAPTCHA_EXPIRATION` 分钟
- 限流: 邮箱级别，60 秒内最多 1 次
- 邮件标题: "登录验证码"
- 邮件内容: 包含验证码和有效期说明
- 开关: 由 `mailProperties.enabled` 控制

---

## 5. 注册流程

**接口**: `POST /auth/register`

**实现**: `AuthController.register()`

**流程**:
1. 校验系统是否开启注册功能（`sys.register` 配置项）
2. 校验请求体（`RegisterBody`）
3. 调用 `SysRegisterService.register()` 执行注册
4. 注册成功后返回

**前端校验规则**:
- 用户名: 必填，2-20 字符
- 密码: 必填，5-20 字符，禁止包含 `< > " ' \ |`
- 确认密码: 必填，须与密码一致
- 验证码: 必填（图形验证码启用时）
- 租户: 必填（多租户启用时）

**请求头**: `isEncrypt: true`（RSA 加密）

---

## 6. 退出登录

**接口**: `POST /auth/logout`

**实现**: `AuthController.logout()`

**流程**:
1. 若 SSE 功能启用，先关闭 SSE 连接（`GET /resource/sse/close`）
2. 调用 `SysLoginService.logout()` 清除 Sa-Token 会话
3. 记录退出日志

---

## 7. 租户选择

**接口**: `GET /auth/tenant/list`

**实现**: `AuthController.tenantList()`

**逻辑**:
1. 检查多租户功能是否启用
2. 若未启用，直接返回 `tenantEnabled: false`
3. 若当前用户为超级管理员，返回全部租户列表
4. 否则根据请求域名（referer 或 URL host）筛选匹配租户
5. 若无匹配结果，返回全部租户列表
6. 限流: IP 级别，60 秒内最多 20 次

---

## 8. 第三方社交账号管理

### 8.1 绑定社交账号

**接口**: `POST /auth/social/callback`

**前提**: 用户已登录（需 Token）

**流程**:
1. 校验用户已登录
2. 使用 `socialCode` + `socialState` 完成第三方授权
3. 调用 `SysLoginService.socialRegister()` 注册/绑定社交账号

### 8.2 获取绑定跳转 URL

**接口**: `GET /auth/binding/{source}?tenantId={tid}&domain={domain}`

**流程**:
1. 校验该平台是否配置了社交登录
2. 生成 state 并编码为 JSON
3. 返回第三方授权跳转 URL

### 8.3 取消社交授权

**接口**: `DELETE /auth/unlock/{socialId}`

**前提**: 用户已登录（需 Token）

**流程**: 删除指定的社交账号绑定记录

---

## 9. 安全机制

### 9.1 密码安全

- 密码使用 BCrypt 加密存储，禁止明文保存
- 登录/注册请求体使用 RSA 加密传输（`@ApiEncrypt`）

### 9.2 防暴力破解

- 图形验证码: 一次性使用，校验后立即删除
- 登录失败记录日志（IP、时间、用户名、结果）
- 登录接口限流: IP 级别，60 秒内最多 20 次

### 9.3 会话安全

- Token 存储在 Redis，支持分布式部署
- 每次登录生成新 Token（`is-share: false`）
- 支持多端同时登录（`is-concurrent: true`）
- Token 过期时间可按客户端配置

### 9.4 租户隔离

- 登录时动态切换租户上下文（`TenantHelper.dynamic()`）
- 用户数据按租户隔离

---

## 10. 数据模型

认证模块不定义独立实体，依赖以下已有模型:

| 模型 | 来源 | 用途 |
|------|------|------|
| `SysUser` | system 模块 | 用户信息存储 |
| `SysTenant` | system 模块 | 租户信息 |
| `SysClient` | system 模块 | 客户端配置（授权类型、Token 参数） |
| `SysSocial` | system 模块 | 第三方社交账号绑定 |
| `LoginUser` | common 模块 | 登录用户上下文 |
| `XcxLoginUser` | common 模块 | 小程序登录用户上下文（扩展 LoginUser） |
| `LoginVo` | auth 模块 | 登录响应（access_token、expireIn、clientId、openid） |
| `LoginTenantVo` | auth 模块 | 租户下拉响应 |
| `TenantListVo` | auth 模块 | 租户列表项 |
| `CaptchaVo` | auth 模块 | 验证码响应 |

---

## 11. 错误码

| 错误场景 | 异常类型 | 提示信息 |
|----------|----------|----------|
| 授权类型不支持 | `ServiceException` | "授权类型不正确!" |
| Client 不存在或不含该 grantType | - | "auth.grant.type.error" |
| Client 已停用 | - | "auth.grant.type.blocked" |
| 用户不存在 | `UserException` | "user.not.exists" |
| 用户已停用 | `UserException` | "user.blocked" |
| 验证码过期 | `CaptchaExpireException` | "user.jcaptcha.expire" |
| 验证码错误 | `CaptchaException` | "user.jcaptcha.error" |
| 社交账号未绑定 | `ServiceException` | "你还没有绑定第三方账号，绑定后才可以登录！" |
| 社交账号无当前租户权限 | `ServiceException` | "对不起，你没有权限登录当前租户！" |
| 注册功能未开启 | - | "当前系统没有开启注册功能！" |
| 邮箱功能未开启 | - | "当前系统没有开启邮箱功能！" |

---

## 12. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
