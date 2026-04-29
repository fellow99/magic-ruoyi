# 对外接口模型 (overall-api.md)

> 本文档定义 magic-ruoyi 平台的全部对外 API 契约，包括 RuoYi-Vue-Plus 继承接口与 magic-api 动态接口。
>
> 版本: 1.0.0 | 最后更新: 2026-04-28

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 前端开发地址 | `http://{host}:8000` |
| API 文档 (Swagger) | `http://{host}:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://{host}:8080/v3/api-docs` |
| 基础路径 (Base Path) | `/` |

### 1.2 统一响应格式

所有 API 响应使用 `R<T>` 泛型包装:

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
| data | T | 否 | 响应数据体，类型随接口变化 |

### 1.3 分页响应格式

列表查询接口返回 `TableDataInfo<T>` 分页结构:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "rows": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

| 字段 | 类型 | 描述 |
|------|------|------|
| rows | Array<T> | 当前页数据列表 |
| total | Long | 符合条件的总记录数 |
| pageNum | Integer | 当前页码（从 1 开始） |
| pageSize | Integer | 每页条数 |

**分页请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 描述 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |

### 1.4 请求头规范

| 请求头 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| Authorization | String | 是（认证接口除外） | Bearer Token，格式 `Bearer {token}` |
| Tenant-Id | String | 是（多租户接口） | 租户 ID，默认 `000000` |
| Content-Type | String | 是（POST/PUT） | `application/json` |
| encrypt-key | String | 否 | RSA 加密密钥（加密接口需要） |

### 1.5 RESTful 路径规范

| 操作 | 方法 | 路径 | 示例 |
|------|------|------|------|
| 列表查询 | GET | `/{resource}/list` | `GET /system/user/list` |
| 详情查询 | GET | `/{resource}/{id}` | `GET /system/user/1` |
| 创建 | POST | `/{resource}` | `POST /system/user` |
| 更新 | PUT | `/{resource}` | `PUT /system/user` |
| 删除 | DELETE | `/{resource}/{ids}` | `DELETE /system/user/1,2,3` |
| 导出 | POST | `/{resource}/export` | `POST /system/user/export` |
| 导入 | POST | `/{resource}/importData` | `POST /system/user/importData` |

### 1.6 错误码规范

| 错误码 | 含义 | 处理建议 |
|--------|------|----------|
| 200 | 请求成功 | 正常处理 data |
| 401 | 未认证 / Token 失效 | 跳转登录页 |
| 403 | 无权限 | 提示用户权限不足 |
| 404 | 资源不存在 | 提示资源未找到 |
| 429 | 请求过于频繁 | 提示用户稍后重试 |
| 500 | 服务端内部错误 | 提示系统异常，联系管理员 |

---

## 2. 认证与授权

### 2.1 认证机制

平台采用 **Sa-Token + JWT** 认证方案:

- Token 名称: `Authorization`
- Token 格式: JWT
- 并发登录: 允许（`is-concurrent: true`）
- Token 共享: 不共享（`is-share: false`），每次登录生成新 Token
- Token 存储: Redis（分布式会话）

### 2.2 权限注解

| 注解 | 用途 | 示例 |
|------|------|------|
| `@SaIgnore` | 跳过认证（白名单） | 登录、验证码接口 |
| `@SaCheckLogin` | 校验已登录 | 任意需要登录的接口 |
| `@SaCheckRole("admin")` | 校验角色 | 仅管理员可访问 |
| `@SaCheckPermission("system:user:add")` | 校验权限标识 | 仅拥有该权限的用户可访问 |

### 2.3 免认证路径

以下路径无需 Token 即可访问:

```
/*.html
/**/*.html
/**/*.css
/**/*.js
/favicon.ico
/error
/*/api-docs
/*/api-docs/**
/warm-flow-ui/config
```

### 2.4 接口加密

敏感接口使用 RSA 非对称加密:

- 全局加密开关: `api-decrypt.enabled: true`
- 加密头标识: `encrypt-key`
- 请求加密: 前端公钥加密，后端私钥解密
- 响应加密: 后端公钥加密，前端私钥解密
- 强制加密注解: `@ApiEncrypt`（登录、注册等敏感接口）

