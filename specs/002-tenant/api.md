# 租户管理模块 API 接口文档

> magic-ruoyi 租户管理模块的 REST API 接口定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 模块前缀 | `/system/tenant` |
| 认证方式 | Sa-Token + JWT（Bearer Token） |
| 请求头 | `Authorization: Bearer {token}` |
| 租户头 | `Tenant-Id: {tenant_id}`（默认 `000000`） |
| 响应格式 | `R<T>` 统一响应体 |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 1.3 分页响应格式

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [],
  "total": 100
}
```

---

## 2. 租户管理 API

### 2.1 分页查询租户列表

```
GET /system/tenant/list
```

**权限标识**: `system:tenant:list`

**请求参数**（Query）:

| 参数 | 类型 | 必填 | 默认值 | 描述 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |
| tenantId | String | 否 | - | 租户编号（精确匹配） |
| contactUserName | String | 否 | - | 联系人（模糊匹配） |
| contactPhone | String | 否 | - | 联系电话（模糊匹配） |
| companyName | String | 否 | - | 企业名称（模糊匹配） |

**响应示例**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,
      "tenantId": "000000",
      "contactUserName": "主户",
      "contactPhone": "",
      "companyName": "主户",
      "licenseNumber": null,
      "address": null,
      "intro": "后台管理系统",
      "domain": null,
      "remark": null,
      "packageId": null,
      "expireTime": null,
      "accountCount": -1,
      "status": "0"
    }
  ],
  "total": 1
}
```

---

### 2.2 获取租户详情

```
GET /system/tenant/{id}
```

