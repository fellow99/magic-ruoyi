# 系统监控模块 API 文档（013-monitor/api.md）

> magic-ruoyi 系统监控模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 在线用户前缀 | `/monitor/online` |
| 登录日志前缀 | `/monitor/logininfor` |
| 操作日志前缀 | `/monitor/operlog` |
| 缓存监控前缀 | `/monitor/cache` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 在线用户接口

### 2.1 查询在线用户列表

```
GET /monitor/online/list
```

**认证**: 需要 Token
**权限**: `monitor:online:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| ipaddr | String | 否 | 登录 IP |
| userName | String | 否 | 用户名 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "tokenId": "abc123...",
      "deptName": "总公司",
      "userName": "admin",
      "ipaddr": "127.0.0.1",
      "loginLocation": "内网IP",
      "browser": "Chrome 120",
      "os": "Windows 10",
      "loginTime": 1704067200000
    }
  ],
  "total": 1
}
```

---

### 2.2 强制踢出用户

```
DELETE /monitor/online/{tokenId}
```

**认证**: 需要 Token
**权限**: `monitor:online:forceLogout`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tokenId | String | 是 | 会话 Token ID |

---

### 2.3 获取当前用户在线设备

```
GET /monitor/online
```

**认证**: 需要 Token

---

### 2.4 删除当前用户在线设备

```
DELETE /monitor/online/myself/{tokenId}
```

**认证**: 需要 Token

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tokenId | String | 是 | 会话 Token ID |

---

## 3. 登录日志接口

### 3.1 查询登录日志列表

```
GET /monitor/logininfor/list
```

**认证**: 需要 Token
**权限**: `monitor:logininfor:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| ipaddr | String | 否 | 登录 IP |
| userName | String | 否 | 用户名 |
| status | String | 否 | 登录状态 |
| orderByColumn | String | 否 | 排序字段 |
| isAsc | String | 否 | 排序方向 |

---

### 3.2 删除登录日志

```
DELETE /monitor/logininfor/{infoIds}
```

**认证**: 需要 Token
**权限**: `monitor:logininfor:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| infoIds | String | 是 | 日志 ID，多个用逗号分隔 |

---

### 3.3 解锁用户登录状态

```
GET /monitor/logininfor/unlock/{userNames}
```

**认证**: 需要 Token
**权限**: `monitor:logininfor:unlock`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| userNames | String | 是 | 用户名，多个用逗号分隔 |

---

### 3.4 清空登录日志

```
DELETE /monitor/logininfor/clean
```

**认证**: 需要 Token
**权限**: `monitor:logininfor:remove`

---

## 4. 操作日志接口

### 4.1 查询操作日志列表

```
GET /monitor/operlog/list
```

**认证**: 需要 Token
**权限**: `monitor:operlog:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| operIp | String | 否 | 操作 IP |
| title | String | 否 | 模块名称 |
| operName | String | 否 | 操作人 |
| businessType | String | 否 | 业务类型 |
| status | String | 否 | 操作状态 |
| orderByColumn | String | 否 | 排序字段 |
| isAsc | String | 否 | 排序方向 |

---

### 4.2 删除操作日志

```
DELETE /monitor/operlog/{operIds}
```

**认证**: 需要 Token
**权限**: `monitor:operlog:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| operIds | String | 是 | 日志 ID，多个用逗号分隔 |

---

### 4.3 清空操作日志

```
DELETE /monitor/operlog/clean
```

**认证**: 需要 Token
**权限**: `monitor:operlog:remove`

---

## 5. 缓存监控接口

### 5.1 查询缓存详情

```
GET /monitor/cache
```

**认证**: 需要 Token
**权限**: `monitor:cache:list`

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "commandStats": [
      { "name": "get", "value": "1000" },
      { "name": "set", "value": "500" }
    ],
    "dbSize": 1500,
    "info": {
      "redis_version": "7.0.0",
      "uptime_in_days": "30",
      "used_memory_human": "50M"
    }
  }
}
```

---

### 5.2 查询缓存名称列表

```
GET /monitor/cache/getNames
```

**认证**: 需要 Token

---

### 5.3 查询缓存键名列表

```
GET /monitor/cache/getKeys/{cacheName}
```

**认证**: 需要 Token

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| cacheName | String | 是 | 缓存名称（前缀） |

---

### 5.4 查询缓存内容

```
GET /monitor/cache/getValue/{cacheName}/{cacheKey}
```

**认证**: 需要 Token

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| cacheName | String | 是 | 缓存名称 |
| cacheKey | String | 是 | 缓存键名 |

---

### 5.5 清理指定名称缓存

```
DELETE /monitor/cache/clearCacheName/{cacheName}
```

**认证**: 需要 Token

---

### 5.6 清理指定键名缓存

```
DELETE /monitor/cache/clearCacheKey/{cacheName}/{cacheKey}
```

**认证**: 需要 Token

---

### 5.7 清理全部缓存

```
DELETE /monitor/cache/clearCacheAll
```

**认证**: 需要 Token

---

## 6. 前端 API 封装

### 6.1 在线用户 API（`@/api/monitor/online/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `list(query)` | GET | `/monitor/online/list` | 查询在线用户列表 |
| `forceLogout(tokenId)` | DELETE | `/monitor/online/{tokenId}` | 强制踢出 |
| `getOnline()` | GET | `/monitor/online` | 获取当前用户在线设备 |
| `delOnline(tokenId)` | DELETE | `/monitor/online/myself/{tokenId}` | 删除当前用户设备 |