---

## 3. 认证模块 API (`/auth/**`)

### 3.1 用户登录

```
POST /auth/login
```

**请求头**: `isEncrypt: true`（RSA 加密请求体）

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
| username | String | 是 | 用户名 |
| password | String | 是 | 密码（加密传输） |
| code | String | 条件 | 验证码答案（验证码启用时必填） |
| uuid | String | 条件 | 验证码 UUID（验证码启用时必填） |
| clientId | String | 是 | 客户端 ID |
| grantType | String | 是 | 授权类型: `password` / `social` |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireIn": 604800
  }
}
```

### 3.2 用户注册

```
POST /auth/register
```

**请求头**: `isEncrypt: true`

**请求体**:

```json
{
  "tenantId": "000000",
  "username": "newuser",
  "password": "encrypted_password",
  "code": "5",
  "uuid": "abc123..."
}
```

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### 3.3 退出登录

```
POST /auth/logout
```

**请求头**: 需要 `Authorization`

**响应**:

```json
{
  "code": 200,
  "msg": "退出成功",
  "data": null
}
```

### 3.4 获取验证码

```
GET /auth/code
```

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "captchaEnabled": true,
    "uuid": "abc123...",
    "img": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

### 3.5 获取租户列表

```
GET /auth/tenant/list
```

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

### 3.6 第三方登录绑定

```
GET /auth/binding/{source}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| source | String | 是 | 登录来源（qq, weibo, wechat, dingtalk 等） |
| tenantId | String | 是 | 租户 ID（Query 参数） |
| domain | String | 是 | 域名（Query 参数） |

**响应**: 返回第三方授权跳转 URL

### 3.7 第三方回调绑定

```
POST /auth/social/callback
```

**请求头**: 需要 `Authorization`

**请求体**:

```json
{
  "source": "qq",
  "socialCode": "code_from_third_party",
  "socialState": "state_from_third_party"
}
```

### 3.8 取消第三方授权

```
DELETE /auth/unlock/{socialId}
```

**请求头**: 需要 `Authorization`

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| socialId | Long | 是 | 社交账号 ID（路径参数） |

---

## 4. 资源模块 API (`/resource/**`)

### 4.1 短信验证码

```
GET /resource/sms/code
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| phonenumber | String | 是 | 手机号（Query 参数） |

**限流**: 同一手机号 60 秒内最多 1 次

### 4.2 邮箱验证码

```
GET /resource/email/code
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| email | String | 是 | 邮箱地址（Query 参数） |

**限流**: 同一邮箱 60 秒内最多 1 次

### 4.3 SSE 推送

```
GET /resource/sse
```

- 协议: Server-Sent Events (`text/event-stream`)
- 认证: 需要有效 JWT Token
- 用途: 服务端向客户端推送实时消息

**消息格式**:

```
event: message
data: {"message": "欢迎登录", "userIds": [1]}
```

### 4.4 SSE 关闭

```
GET /resource/sse/close
```

关闭当前 SSE 连接。

---

## 5. 系统管理 API (`/system/**`)

> 以下接口继承自 RuoYi-Vue-Plus，全部需要认证与权限校验。

### 5.1 用户管理 (`/system/user`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 用户列表 | GET | `/system/user/list` | `system:user:list` |
| 用户详情 | GET | `/system/user/{userId}` | `system:user:query` |
| 新增用户 | POST | `/system/user` | `system:user:add` |
| 修改用户 | PUT | `/system/user` | `system:user:edit` |
| 删除用户 | DELETE | `/system/user/{userIds}` | `system:user:remove` |
| 重置密码 | PUT | `/system/user/resetPwd` | `system:user:resetPwd` |
| 修改状态 | PUT | `/system/user/changeStatus` | `system:user:edit` |
| 个人信息 | GET | `/system/user/profile` | - |
| 修改个人信息 | PUT | `/system/user/profile` | - |
| 修改个人密码 | PUT | `/system/user/profile/updatePwd` | - |
| 上传头像 | POST | `/system/user/profile/avatar` | - |
| 查询授权角色 | GET | `/system/user/authRole/{userId}` | `system:user:query` |
| 保存授权角色 | PUT | `/system/user/authRole` | `system:user:edit` |
| 部门用户列表 | GET | `/system/user/list/dept/{deptId}` | `system:user:list` |
| 部门树 | GET | `/system/user/deptTree` | `system:user:list` |
| 获取用户信息 | GET | `/system/user/getInfo` | - |