**权限标识**: `system:tenant:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| id | Long | 是 | 租户主键 ID |

**响应**: 返回 `TenantVO` 对象，结构同列表项。

---

### 2.3 新增租户

```
POST /system/tenant
```

**权限标识**: `system:tenant:add`

**请求头**:

| 请求头 | 值 | 说明 |
|--------|-----|------|
| isEncrypt | true | RSA 加密传输 |
| repeatSubmit | false | 防重复提交 |

**请求体**:

```json
{
  "username": "admin",
  "password": "admin123",
  "contactUserName": "张三",
  "contactPhone": "13800138000",
  "companyName": "示例企业",
  "licenseNumber": "91110000MA001XXXXX",
  "domain": "example.com",
  "address": "北京市朝阳区",
  "intro": "企业简介",
  "remark": "备注信息",
  "packageId": 1,
  "expireTime": "2027-12-31 23:59:59",
  "accountCount": 50,
  "status": "0"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| username | String | 是 | 租户管理员用户名（仅新增时需要） |
| password | String | 是 | 租户管理员密码（仅新增时需要） |
| contactUserName | String | 是 | 联系人姓名 |
| contactPhone | String | 是 | 联系电话 |
| companyName | String | 是 | 企业名称 |
| licenseNumber | String | 否 | 统一社会信用代码 |
| domain | String | 否 | 绑定域名 |
| address | String | 否 | 企业地址 |
| intro | String | 否 | 企业简介 |
| remark | String | 否 | 备注 |
| packageId | Long | 否 | 租户套餐 ID |
| expireTime | String | 否 | 过期时间（格式：yyyy-MM-dd HH:mm:ss） |
| accountCount | Integer | 否 | 用户数量上限（-1 不限制） |
| status | String | 否 | 状态（0=正常，1=停用），默认 0 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 2.4 修改租户

```
PUT /system/tenant
```

**权限标识**: `system:tenant:edit`

**请求体**: 同新增，需包含 `id` 字段。`username` 和 `password` 字段在修改时忽略。

**响应**: 同新增。

---

### 2.5 删除租户

```
DELETE /system/tenant/{ids}
```

**权限标识**: `system:tenant:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| ids | String | 是 | 租户 ID 列表，逗号分隔，如 `1,2,3` |

**响应**:

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

### 2.6 修改租户状态

```
PUT /system/tenant/changeStatus
```

**权限标识**: `system:tenant:edit`

**请求体**:

```json
{
  "id": 1,
  "tenantId": "000000",
  "status": "1"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| id | Long | 是 | 租户主键 ID |
| tenantId | String | 是 | 租户编号 |
| status | String | 是 | 目标状态（0=正常，1=停用） |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 2.7 动态切换租户

```
GET /system/tenant/dynamic/{tenantId}
```

**权限标识**: 无（需登录）

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tenantId | String | 是 | 目标租户编号 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 2.8 清除动态租户

```
GET /system/tenant/dynamic/clear
```

**权限标识**: 无（需登录）

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 2.9 同步租户套餐

```
GET /system/tenant/syncTenantPackage
```

**权限标识**: `system:tenant:edit`

**请求参数**（Query）:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tenantId | String | 是 | 租户编号 |
| packageId | Long | 是 | 套餐 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "同步成功",
  "data": null
}
```

---

### 2.10 同步租户字典

```
GET /system/tenant/syncTenantDict
```

**权限标识**: 仅超级管理员（userId = 1）

**请求参数**: 无

**响应**:

```json
{
  "code": 200,
  "msg": "同步成功",
  "data": null
}
```

---

### 2.11 同步租户参数配置

```
GET /system/tenant/syncTenantConfig
```

**权限标识**: 仅超级管理员（userId = 1）

**请求参数**: 无

**响应**:

```json
{
  "code": 200,
  "msg": "同步成功",
  "data": null
}
```

---

### 2.12 导出租户数据

```
POST /system/tenant/export
```

**权限标识**: `system:tenant:export`

**请求参数**（Query）: 同列表查询参数

**响应**: Excel 文件下载（`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`）

---

## 3. 租户套餐 API

### 3.1 分页查询套餐列表

```
GET /system/tenant/package/list
```

**权限标识**: `system:tenantPackage:list`

**请求参数**（Query）:

| 参数 | 类型 | 必填 | 默认值 | 描述 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |
| packageName | String | 否 | - | 套餐名称（模糊匹配） |

**响应示例**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "packageId": 1,
      "packageName": "基础版",
      "menuIds": "1,2,3,4,5",
      "remark": "基础功能套餐",
      "menuCheckStrictly": true,
      "status": "0"
    }
  ],
  "total": 1
}
```

---

### 3.2 查询套餐下拉选列表

```
GET /system/tenant/package/selectList
```

**权限标识**: 无（需登录）

**请求参数**: 无

**响应**: 返回所有正常状态的套餐列表，用于下拉选择。

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "packageId": 1,
      "packageName": "基础版",
      "status": "0"
    }
  ]
}
```

---

### 3.3 获取套餐详情

```
GET /system/tenant/package/{packageId}
```

**权限标识**: `system:tenantPackage:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| packageId | Long | 是 | 套餐主键 ID |

**响应**: 返回 `TenantPkgVO` 对象，结构同列表项。

---

### 3.4 新增套餐

```
POST /system/tenant/package
```

**权限标识**: `system:tenantPackage:add`

**请求体**:

```json
{
  "packageName": "高级版",
  "menuIds": "1,2,3,4,5,6,7,8",
  "menuCheckStrictly": true,
  "remark": "高级功能套餐"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| packageName | String | 是 | 套餐名称 |
| menuIds | String/Array | 否 | 关联菜单 ID 列表（逗号分隔或数组） |
| menuCheckStrictly | Boolean | 否 | 菜单树父子联动开关，默认 true |
| remark | String | 否 | 备注 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 3.5 修改套餐

```
PUT /system/tenant/package
```

**权限标识**: `system:tenantPackage:edit`

**请求体**: 同新增，需包含 `packageId` 字段。

**响应**: 同新增。

---

### 3.6 删除套餐

```
DELETE /system/tenant/package/{packageIds}
```

**权限标识**: `system:tenantPackage:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| packageIds | String | 是 | 套餐 ID 列表，逗号分隔，如 `1,2,3` |

