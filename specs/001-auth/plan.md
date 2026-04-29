# 001-Auth 认证模块 - 技术实现方案（Plan）

> magic-ruoyi 认证模块技术实现方案。定义认证策略、登录流程、验证码机制、注册流程的技术实现细节。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. Technical Context

### 1.1 运行环境

| 组件 | 版本/配置 | 说明 |
|------|-----------|------|
| Java | 21+ | 语言版本 |
| Spring Boot | 3.5.14 | 基础框架 |
| RuoYi-Vue-Plus | 5.6.0 | 父框架 |
| Sa-Token | 1.44.0 | 认证框架 |
| MySQL | 最新稳定版 | 用户/租户/客户端数据存储 |
| Redis | 最新稳定版 | 验证码、会话、分布式锁 |
| Vue | 3.5.22 | 前端框架 |
| Element Plus | 2.11.7 | 前端 UI 库 |

### 1.2 模块依赖

| 依赖 | 来源 | 用途 |
|------|------|------|
| `ruoyi-common-satoken` | org.dromara | Sa-Token 集成、LoginHelper |
| `ruoyi-common-tenant` | org.dromara | TenantHelper 租户上下文 |
| `ruoyi-common-redis` | org.dromara | RedisUtils 缓存操作 |
| `ruoyi-common-ratelimiter` | org.dromara | @RateLimiter 限流注解 |
| `ruoyi-common-encrypt` | org.dromara | @ApiEncrypt 接口加密 |
| `ruoyi-common-web` | org.dromara | WaveAndCircleCaptcha、CaptchaProperties |
| `ruoyi-common-mail` | org.dromara | MailUtils 邮件发送 |
| `ruoyi-common-social` | org.dromara | JustAuth 集成、SocialUtils |
| `ruoyi-system` | org.dromara | SysUser、SysTenant、SysClient、SysSocial 等服务 |
| `sms4j-core` | org.dromara | SMS4J 短信发送 |
| `hutool-crypto` | cn.hutool | BCrypt 密码校验 |
| `hutool-captcha` | cn.hutool | MathGenerator、RandomGenerator |

### 1.3 包结构

```
org.fellow99.magic.ruoyi
├── controller/
│   ├── AuthController.java          # 认证入口（登录、注册、退出、社交绑定、租户列表）
│   └── CaptchaController.java       # 验证码入口（图形、短信、邮箱）
├── service/
│   ├── IAuthStrategy.java           # 认证策略接口（策略模式路由）
│   ├── SysLoginService.java         # 登录公共服务（租户校验、登录记录、用户构建）
│   ├── SysRegisterService.java      # 注册服务
│   └── impl/
│       ├── PasswordAuthStrategy.java    # 密码认证策略
│       ├── SmsAuthStrategy.java         # 短信验证码认证策略
│       ├── EmailAuthStrategy.java       # 邮箱验证码认证策略
│       ├── SocialAuthStrategy.java      # 第三方社交认证策略
│       └── XcxAuthStrategy.java         # 微信小程序认证策略
└── domain/vo/
    ├── LoginVo.java                 # 登录响应（access_token、expire_in、client_id、openid）
    ├── CaptchaVo.java               # 验证码响应（captchaEnabled、uuid、img）
    ├── LoginTenantVo.java           # 登录租户响应（tenantEnabled、voList）
    └── TenantListVo.java            # 租户列表项（tenantId、companyName、domain）
```

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 策略模式通过接口 + Bean 命名实现，不引入额外抽象层。每种策略实现不超过 120 行 |
| 约定优于配置 | 合规 | Bean 命名遵循 `grantType + "AuthStrategy"` 约定，路由逻辑统一在 IAuthStrategy.login() 静态方法中 |
| 实用优于完美 | 合规 | 五种认证策略按需实现，XcxAuthStrategy 中 loadUserByOpenid() 为 TODO 占位，不影响其他策略 |
| 安全优于便利 | 合规 | @ApiEncrypt 加密登录/注册请求体，@RateLimiter 限流，验证码一次性使用，BCrypt 密码加密 |
| 零样板代码 | 合规 | 使用 MapStruct 对象转换，Lombok @Data/@RequiredArgsConstructor 减少样板代码 |
| 多租户优先 | 合规 | 所有策略通过 TenantHelper.dynamic() 设置租户上下文，用户查询自动按租户隔离 |
| 前后端分离 | 合规 | 前端独立 login.vue/register.vue，通过 JWT Token 认证，RSA 加密请求体 |
| 依赖整合，非复制 | 合规 | 自定义代码使用 org.fellow99 包名，与上游 org.dromara 严格隔离 |