**用户查询参数** (`GET /system/user/list`):

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| userName | String | 否 | 用户名（模糊匹配） |
| nickName | String | 否 | 昵称（模糊匹配） |
| phonenumber | String | 否 | 手机号 |
| status | String | 否 | 状态（0=正常，1=停用） |
| deptId | String | 否 | 部门 ID |
| roleId | String | 否 | 角色 ID |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

### 5.2 角色管理 (`/system/role`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 角色列表 | GET | `/system/role/list` | `system:role:list` |
| 角色详情 | GET | `/system/role/{roleId}` | `system:role:query` |
| 新增角色 | POST | `/system/role` | `system:role:add` |
| 修改角色 | PUT | `/system/role` | `system:role:edit` |
| 删除角色 | DELETE | `/system/role/{roleIds}` | `system:role:remove` |
| 数据权限 | PUT | `/system/role/dataScope` | `system:role:edit` |
| 修改状态 | PUT | `/system/role/changeStatus` | `system:role:edit` |
| 已授权用户 | GET | `/system/role/authUser/allocatedList` | `system:role:list` |
| 未授权用户 | GET | `/system/role/authUser/unallocatedList` | `system:role:list` |
| 取消授权 | PUT | `/system/role/authUser/cancel` | `system:role:edit` |
| 批量取消授权 | PUT | `/system/role/authUser/cancelAll` | `system:role:edit` |
| 批量授权 | PUT | `/system/role/authUser/selectAll` | `system:role:edit` |
| 部门树 | GET | `/system/role/deptTree/{roleId}` | `system:role:query` |

### 5.3 菜单管理 (`/system/menu`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 菜单列表 | GET | `/system/menu/list` | `system:menu:list` |
| 菜单详情 | GET | `/system/menu/{menuId}` | `system:menu:query` |
| 菜单树 | GET | `/system/menu/treeselect` | `system:menu:list` |
| 角色菜单树 | GET | `/system/menu/roleMenuTreeselect/{roleId}` | `system:menu:list` |
| 新增菜单 | POST | `/system/menu` | `system:menu:add` |
| 修改菜单 | PUT | `/system/menu` | `system:menu:edit` |
| 删除菜单 | DELETE | `/system/menu/{menuId}` | `system:menu:remove` |
| 获取路由 | GET | `/system/menu/getRouters` | - |

### 5.4 部门管理 (`/system/dept`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 部门列表 | GET | `/system/dept/list` | `system:dept:list` |
| 部门详情 | GET | `/system/dept/{deptId}` | `system:dept:query` |
| 部门树 | GET | `/system/dept/exclude/{deptId}` | `system:dept:list` |
| 新增部门 | POST | `/system/dept` | `system:dept:add` |
| 修改部门 | PUT | `/system/dept` | `system:dept:edit` |
| 删除部门 | DELETE | `/system/dept/{deptId}` | `system:dept:remove` |

### 5.5 岗位管理 (`/system/post`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 岗位列表 | GET | `/system/post/list` | `system:post:list` |
| 岗位详情 | GET | `/system/post/{postId}` | `system:post:query` |
| 新增岗位 | POST | `/system/post` | `system:post:add` |
| 修改岗位 | PUT | `/system/post` | `system:post:edit` |
| 删除岗位 | DELETE | `/system/post/{postIds}` | `system:post:remove` |

### 5.6 字典管理

