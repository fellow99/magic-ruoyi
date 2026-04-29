# 认证模块 API 文档（001-auth/api.md）

> magic-ruoyi 认证模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 认证模块前缀 | `/auth` |
| 资源模块前缀 | `/resource` |
| API 文档 | `http://{host}:8080/swagger-ui/index.html` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| code | Integer | 是 | 状态码。200=成功，500=服务端错误 |
| msg | String | 是 | 提示信息 |
| data | T | 否 | 响应数据体 |

### 1.3 请求头配置

| 请求头 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| Authorization | String | 是（认证接口除外） | Bearer Token，格式 `Bearer {token}` |
| Content-Type | String | 是（POST/PUT） | `application/json` |
| encrypt-key | String | 否 | RSA 加密密钥（加密接口需要） |

### 1.4 前端请求配置项

| 配置项 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| isToken | Boolean | true | 是否需要 Token |
| isEncrypt | Boolean | false | 是否使用 RSA 加密请求体 |
| repeatSubmit | Boolean | true | 是否防重复提交 |

---

## 2. 认证接口（`/auth/**`）

### 2.1 用户登录

```
POST /auth/login
```

**认证**: 不需要
**加密**: 是（`@ApiEncrypt`，RSA 加密请求体）
**防重**: 否

**请求体**:

```json
{
  "tenantId": "000000",
  "username": "admin",
  "password": "encrypted_password",
  "code": "5",
  "uuid": "abc123...",
  "clientId": "e5cd7e4891bf95d1d19206ce24a7b32e",
  "grantType": "password"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tenantId | String | 是 | 租户 ID |
| clientId | String | 是 | 客户端 ID |
| grantType | String | 是 | 授权类型 |
| username | String | 条件 | 用户名（password 策略必填） |
| password | String | 条件 | 密码（password 策略必填，RSA 加密） |
| code | String | 条件 | 验证码答案（验证码启用时必填） |
| uuid | String | 条件 | 验证码 UUID（验证码启用时必填） |
| phonenumber | String | 条件 | 手机号（sms 策略必填） |
| smsCode | String | 条件 | 短信验证码（sms 策略必填） |
| email | String | 条件 | 邮箱（email 策略必填） |
| emailCode | String | 条件 | 邮箱验证码（email 策略必填） |
| source | String | 条件 | 社交来源（social 策略必填） |
| socialCode | String | 条件 | 社交授权码（social 策略必填） |
| socialState | String | 条件 | 社交 state（social 策略必填） |
| xcxCode | String | 条件 | 小程序 code（xcx 策略必填） |
| appid | String | 条件 | 小程序 appid（xcx 策略必填） |

**grantType 枚举值**:

| 值 | 对应策略 | 说明 |
|----|----------|------|
| `password` | PasswordAuthStrategy | 用户名密码登录 |
| `sms` | SmsAuthStrategy | 短信验证码登录 |
| `email` | EmailAuthStrategy | 邮箱验证码登录 |
| `social` | SocialAuthStrategy | 第三方社交登录 |
| `xcx` | XcxAuthStrategy | 微信小程序登录 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireIn": 604800,
    "clientId": "e5cd7e4891bf95d1d19206ce24a7b32e",
    "openid": "oXXXX..."
  }
}
```

| 字段 | 类型 | 描述 |
|------|------|------|
| access_token | String | JWT Token，用于后续请求认证 |
| expireIn | Long | Token 有效期（秒） |
| clientId | String | 客户端 ID |
| openid | String | 微信 openid（仅 xcx 策略返回） |

**错误响应**:

| code | msg | 场景 |
|------|-----|------|
| 500 | 授权类型不正确! | grantType 无对应策略实现 |
| 500 | auth.grant.type.error | clientId 不存在或不包含该 grantType |
| 500 | auth.grant.type.blocked | Client 已停用 |
| 500 | user.not.exists | 用户不存在 |
| 500 | user.blocked | 用户已停用 |
| 500 | user.jcaptcha.expire | 验证码已过期 |
| 500 | user.jcaptcha.error | 验证码错误 |

---

### 2.2 用户注册

```
POST /auth/register
```

**认证**: 不需要
**加密**: 是（`@ApiEncrypt`）
**防重**: 否

**请求体**:

```json
{
  "tenantId": "000000",
  "username": "newuser",
  "password": "encrypted_password",
  "confirmPassword": "encrypted_password",
  "code": "5",
  "uuid": "abc123...",
  "userType": "sys_user"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tenantId | String | 是 | 租户 ID |
| username | String | 是 | 用户名（2-20 字符） |
| password | String | 是 | 密码（5-20 字符，RSA 加密） |
| confirmPassword | String | 是 | 确认密码 |
| code | String | 条件 | 验证码答案（验证码启用时必填） |
| uuid | String | 条件 | 验证码 UUID（验证码启用时必填） |
| userType | String | 否 | 用户类型，默认 `sys_user` |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**错误响应**:

| code | msg | 场景 |
|------|-----|------|
| 500 | 当前系统没有开启注册功能！ | 系统配置关闭了注册功能 |

---

### 2.3 退出登录

```
POST /auth/logout
```

**认证**: 需要 Token
**加密**: 否

**请求体**: 无

**响应**:

```json
{
  "code": 200,
  "msg": "退出成功",
  "data": null
}
```

**副作用**:
- 清除 Sa-Token 会话
- 若 SSE 功能启用，同时关闭 SSE 连接（`GET /resource/sse/close`）

---

### 2.4 获取图形验证码

```
GET /auth/code
```

**认证**: 不需要
**加密**: 否
**限流**: IP 级别，60 秒内最多 10 次

**请求参数**: 无

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "captchaEnabled": true,
    "uuid": "abc123...",
    "img": "data:image/gif;base64,iVBORw0KGgo..."
  }
}
```