---

## 3. Research Findings

### 3.1 策略模式实现决策

**决策**: 使用 Spring Bean 命名 + 静态方法路由实现策略模式。

**理由**:
- `IAuthStrategy.login()` 静态方法接收 `grantType`，拼接 `BASE_NAME` 得到 Bean 名称
- 通过 `SpringUtils.containsBean()` 校验策略是否存在，不存在则抛 ServiceException
- 每种策略通过 `@Service("xxx" + IAuthStrategy.BASE_NAME)` 注册，如 `@Service("passwordAuthStrategy")`
- 相比 Map 注册表或枚举路由，Spring Bean 命名天然支持依赖注入和懒加载

**替代方案排除**:
- Map 注册表: 需要手动维护注册逻辑，新增策略容易遗漏
- 枚举路由: 硬编码枚举值，扩展性差

### 3.2 验证码存储决策

**决策**: 验证码统一存储在 Redis，Key 格式为 `GlobalConstants.CAPTCHA_CODE_KEY + {identifier}`。

**标识符映射**:

| 验证码类型 | 标识符 | Key 示例 |
|-----------|--------|---------|
| 图形验证码 | UUID | `captcha_code_key:a1b2c3d4...` |
| 短信验证码 | 手机号 | `captcha_code_key:13800138000` |
| 邮箱验证码 | 邮箱 | `captcha_code_key:user@example.com` |

**理由**: Redis 天然支持过期时间设置，分布式部署下共享验证码数据。

### 3.3 Token 有效期决策

**决策**: Token 有效期由 `SysClientVo` 配置控制，不同客户端可设置不同的 timeout 和 activeTimeout。

**理由**: 支持多端差异化过期策略，如 PC 端 30 分钟、APP 端 1 天。

### 3.4 登录后 SSE 推送决策

**决策**: 登录成功后 5 秒通过 SSE 推送欢迎消息。

**理由**: 增强用户体验，展示系统实时通信能力。使用 `ScheduledExecutorService` 延迟执行，不阻塞登录响应。

---

## 4. Data Model

### 4.1 认证模块 VO 定义

#### LoginVo（登录响应）

| 字段 | 类型 | JSON Key | 说明 |
|------|------|----------|------|
| accessToken | String | access_token | JWT 授权令牌 |
| refreshToken | String | refresh_token | 刷新令牌（预留） |
| expireIn | Long | expire_in | access_token 有效期（秒） |
| refreshExpireIn | Long | refresh_expire_in | refresh_token 有效期（秒） |
| clientId | String | client_id | 客户端 ID |
| scope | String | scope | 令牌权限范围 |
| openid | String | openid | 小程序用户 openid |

#### CaptchaVo（验证码响应）

| 字段 | 类型 | 说明 |
|------|------|------|
| captchaEnabled | Boolean | 是否开启验证码（默认 true） |
| uuid | String | 验证码唯一标识 |
| img | String | 验证码图片 Base64 |

#### LoginTenantVo（登录租户响应）

| 字段 | 类型 | 说明 |
|------|------|------|
| tenantEnabled | Boolean | 租户开关 |
| voList | List\<TenantListVo\> | 租户列表 |

#### TenantListVo（租户列表项）

| 字段 | 类型 | 说明 |
|------|------|------|
| tenantId | String | 租户编号 |
| companyName | String | 企业名称 |
| domain | String | 绑定域名 |