**字典类型** (`/system/dict/type`):

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 类型列表 | GET | `/system/dict/type/list` | `system:dict:list` |
| 类型详情 | GET | `/system/dict/type/{dictId}` | `system:dict:query` |
| 新增类型 | POST | `/system/dict/type` | `system:dict:add` |
| 修改类型 | PUT | `/system/dict/type` | `system:dict:edit` |
| 删除类型 | DELETE | `/system/dict/type/{dictIds}` | `system:dict:remove` |
| 全部类型 | GET | `/system/dict/type/optionselect` | - |

**字典数据** (`/system/dict/data`):

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 数据列表 | GET | `/system/dict/data/list` | `system:dict:list` |
| 数据详情 | GET | `/system/dict/data/{dictCode}` | `system:dict:query` |
| 按类型查询 | GET | `/system/dict/data/type/{dictType}` | - |
| 新增数据 | POST | `/system/dict/data` | `system:dict:add` |
| 修改数据 | PUT | `/system/dict/data` | `system:dict:edit` |
| 删除数据 | DELETE | `/system/dict/data/{dictCodes}` | `system:dict:remove` |

### 5.7 参数配置 (`/system/config`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 参数列表 | GET | `/system/config/list` | `system:config:list` |
| 参数详情 | GET | `/system/config/{configId}` | `system:config:query` |
| 按 Key 查询 | GET | `/system/config/configKey/{configKey}` | - |
| 新增参数 | POST | `/system/config` | `system:config:add` |
| 修改参数 | PUT | `/system/config` | `system:config:edit` |
| 删除参数 | DELETE | `/system/config/{configIds}` | `system:config:remove` |
| 刷新缓存 | DELETE | `/system/config/refreshCache` | `system:config:remove` |

### 5.8 通知公告 (`/system/notice`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 公告列表 | GET | `/system/notice/list` | `system:notice:list` |
| 公告详情 | GET | `/system/notice/{noticeId}` | `system:notice:query` |
| 新增公告 | POST | `/system/notice` | `system:notice:add` |
| 修改公告 | PUT | `/system/notice` | `system:notice:edit` |
| 删除公告 | DELETE | `/system/notice/{noticeIds}` | `system:notice:remove` |

### 5.9 租户管理 (`/system/tenant`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 租户列表 | GET | `/system/tenant/list` | `system:tenant:list` |
| 租户详情 | GET | `/system/tenant/{id}` | `system:tenant:query` |
| 新增租户 | POST | `/system/tenant` | `system:tenant:add` |
| 修改租户 | PUT | `/system/tenant` | `system:tenant:edit` |
| 删除租户 | DELETE | `/system/tenant/{ids}` | `system:tenant:remove` |
| 动态切换租户 | GET | `/system/tenant/dynamic/{tenantId}` | - |
| 清除动态租户 | GET | `/system/tenant/dynamic` | - |

### 5.10 租户套餐 (`/system/tenantPackage`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 套餐列表 | GET | `/system/tenantPackage/list` | `system:tenantPackage:list` |
| 套餐详情 | GET | `/system/tenantPackage/{id}` | `system:tenantPackage:query` |
| 新增套餐 | POST | `/system/tenantPackage` | `system:tenantPackage:add` |
| 修改套餐 | PUT | `/system/tenantPackage` | `system:tenantPackage:edit` |
| 删除套餐 | DELETE | `/system/tenantPackage/{ids}` | `system:tenantPackage:remove` |

### 5.11 文件管理 (`/system/oss`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 文件列表 | GET | `/system/oss/list` | `system:oss:list` |
| 文件详情 | GET | `/system/oss/{ossId}` | `system:oss:query` |
| 上传文件 | POST | `/system/oss/upload` | `system:oss:upload` |
| 删除文件 | DELETE | `/system/oss/{ossIds}` | `system:oss:remove` |
| 下载文件 | GET | `/system/oss/download/{ossId}` | `system:oss:query` |

### 5.12 客户端管理 (`/system/client`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 客户端列表 | GET | `/system/client/list` | `system:client:list` |
| 客户端详情 | GET | `/system/client/{id}` | `system:client:query` |
| 新增客户端 | POST | `/system/client` | `system:client:add` |
| 修改客户端 | PUT | `/system/client` | `system:client:edit` |
| 删除客户端 | DELETE | `/system/client/{ids}` | `system:client:remove` |
| 状态修改 | PUT | `/system/client/changeStatus` | `system:client:edit` |

