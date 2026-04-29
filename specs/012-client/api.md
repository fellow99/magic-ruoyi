# 客户端管理模块 API 文档（012-client/api.md）

> magic-ruoyi 客户端管理模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 模块前缀 | `/system/client` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 客户端接口

### 2.1 查询客户端列表

```
GET /system/client/list
```

**认证**: 需要 Token
**权限**: `system:client:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| clientId | String | 否 | 客户端 ID |
| clientKey | String | 否 | 客户端 Key |
| clientSecret | String | 否 | 客户端秘钥 |
| grantType | String | 否 | 授权类型 |
| deviceType | String | 否 | 设备类型 |
| activeTimeout | Long | 否 | Token 活跃超时 |
| timeout | Long | 否 | Token 固定超时 |
| status | String | 否 | 状态 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,
      "clientId": "e5cd7e4891bf95d1d19206ce24a7b32e",
      "clientKey": "e5cd7e4891bf95d1d19206ce24a7b32e",
      "clientSecret": "b9568956e5cd7e4891bf95d1d19206ce",
      "grantTypeList": ["password", "sms", "email"],
      "deviceType": "pc",
      "activeTimeout": 1800,
      "timeout": 604800,
      "status": "0"
    }
  ],
  "total": 1
}
```

---

### 2.2 查询客户端详情

```
GET /system/client/{id}
```

**认证**: 需要 Token
**权限**: `system:client:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| id | Long | 是 | 客户端 ID |

---

### 2.3 新增客户端

```
POST /system/client
```

**认证**: 需要 Token
**权限**: `system:client:add`

**请求体**:

```json
{
  "clientKey": "my-app",
  "clientSecret": "secret123",
  "grantTypeList": ["password", "sms"],
  "deviceType": "pc",
  "activeTimeout": 1800,
  "timeout": 604800,
  "status": "0"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| clientKey | String | 是 | 客户端 Key |
| clientSecret | String | 是 | 客户端秘钥 |
| grantTypeList | Array\<String\> | 是 | 授权类型列表 |
| deviceType | String | 是 | 设备类型 |
| activeTimeout | Long | 是 | Token 活跃超时（秒） |
| timeout | Long | 是 | Token 固定超时（秒） |
| status | String | 否 | 状态，默认 "0" |

---

### 2.4 修改客户端

```
PUT /system/client
```

**认证**: 需要 Token
**权限**: `system:client:edit`

**请求体**:

```json
{
  "id": 1,
  "clientId": "e5cd7e4891bf95d1d19206ce24a7b32e",
  "clientKey": "my-app",
  "clientSecret": "secret123",
  "grantTypeList": ["password", "sms", "email"],
  "deviceType": "pc",
  "activeTimeout": 3600,
  "timeout": 604800,
  "status": "0"
}
```

---

### 2.5 删除客户端

```
DELETE /system/client/{ids}
```

**认证**: 需要 Token
**权限**: `system:client:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| ids | String | 是 | 客户端 ID，多个用逗号分隔 |

---

### 2.6 切换客户端状态

```
PUT /system/client/changeStatus
```

**认证**: 需要 Token

**请求体**:

```json
{
  "clientId": "e5cd7e4891bf95d1d19206ce24a7b32e",
  "status": "1"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| clientId | String | 是 | 客户端 ID |
| status | String | 是 | 新状态（0=正常, 1=停用） |

---

## 3. 前端 API 封装

### 3.1 `@/api/system/client/index.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listClient(query)` | GET | `/system/client/list` | 查询客户端列表 |
| `getClient(id)` | GET | `/system/client/{id}` | 查询客户端详情 |
| `addClient(data)` | POST | `/system/client` | 新增客户端 |
| `updateClient(data)` | PUT | `/system/client` | 修改客户端 |
| `delClient(ids)` | DELETE | `/system/client/{ids}` | 删除客户端 |
| `changeStatus(clientId, status)` | PUT | `/system/client/changeStatus` | 切换状态 |

### 3.2 类型定义（`@/api/system/client/types.ts`）

```typescript
export interface ClientVO {
  id: string | number;
  clientId: string;
  clientKey: string;
  clientSecret: string;
  grantTypeList: string[];
  deviceType: string;
  activeTimeout: number;
  timeout: number;
  status: string;
}

export interface ClientForm extends BaseEntity {
  id?: string | number;
  clientId?: string | number;
  clientKey?: string;
  clientSecret?: string;
  grantTypeList?: string[];
  deviceType?: string;
  activeTimeout?: number;
  timeout?: number;
  status?: string;
}

export interface ClientQuery extends PageQuery {
  clientId?: string | number;
  clientKey?: string;
  clientSecret?: string;
  grantType?: string;
  deviceType?: string;
  activeTimeout?: number;
  timeout?: number;
  status?: string;
}
```

---

## 4. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