### 4.2 依赖的外部实体

| 实体 | 来源 | 认证模块中的用途 |
|------|------|-----------------|
| SysUser | ruoyi-system | 用户信息查询、密码校验 |
| SysTenant | ruoyi-system | 租户状态校验（是否停用、是否过期） |
| SysClient | ruoyi-system | 客户端配置（授权类型、Token 参数） |
| SysSocial | ruoyi-system | 第三方社交账号绑定记录 |
| LoginUser | ruoyi-common | 登录用户上下文（构建后存入 Sa-Token 会话） |
| XcxLoginUser | ruoyi-common | 小程序登录用户上下文（扩展 LoginUser，含 openid） |

### 4.3 状态转换

```
[未登录] ──POST /auth/login──▶ [已登录]
    │                              │
    │  验证码错误/密码错误/用户停用   │  POST /auth/logout
    ▼                              ▼
[登录失败]                      [已退出]

[未注册] ──POST /auth/register──▶ [已注册] ──POST /auth/login──▶ [已登录]
```

### 4.4 验证规则

| 规则 | 适用场景 | 验证方式 |
|------|---------|---------|
| 图形验证码 | 密码登录（启用时） | Redis 中比对，不区分大小写，校验后删除 |
| 短信验证码 | 短信登录 | Redis 中比对，为空抛 CaptchaExpireException |
| 邮箱验证码 | 邮箱登录 | Redis 中比对，为空抛 CaptchaExpireException |
| Client 校验 | 所有登录 | clientId 存在且 grantType 在授权类型列表中 |
| 租户校验 | 所有登录 | 租户存在、未停用、未过期 |
| 用户状态 | 所有登录 | 用户存在且状态为正常（0） |
| 密码校验 | 密码登录 | BCrypt.checkpw() 比对 |

---

## 5. Interface Contracts

### 5.1 提供接口（后端 API）

#### 认证接口（/auth）

| 方法 | 路径 | 认证 | 加密 | 限流 | 说明 |
|------|------|------|------|------|------|
| POST | /auth/login | 否 | 是 | 否 | 用户登录（5 种 grantType） |
| POST | /auth/register | 否 | 是 | 否 | 用户注册 |
| POST | /auth/logout | 是 | 否 | 否 | 退出登录 |
| GET | /auth/tenant/list | 否 | 否 | IP: 20次/60s | 登录页租户下拉 |
| GET | /auth/binding/{source} | 否 | 否 | 否 | 获取社交授权跳转 URL |
| POST | /auth/social/callback | 是 | 否 | 否 | 前端回调绑定社交账号 |
| DELETE | /auth/unlock/{socialId} | 是 | 否 | 否 | 取消社交账号授权 |

#### 验证码接口

| 方法 | 路径 | 认证 | 限流 | 说明 |
|------|------|------|------|------|
| GET | /auth/code | 否 | IP: 10次/60s | 生成图形验证码 |
| GET | /resource/sms/code?phonenumber= | 否 | 手机号: 1次/60s | 发送短信验证码 |
| GET | /resource/email/code?email= | 否 | 邮箱: 1次/60s | 发送邮箱验证码 |

### 5.2 消费接口（依赖上游）

| 接口 | 来源 | 用途 |
|------|------|------|
| ISysClientService.queryByClientId() | ruoyi-system | 查询客户端配置 |
| ISysTenantService.queryList() | ruoyi-system | 查询租户列表 |
| ISysConfigService.selectRegisterEnabled() | ruoyi-system | 检查注册功能开关 |
| ISysSocialService.deleteWithValidById() | ruoyi-system | 删除社交账号绑定 |
| SysUserMapper.selectVoOne() | ruoyi-system | 按用户名/手机号/邮箱查询用户 |
| SysLoginService.checkTenant() | org.fellow99 | 校验租户状态 |
| SysLoginService.checkLogin() | org.fellow99 | 校验登录失败次数 |
| SysLoginService.buildLoginUser() | org.fellow99 | 构建登录用户上下文 |
| SysLoginService.recordLogininfor() | org.fellow99 | 记录登录日志 |
| SysLoginService.socialRegister() | org.fellow99 | 注册/绑定社交账号 |
| SysRegisterService.register() | org.fellow99 | 执行用户注册 |