### 5.13 OSS 配置 (`/system/ossConfig`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 配置列表 | GET | `/system/ossConfig/list` | `system:ossConfig:list` |
| 配置详情 | GET | `/system/ossConfig/{id}` | `system:ossConfig:query` |
| 新增配置 | POST | `/system/ossConfig` | `system:ossConfig:add` |
| 修改配置 | PUT | `/system/ossConfig` | `system:ossConfig:edit` |
| 删除配置 | DELETE | `/system/ossConfig/{ids}` | `system:ossConfig:remove` |

---

## 6. 系统监控 API (`/monitor/**`)

### 6.1 在线用户 (`/monitor/online`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 在线用户列表 | GET | `/monitor/online/list` | `monitor:online:list` |
| 强制踢出 | DELETE | `/monitor/online/{tokenId}` | `monitor:online:forceLogout` |

### 6.2 登录日志 (`/monitor/logininfor`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 日志列表 | GET | `/monitor/logininfor/list` | `monitor:logininfor:list` |
| 删除日志 | DELETE | `/monitor/logininfor/{infoIds}` | `monitor:logininfor:remove` |
| 清空日志 | DELETE | `/monitor/logininfor/clean` | `monitor:logininfor:remove` |
| 解锁账户 | PUT | `/monitor/logininfor/unlock/{userName}` | `monitor:logininfor:unlock` |

### 6.3 操作日志 (`/monitor/operlog`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 日志列表 | GET | `/monitor/operlog/list` | `monitor:operlog:list` |
| 删除日志 | DELETE | `/monitor/operlog/{operIds}` | `monitor:operlog:remove` |
| 清空日志 | DELETE | `/monitor/operlog/clean` | `monitor:operlog:remove` |

### 6.4 缓存监控 (`/monitor/cache`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 缓存信息 | GET | `/monitor/cache/getNames` | `monitor:cache:list` |
| 缓存键列表 | GET | `/monitor/cache/getKeys/{cacheName}` | `monitor:cache:list` |
| 缓存值 | GET | `/monitor/cache/getValue/{cacheName}/{cacheKey}` | `monitor:cache:list` |
| 清理缓存 | DELETE | `/monitor/cache/clearCacheName/{cacheName}` | `monitor:cache:remove` |
| 清理全部 | DELETE | `/monitor/cache/clearCacheAll` | `monitor:cache:remove` |

---

## 7. 工具模块 API (`/tool/**`)

### 7.1 代码生成 (`/tool/gen`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 表列表 | GET | `/tool/gen/list` | `tool:gen:list` |
| 表详情 | GET | `/tool/gen/{tableId}` | `tool:gen:query` |
| 导入表 | POST | `/tool/gen/importTable` | `tool:gen:import` |
| 修改生成配置 | PUT | `/tool/gen` | `tool:gen:edit` |
| 预览代码 | GET | `/tool/gen/preview/{tableId}` | `tool:gen:preview` |
| 生成代码 | GET | `/tool/gen/batchGenCode` | `tool:gen:code` |
| 删除表配置 | DELETE | `/tool/gen/{tableIds}` | `tool:gen:remove` |
| 同步表 | GET | `/tool/gen/syncDb/{tableName}` | `tool:gen:edit` |

---

## 8. 工作流 API (`/workflow/**`)

> 基于 Warm-Flow 工作流引擎。

### 8.1 流程分类 (`/workflow/category`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 分类列表 | GET | `/workflow/category/list` | `workflow:category:list` |
| 分类详情 | GET | `/workflow/category/{id}` | `workflow:category:query` |
| 新增分类 | POST | `/workflow/category` | `workflow:category:add` |
| 修改分类 | PUT | `/workflow/category` | `workflow:category:edit` |
| 删除分类 | DELETE | `/workflow/category/{ids}` | `workflow:category:remove` |