| 字段 | 类型 | 描述 |
|------|------|------|
| captchaEnabled | Boolean | 验证码功能是否启用 |
| uuid | String | 验证码唯一标识，登录时需回传 |
| img | String | Base64 编码的验证码图片 |

**验证码关闭时**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "captchaEnabled": false
  }
}
```

---

### 2.5 获取租户列表

```
GET /auth/tenant/list
```

**认证**: 不需要
**加密**: 否
**限流**: IP 级别，60 秒内最多 20 次

**请求参数**: 无

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "tenantEnabled": true,
    "voList": [
      {
        "tenantId": "000000",
        "companyName": "默认租户",
        "domain": "localhost"
      }
    ]
  }
}
```

| 字段 | 类型 | 描述 |
|------|------|------|
| tenantEnabled | Boolean | 多租户功能是否启用 |
| voList | Array\<TenantListVo\> | 租户列表 |

**TenantListVo 结构**:

| 字段 | 类型 | 描述 |
|------|------|------|
| tenantId | String | 租户 ID |
| companyName | String | 公司名称 |
| domain | String | 绑定域名 |

---

### 2.6 获取第三方绑定跳转 URL

```
GET /auth/binding/{source}
```

**认证**: 不需要
**加密**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| source | String | 是 | 登录来源（wechat、maxkey、topiam、gitee、github 等） |

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tenantId | String | 是 | 租户 ID |
| domain | String | 是 | 当前域名（`window.location.host`） |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "https://open.weixin.qq.com/connect/oauth2/authorize?..."
}
```

**错误响应**:

| code | msg | 场景 |
|------|-----|------|
| 500 | {source}平台账号暂不支持 | 该平台未配置社交登录 |

---

### 2.7 第三方回调绑定

```
POST /auth/social/callback
```

**认证**: 需要 Token
**加密**: 否

**请求体**:

```json
{
  "source": "wechat",
  "socialCode": "code_from_third_party",
  "socialState": "state_from_third_party"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| source | String | 是 | 登录来源 |
| socialCode | String | 是 | 第三方返回的授权码 |
| socialState | String | 是 | 第三方返回的 state 参数 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 2.8 取消第三方授权

```
DELETE /auth/unlock/{socialId}
```

**认证**: 需要 Token
**加密**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| socialId | Long | 是 | 社交账号绑定记录 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

## 3. 资源接口（`/resource/**`）

### 3.1 发送短信验证码

```
GET /resource/sms/code
```

**认证**: 不需要
**加密**: 否
**限流**: 手机号级别，60 秒内最多 1 次

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| phonenumber | String | 是 | 手机号 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**错误响应**:

| code | msg | 场景 |
|------|-----|------|
| 500 | SMS 发送失败的具体信息 | 短信服务商返回错误 |

---

### 3.2 发送邮箱验证码

```
GET /resource/email/code
```

**认证**: 不需要
**加密**: 否
**限流**: 邮箱级别，60 秒内最多 1 次

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| email | String | 是 | 邮箱地址 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**错误响应**:

| code | msg | 场景 |
|------|-----|------|
| 500 | 当前系统没有开启邮箱功能！ | 邮件服务未启用 |
| 500 | 邮件发送失败的具体信息 | 邮件发送异常 |

---

## 4. 限流策略汇总

| 接口 | 限流维度 | 时间窗口 | 最大次数 |
|------|----------|----------|----------|
| `GET /auth/code` | IP | 60 秒 | 10 次 |
| `GET /auth/tenant/list` | IP | 60 秒 | 20 次 |
| `GET /resource/sms/code` | 手机号 | 60 秒 | 1 次 |
| `GET /resource/email/code` | 邮箱 | 60 秒 | 1 次 |

---

## 5. 前端 API 调用示例

### 5.1 登录

```typescript
import { login } from '@/api/login';

const result = await login({
  tenantId: '000000',
  username: 'admin',
  password: 'admin123',
  code: '5',
  uuid: 'abc123...',
  clientId: 'e5cd7e4891bf95d1d19206ce24a7b32e',
  grantType: 'password'
});

// result.data.access_token → JWT Token
// result.data.expireIn → 过期时间（秒）
```

### 5.2 注册

```typescript
import { register } from '@/api/login';

await register({
  tenantId: '000000',
  username: 'newuser',
  password: 'password123',
  confirmPassword: 'password123',
  code: '5',
  uuid: 'abc123...',
  userType: 'sys_user'
});
```

### 5.3 获取验证码

```typescript
import { getCodeImg } from '@/api/login';

const { data } = await getCodeImg();
// data.captchaEnabled → 是否启用验证码
// data.uuid → 验证码 UUID
// data.img → Base64 图片
```

### 5.4 社交登录跳转

```typescript
import { authRouterUrl } from '@/api/system/social/auth';

const res = await authRouterUrl('wechat', '000000');
if (res.code === 200) {
  window.location.href = res.data; // 跳转到微信授权页
}
```

### 5.5 获取租户列表

```typescript
import { getTenantList } from '@/api/login';

const { data } = await getTenantList(false);
// data.tenantEnabled → 是否启用多租户
// data.voList → 租户列表数组
```

---

## 6. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