### 5.3 事件协议

| 事件 | 触发时机 | 内容 |
|------|---------|------|
| SSE 欢迎消息 | 登录成功后 5 秒 | 用户 ID + 欢迎语（含时间段问候） |
| LogininforEvent | 登录成功/失败 | 租户 ID、用户名、IP、消息、状态 |

### 5.4 前端 API 客户端

| 函数 | 文件 | 方法 | 路径 | 请求头 |
|------|------|------|------|--------|
| login() | api/login.ts | POST | /auth/login | isToken: false, isEncrypt: true |
| register() | api/login.ts | POST | /auth/register | isToken: false, isEncrypt: true |
| logout() | api/login.ts | POST | /auth/logout | - |
| getCodeImg() | api/login.ts | GET | /auth/code | isToken: false |
| getTenantList() | api/login.ts | GET | /auth/tenant/list | isToken: 动态 |
| callback() | api/login.ts | POST | /auth/social/callback | - |
| getInfo() | api/login.ts | GET | /system/user/getInfo | - |

---

## 6. Implementation Strategy

### 6.1 架构模式

**策略模式（Strategy Pattern）**

```
                    ┌─────────────────┐
                    │  AuthController │
                    └────────┬────────┘
                             │ login()
                             ▼
                    ┌─────────────────┐
                    │ IAuthStrategy   │ ← 静态方法 login(body, client, grantType)
                    │   (接口)         │    通过 grantType + "AuthStrategy" 路由
                    └────────┬────────┘
                             │ SpringUtils.getBean(beanName)
              ┌──────────────┼──────────────┬──────────────┬──────────────┐
              ▼              ▼              ▼              ▼              ▼
     ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
     │ Password    │ │ Sms         │ │ Email       │ │ Social      │ │ Xcx         │
     │ AuthStrategy│ │ AuthStrategy│ │ AuthStrategy│ │ AuthStrategy│ │ AuthStrategy│
     └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
```

### 6.2 关键算法

#### 6.2.1 登录通用流程

```
1. 解析请求体为 LoginBody（根据 grantType 选择具体子类）
2. 参数校验（ValidatorUtils.validate）
3. 前置校验:
   a. Client 校验: clientId 存在且 grantType 在授权类型列表中
   b. Client 状态: 必须为正常（0）
   c. 租户校验: checkTenant() 检查租户存在、未停用、未过期
4. 策略路由: IAuthStrategy.login(body, client, grantType)
   a. 拼接 Bean 名称: grantType + "AuthStrategy"
   b. SpringUtils.containsBean() 校验
   c. SpringUtils.getBean() 获取实例
   d. 调用 instance.login(body, client)
5. 策略内部:
   a. 验证码校验（若启用）
   b. TenantHelper.dynamic(tenantId, () -> {...}) 设置租户上下文
   c. 查询用户（按用户名/手机号/邮箱/openid）
   d. 校验用户状态
   e. checkLogin() 校验失败次数（防暴力破解）
   f. buildLoginUser() 构建 LoginUser
6. 生成 Token: LoginHelper.login(loginUser, model)
   a. 设置 deviceType、timeout、activeTimeout
   b. 设置 extra: clientId
7. 构建 LoginVo 返回
8. 延迟 5 秒发送 SSE 欢迎消息
```

#### 6.2.2 验证码生成算法

```
1. 检查 captchaProperties.enable，关闭则返回 captchaEnabled: false
2. 生成 UUID 作为验证码标识
3. 根据 captchaProperties.type 选择生成器:
   - "math": MathGenerator（数学运算表达式）
   - 其他: RandomGenerator（随机字符）
4. 创建 WaveAndCircleCaptcha（160x60，Arial Bold 45）
5. 生成验证码图片
6. 数学类型使用 SpEL 表达式计算结果
7. 存入 Redis: CAPTCHA_CODE_KEY + uuid，过期时间 CAPTCHA_EXPIRATION 分钟
8. 返回 CaptchaVo（uuid + img Base64）
```