### 8.2 流程定义 (`/workflow/definition`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 定义列表 | GET | `/workflow/definition/list` | `workflow:definition:list` |
| 定义详情 | GET | `/workflow/definition/{id}` | `workflow:definition:query` |
| 保存定义 | POST | `/workflow/definition/save` | `workflow:definition:add` |
| 发布定义 | POST | `/workflow/definition/publish` | `workflow:definition:publish` |
| 删除定义 | DELETE | `/workflow/definition/{ids}` | `workflow:definition:remove` |

### 8.3 流程实例 (`/workflow/instance`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 实例列表 | GET | `/workflow/instance/list` | `workflow:instance:list` |
| 启动实例 | POST | `/workflow/instance/start` | `workflow:instance:start` |
| 删除实例 | DELETE | `/workflow/instance/{ids}` | `workflow:instance:remove` |

### 8.4 任务管理 (`/workflow/task`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 待办任务 | GET | `/workflow/task/todoList` | `workflow:task:list` |
| 已办任务 | GET | `/workflow/task/doneList` | `workflow:task:list` |
| 完成任务 | POST | `/workflow/task/complete` | `workflow:task:complete` |
| 退回任务 | POST | `/workflow/task/return` | `workflow:task:return` |
| 转交任务 | POST | `/workflow/task/transfer` | `workflow:task:transfer` |
| 撤回任务 | POST | `/workflow/task/revoke` | `workflow:task:revoke` |

### 8.5 SpEL 表达式 (`/workflow/spel`)

| 操作 | 方法 | 路径 | 权限标识 |
|------|------|------|----------|
| 表达式列表 | GET | `/workflow/spel/list` | `workflow:spel:list` |
| 表达式详情 | GET | `/workflow/spel/{id}` | `workflow:spel:query` |
| 新增表达式 | POST | `/workflow/spel` | `workflow:spel:add` |
| 修改表达式 | PUT | `/workflow/spel` | `workflow:spel:edit` |
| 删除表达式 | DELETE | `/workflow/spel/{ids}` | `workflow:spel:remove` |

---

## 9. Magic-API 动态接口

### 9.1 概述

Magic-API 提供可视化 API 编辑器，支持在线编写、调试和发布 REST API。

| 项目 | 值 |
|------|-----|
| 编辑器入口 | `GET /magic/web` |
| 动态 API 前缀 | `/api/{group}/{name}`（可配置） |
| WebSocket 调试 | `ws://{host}/magic/web/console` |
| 脚本语言 | JavaScript / Java |
| 热加载 | 保存即生效，无需重启 |

### 9.2 动态接口调用

```
{METHOD} /api/{group}/{name}
```

| 参数 | 说明 |
|------|------|
| group | 接口分组（模块） |
| name | 接口名称 |
| METHOD | GET / POST / PUT / DELETE（在脚本中定义） |

**请求参数**: 由脚本定义，支持 Query 参数、Path 参数、Request Body

**响应格式**: 遵循统一响应格式 `R<T>`

### 9.3 动态接口权限

- 动态接口默认需要认证
- 权限通过 magic-api 编辑器配置
- 不得开放匿名访问
- 建议在脚本中使用 `@SaCheckPermission` 进行权限校验

### 9.4 编辑器功能

| 功能 | 说明 |
|------|------|
| API 脚本编辑 | 在线编写 JavaScript/Java 脚本 |
| 调试 | 内置调试面板，支持参数输入与结果查看 |
| 数据源管理 | 配置多数据源连接 |
| 函数管理 | 自定义可复用函数 |
| 模块管理 | API 分组与版本管理 |
| 备份恢复 | API 脚本备份与恢复 |

### 9.5 动态接口示例

**示例: 查询用户列表**

```
GET /api/system/userList?pageNum=1&pageSize=10
```

脚本内容:
```javascript
return db.select("select * from sys_user where del_flag = '0' limit #{pageNum}, #{pageSize}");
```

**示例: 创建订单**

```
POST /api/order/create
```

请求体:
```json
{
  "userId": 1,
  "productId": 100,
  "quantity": 2
}
```