**响应**:

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

### 3.7 修改套餐状态

```
PUT /system/tenant/package/changeStatus
```

**权限标识**: `system:tenantPackage:edit`

**请求体**:

```json
{
  "packageId": 1,
  "status": "1"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| packageId | Long | 是 | 套餐主键 ID |
| status | String | 是 | 目标状态（0=正常，1=停用） |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 3.8 查询套餐菜单树

```
GET /system/menu/tenantPackageMenuTreeselect/{packageId}
```

**权限标识**: `system:menu:list`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| packageId | Long | 是 | 套餐 ID，传 0 返回完整菜单树 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "menus": [
      {
        "id": 1,
        "label": "系统管理",
        "children": [...]
      }
    ],
    "checkedKeys": [1, 2, 3]
  }
}
```

---

### 3.9 导出套餐数据

```
POST /system/tenant/package/export
```

**权限标识**: `system:tenantPackage:export`

**请求参数**（Query）: 同套餐列表查询参数

**响应**: Excel 文件下载

---

## 4. 前端 API 客户端

### 4.1 租户 API 客户端

文件: `src/api/system/tenant/index.ts`

| 函数名 | 方法 | 路径 | 说明 |
|--------|------|------|------|
| `listTenant` | GET | `/system/tenant/list` | 查询租户列表 |
| `getTenant` | GET | `/system/tenant/{id}` | 查询租户详情 |
| `addTenant` | POST | `/system/tenant` | 新增租户 |
| `updateTenant` | PUT | `/system/tenant` | 修改租户 |
| `delTenant` | DELETE | `/system/tenant/{ids}` | 删除租户 |
| `changeTenantStatus` | PUT | `/system/tenant/changeStatus` | 修改租户状态 |
| `dynamicTenant` | GET | `/system/tenant/dynamic/{tenantId}` | 动态切换租户 |
| `dynamicClear` | GET | `/system/tenant/dynamic/clear` | 清除动态租户 |
| `syncTenantPackage` | GET | `/system/tenant/syncTenantPackage` | 同步租户套餐 |
| `syncTenantDict` | GET | `/system/tenant/syncTenantDict` | 同步租户字典 |
| `syncTenantConfig` | GET | `/system/tenant/syncTenantConfig` | 同步租户参数 |

### 4.2 租户套餐 API 客户端

文件: `src/api/system/tenantPackage/index.ts`

| 函数名 | 方法 | 路径 | 说明 |
|--------|------|------|------|
| `listTenantPackage` | GET | `/system/tenant/package/list` | 查询套餐列表 |
| `selectTenantPackage` | GET | `/system/tenant/package/selectList` | 查询套餐下拉选 |
| `getTenantPackage` | GET | `/system/tenant/package/{packageId}` | 查询套餐详情 |
| `addTenantPackage` | POST | `/system/tenant/package` | 新增套餐 |
| `updateTenantPackage` | PUT | `/system/tenant/package` | 修改套餐 |
| `delTenantPackage` | DELETE | `/system/tenant/package/{packageIds}` | 删除套餐 |
| `changePackageStatus` | PUT | `/system/tenant/package/changeStatus` | 修改套餐状态 |

---

## 5. 错误码

| 错误码 | 含义 | 处理建议 |
|--------|------|----------|
| 200 | 请求成功 | 正常处理 |
| 401 | 未认证 / Token 失效 | 跳转登录页 |
| 403 | 无权限 | 提示权限不足 |
| 404 | 资源不存在 | 提示未找到 |
| 500 | 服务端错误 | 提示系统异常 |

**业务错误码**:

| 错误码 | 含义 |
|--------|------|
| 租户已停用 | 该租户已被停用，无法操作 |
| 租户已过期 | 该租户已过期，请联系管理员 |
| 用户数量超出限制 | 租户用户数已达上限 |
| 套餐不存在 | 指定的套餐 ID 不存在 |

---

## 6. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，租户管理模块 API 定义 |