#### 6.2.3 租户选择算法

```
1. 检查 TenantHelper.isEnable()，未启用则返回 tenantEnabled: false
2. 查询全部租户列表（SysTenantBo 空条件）
3. 转换为 TenantListVo 列表
4. 若当前用户为超级管理员，返回全部租户
5. 否则:
   a. 从 referer 或 request URL 提取域名（host）
   b. 按 domain 筛选匹配的租户
   c. 若无匹配结果，返回全部租户（兜底）
```

### 6.3 错误处理

| 异常类型 | 触发条件 | 处理方式 |
|---------|---------|---------|
| ServiceException | grantType 不支持、社交账号未绑定、社交账号无租户权限 | 返回 R.fail(message) |
| CaptchaExpireException | 验证码过期或不存在 | 记录登录失败日志，抛出异常由全局处理器捕获 |
| CaptchaException | 验证码不匹配 | 记录登录失败日志，抛出异常由全局处理器捕获 |
| UserException | 用户不存在、用户已停用 | 记录登录失败日志，抛出异常由全局处理器捕获 |
| UserException | 注册功能未开启 | 返回 R.fail("当前系统没有开启注册功能！") |
| ServiceException | 邮箱功能未开启 | 返回 R.fail("当前系统没有开启邮箱功能！") |

### 6.4 性能优化

| 优化点 | 策略 |
|--------|------|
| 验证码限流 | @RateLimiter 注解，IP/手机号/邮箱级别 |
| 登录限流 | @RateLimiter 注解，IP 级别 20 次/60 秒 |
| Redis 操作 | 使用 RedisUtils 封装，统一 Key 前缀 |
| SSE 延迟推送 | ScheduledExecutorService 异步执行，不阻塞响应 |
| 验证码关闭时跳过限流 | CaptchaController 使用独立方法 + AOP 代理，避免关闭后仍走限流 |

### 6.5 安全控制

| 安全措施 | 实现方式 |
|---------|---------|
| 密码加密存储 | BCrypt |
| 请求体加密 | @ApiEncrypt + RSA 非对称加密 |
| 验证码一次性 | 校验后立即 Redis.deleteObject() |
| 防暴力破解 | checkLogin() 记录失败次数，锁定机制 |
| 分布式锁 | @Lock4j 防止并发登录问题 |
| 会话隔离 | Sa-Token + Redis，支持分布式部署 |
| 租户隔离 | TenantHelper.dynamic() 动态切换上下文 |

---

## 7. Testing Considerations

### 7.1 可测试性设计

- 策略实现类通过 `@RequiredArgsConstructor` 注入依赖，便于 Mock
- `IAuthStrategy.login()` 静态方法依赖 SpringUtils，测试时需 Mock Spring 容器
- 验证码校验、用户查询等逻辑已抽取为私有方法，可通过反射或提升可见性测试

### 7.2 测试类别

| 测试类型 | 测试对象 | 工具 |
|---------|---------|------|
| 单元测试 | PasswordAuthStrategy.login() | JUnit 5 + Mockito |
| 单元测试 | CaptchaController.getCodeImpl() | JUnit 5 + Mockito RedisUtils |
| 单元测试 | 各策略的 validateCaptcha/validateSmsCode | JUnit 5 + Mockito |
| 集成测试 | AuthController.login() | Spring Boot Test + MockMvc |
| 集成测试 | AuthController.register() | Spring Boot Test + MockMvc |
| 前端测试 | login.vue 表单校验 | Vitest + Vue Test Utils |

### 7.3 边缘情况