---

## 10. 前端 API 客户端

### 10.1 请求封装

前端使用 Axios 封装（`@/utils/request`），统一处理:

- Token 注入（`Authorization` 请求头）
- 租户 ID 注入（`Tenant-Id` 请求头）
- 请求/响应加密解密
- 错误处理与重试
- 重复提交检测

### 10.2 请求配置项

| 配置项 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| isToken | Boolean | true | 是否需要 Token |
| isEncrypt | Boolean | false | 是否加密请求 |
| repeatSubmit | Boolean | true | 是否防重复提交 |

### 10.3 API 模块组织

```
src/api/
├── login.ts              # 认证接口
├── menu.ts               # 菜单/路由接口
├── types.ts              # 通用类型定义
├── system/               # 系统管理
│   ├── user/             # 用户
│   ├── role/             # 角色
│   ├── menu/             # 菜单
│   ├── dept/             # 部门
│   ├── post/             # 岗位
│   ├── dict/             # 字典
│   ├── config/           # 参数配置
│   ├── notice/           # 通知公告
│   ├── tenant/           # 租户
│   ├── tenantPackage/    # 租户套餐
│   ├── oss/              # 文件管理
│   ├── ossConfig/        # OSS 配置
│   ├── client/           # 客户端管理
│   └── social/           # 社交登录
├── monitor/              # 系统监控
│   ├── online/           # 在线用户
│   ├── loginInfo/        # 登录日志
│   ├── operlog/          # 操作日志
│   └── cache/            # 缓存监控
├── tool/                 # 工具模块
│   └── gen/              # 代码生成
├── workflow/             # 工作流
│   ├── category/         # 流程分类
│   ├── definition/       # 流程定义
│   ├── instance/         # 流程实例
│   ├── task/             # 任务管理
│   ├── leave/            # 请假示例
│   └── spel/             # SpEL 表达式
└── demo/                 # 示例模块
    ├── demo/             # 基础示例
    └── tree/             # 树形示例
```

---

## 11. 安全边界

### 11.1 租户隔离

所有业务表默认启用租户隔离，通过 MyBatis-Plus 租户插件自动过滤。

**排除租户过滤的表**:

```
sys_menu
sys_tenant
sys_tenant_package
sys_role_dept
sys_role_menu
sys_user_post
sys_user_role
sys_client
sys_oss_config
flow_spel
```

### 11.2 XSS 防护

- 全局启用 XSS 过滤
- 排除路径: `/system/notice`（富文本内容）
- 用户输入在前端展示时必须转义

### 11.3 限流策略

| 接口 | 限流规则 |
|------|----------|
| 登录接口 | IP 级别，60 秒内最多 20 次 |
| 租户列表 | IP 级别，60 秒内最多 20 次 |
| 短信验证码 | 手机号级别，60 秒内最多 1 次 |
| 邮箱验证码 | 邮箱级别，60 秒内最多 1 次 |
| 图形验证码 | IP 级别，60 秒内最多 10 次 |

### 11.4 防重复提交

- 写操作接口使用 `@RepeatSubmit` 注解
- 基于 Token + 时间窗口实现
- 默认 3 秒内同一请求只处理一次

---

## 12. 附录

### 12.1 常用枚举值

**用户状态**:

| 值 | 含义 |
|----|------|
| 0 | 正常 |
| 1 | 停用 |

**菜单类型**:

| 值 | 含义 |
|----|------|
| M | 目录 |
| C | 菜单 |
| F | 按钮 |

**数据范围**:

| 值 | 含义 |
|----|------|
| 1 | 全部数据权限 |
| 2 | 自定义数据权限 |
| 3 | 本部门数据权限 |
| 4 | 本部门及以下数据权限 |
| 5 | 仅本人数据权限 |

### 12.2 主键策略

- 类型: `ASSIGN_ID`（雪花算法）
- 格式: Long 类型数字 ID
- 示例: `1750000000000000001`

### 12.3 日期格式

- 统一格式: `yyyy-MM-dd HH:mm:ss`
- 示例: `2026-04-28 10:30:00`