### 6.2 登录日志 API（`@/api/monitor/loginInfo/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `list(query)` | GET | `/monitor/logininfor/list` | 查询登录日志列表 |
| `delLoginInfo(infoIds)` | DELETE | `/monitor/logininfor/{infoIds}` | 删除登录日志 |
| `unlockLoginInfo(userName)` | GET | `/monitor/logininfor/unlock/{userName}` | 解锁用户 |
| `cleanLoginInfo()` | DELETE | `/monitor/logininfor/clean` | 清空登录日志 |

### 6.3 操作日志 API（`@/api/monitor/operlog/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `list(query)` | GET | `/monitor/operlog/list` | 查询操作日志列表 |
| `delOperlog(operIds)` | DELETE | `/monitor/operlog/{operIds}` | 删除操作日志 |
| `cleanOperlog()` | DELETE | `/monitor/operlog/clean` | 清空操作日志 |

### 6.4 缓存监控 API（`@/api/monitor/cache/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `getCache()` | GET | `/monitor/cache` | 查询缓存详情 |
| `listCacheName()` | GET | `/monitor/cache/getNames` | 缓存名称列表 |
| `listCacheKey(cacheName)` | GET | `/monitor/cache/getKeys/{cacheName}` | 缓存键名列表 |
| `getCacheValue(cacheName, cacheKey)` | GET | `/monitor/cache/getValue/{cacheName}/{cacheKey}` | 缓存内容 |
| `clearCacheName(cacheName)` | DELETE | `/monitor/cache/clearCacheName/{cacheName}` | 清理名称缓存 |
| `clearCacheKey(cacheName, cacheKey)` | DELETE | `/monitor/cache/clearCacheKey/{cacheName}/{cacheKey}` | 清理键名缓存 |
| `clearCacheAll()` | DELETE | `/monitor/cache/clearCacheAll` | 清理全部缓存 |

### 6.5 类型定义

**在线用户（`@/api/monitor/online/types.ts`）**:

```typescript
export interface OnlineVO extends BaseEntity {
  tokenId: string;
  deptName: string;
  userName: string;
  ipaddr: string;
  loginLocation: string;
  browser: string;
  os: string;
  loginTime: number;
}

export interface OnlineQuery extends PageQuery {
  ipaddr: string;
  userName: string;
}
```

**登录日志（`@/api/monitor/loginInfo/types.ts`）**:

```typescript
export interface LoginInfoVO {
  infoId: string | number;
  tenantId: string | number;
  userName: string;
  status: string;
  ipaddr: string;
  loginLocation: string;
  browser: string;
  os: string;
  msg: string;
  loginTime: string;
}

export interface LoginInfoQuery extends PageQuery {
  ipaddr: string;
  userName: string;
  status: string;
  orderByColumn: string;
  isAsc: string;
}
```

**操作日志（`@/api/monitor/operlog/types.ts`）**:

```typescript
export interface OperLogVO extends BaseEntity {
  operId: string | number;
  tenantId: string;
  title: string;
  businessType: number;
  businessTypes: number[] | undefined;
  method: string;
  requestMethod: string;
  operatorType: number;
  operName: string;
  deptName: string;
  operUrl: string;
  operIp: string;
  operLocation: string;
  operParam: string;
  jsonResult: string;
  status: number;
  errorMsg: string;
  operTime: string;
  costTime: number;
}

export interface OperLogQuery extends PageQuery {
  operIp: string;
  title: string;
  operName: string;
  businessType: string;
  status: string;
  orderByColumn: string;
  isAsc: string;
}
```

**缓存监控（`@/api/monitor/cache/types.ts`）**:

```typescript
export interface CacheVO {
  commandStats: Array<{ name: string; value: string }>;
  dbSize: number;
  info: { [key: string]: string };
}
```

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