| 场景 | 预期行为 |
|------|---------|
| 不存在的 grantType | 返回 "授权类型不正确!" |
| 不存在的 clientId | 返回 "auth.grant.type.error" |
| 已停用的 Client | 返回 "auth.grant.type.blocked" |
| 已停用的租户 | 登录被拒绝 |
| 已过期的租户 | 登录被拒绝 |
| 不存在的用户 | 抛出 UserException |
| 已停用的用户 | 抛出 UserException |
| 验证码过期 | 抛出 CaptchaExpireException |
| 验证码错误 | 抛出 CaptchaException |
| 注册功能关闭 | 返回 "当前系统没有开启注册功能！" |
| 邮箱功能关闭 | 返回 "当前系统没有开启邮箱功能！" |
| 社交账号未绑定 | 返回 "你还没有绑定第三方账号..." |
| 社交账号无当前租户权限 | 返回 "对不起，你没有权限登录当前租户！" |
| 小程序 openid 未绑定用户 | TODO 占位，需自行实现 |

---

## 8. File Inventory

### 8.1 后端文件

| 文件路径 | 类型 | 行数 | 职责 |
|---------|------|------|------|
| `controller/AuthController.java` | Controller | 243 | 登录、注册、退出、社交绑定、租户列表 |
| `controller/CaptchaController.java` | Controller | 160 | 图形/短信/邮箱验证码生成与发送 |
| `service/IAuthStrategy.java` | Interface | 46 | 认证策略接口 + 静态路由方法 |
| `service/SysLoginService.java` | Service | - | 登录公共服务（租户校验、登录记录、用户构建） |
| `service/SysRegisterService.java` | Service | - | 用户注册服务 |
| `service/impl/PasswordAuthStrategy.java` | Service Impl | 123 | 密码认证策略实现 |
| `service/impl/SmsAuthStrategy.java` | Service Impl | 102 | 短信验证码认证策略实现 |
| `service/impl/EmailAuthStrategy.java` | Service Impl | 102 | 邮箱验证码认证策略实现 |
| `service/impl/SocialAuthStrategy.java` | Service Impl | 119 | 第三方社交认证策略实现 |
| `service/impl/XcxAuthStrategy.java` | Service Impl | 111 | 微信小程序认证策略实现（含 TODO） |
| `domain/vo/LoginVo.java` | VO | 54 | 登录响应对象 |
| `domain/vo/CaptchaVo.java` | VO | 25 | 验证码响应对象 |
| `domain/vo/LoginTenantVo.java` | VO | 25 | 登录租户响应对象 |
| `domain/vo/TenantListVo.java` | VO | 31 | 租户列表项对象 |

### 8.2 前端文件

| 文件路径 | 类型 | 行数 | 职责 |
|---------|------|------|------|
| `src/views/login.vue` | 页面组件 | 314 | 登录页面（表单、验证码、社交登录按钮） |
| `src/views/register.vue` | 页面组件 | 263 | 注册页面（表单、验证码、租户选择） |
| `src/api/login.ts` | API 模块 | 113 | 认证相关 API 函数封装 |

### 8.3 依赖的上游文件（org.dromara）

| 文件/类 | 来源模块 | 用途 |
|---------|---------|------|
| SysLoginService | ruoyi-system | 登录公共服务（checkTenant、checkLogin、buildLoginUser） |
| SysRegisterService | ruoyi-system | 注册服务 |
| SysUserMapper | ruoyi-system | 用户数据访问 |
| ISysClientService | ruoyi-system | 客户端配置服务 |
| ISysTenantService | ruoyi-system | 租户服务 |
| ISysSocialService | ruoyi-system | 社交账号服务 |
| ISysConfigService | ruoyi-system | 系统配置服务 |
| LoginHelper | ruoyi-common-satoken | Token 生成工具 |
| TenantHelper | ruoyi-common-tenant | 租户上下文工具 |
| RedisUtils | ruoyi-common-redis | Redis 操作工具 |
| WaveAndCircleCaptcha | ruoyi-common-web | 波浪+圆圈干扰验证码 |
| SocialUtils | ruoyi-common-social | 第三方登录工具 |
| MailUtils | ruoyi-common-mail | 邮件发送工具 |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
