# 角色管理API接口文档 (api.md)

> magic-ruoyi 角色管理模块的API接口定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 模块路径 | `/system/role` |
| 认证方式 | Sa-Token + JWT |
| 请求头 | `Authorization: Bearer {token}` |
| 租户头 | `Tenant-Id: {tenantId}` |

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
  "data": {
    "rows": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 2. 角色CRUD接口

### 2.1 分页查询角色列表

```
GET /system/role/list
```

**权限标识**: `system:role:list`

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| roleName | String | 否 | Query | 角色名称（模糊匹配） |
| roleKey | String | 否 | Query | 权限字符（模糊匹配） |
| status | String | 否 | Query | 状态（0=正常，1=停用） |
| beginTime | String | 否 | Query | 开始时间（格式：yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | Query | 结束时间（格式：yyyy-MM-dd HH:mm:ss） |
| pageNum | Integer | 否 | Query | 页码，默认1 |
| pageSize | Integer | 否 | Query | 每页条数，默认10 |

**响应示例**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "rows": [
      {
        "roleId": 1,
        "roleName": "超级管理员",
        "roleKey": "superadmin",
        "roleSort": 1,
        "dataScope": "1",
        "menuCheckStrictly": true,
        "deptCheckStrictly": true,
        "status": "0",
        "delFlag": "0",
        "remark": "超级管理员",
        "flag": false,
        "admin": true,
        "createTime": "2026-04-28 10:00:00"
      }
    ],
    "total": 3,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 2.2 获取角色详情

```
GET /system/role/{roleId}
```

**权限标识**: `system:role:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleId | Long | 是 | 角色ID |

**响应**: 返回 `RoleVO` 对象（同列表中的单条数据结构）

### 2.3 查询角色下拉选项

```
GET /system/role/optionselect
```

**权限标识**: 无（内部调用）

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| roleIds | Array<Long> | 否 | Query | 角色ID列表 |

**响应**: 返回 `RoleVO[]` 数组

### 2.4 新增角色

```
POST /system/role
```

**权限标识**: `system:role:add`

**请求体**:

```json
{
  "roleName": "财务经理",
  "roleKey": "finance_manager",
  "roleSort": 5,
  "status": "0",
  "menuCheckStrictly": true,
  "deptCheckStrictly": true,
  "remark": "财务部门管理角色",
  "menuIds": [1, 100, 1001, 1002]
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleName | String | 是 | 角色名称 |
| roleKey | String | 是 | 权限字符 |
| roleSort | Integer | 是 | 显示顺序 |
| status | String | 是 | 状态（0=正常，1=停用） |
| menuCheckStrictly | Boolean | 否 | 菜单树父子联动，默认true |
| deptCheckStrictly | Boolean | 否 | 部门树父子联动，默认true |
| remark | String | 否 | 备注 |
| menuIds | Array<Long> | 否 | 分配的菜单ID列表 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### 2.5 修改角色

```
PUT /system/role
```

**权限标识**: `system:role:edit`

**请求体**:

```json
{
  "roleId": 3,
  "roleName": "管理员",
  "roleKey": "admin",
  "roleSort": 3,
  "status": "0",
  "menuCheckStrictly": true,
  "deptCheckStrictly": true,
  "remark": "",
  "menuIds": [1, 5, 100, 101, 102]
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleId | Long | 是 | 角色ID |
| roleName | String | 是 | 角色名称 |
| roleKey | String | 是 | 权限字符 |
| roleSort | Integer | 是 | 显示顺序 |
| status | String | 是 | 状态 |
| menuCheckStrictly | Boolean | 否 | 菜单树父子联动 |
| deptCheckStrictly | Boolean | 否 | 部门树父子联动 |
| remark | String | 否 | 备注 |
| menuIds | Array<Long> | 否 | 分配的菜单ID列表 |

### 2.6 删除角色

```
DELETE /system/role/{roleIds}
```

**权限标识**: `system:role:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleIds | String | 是 | 角色ID列表，逗号分隔，如 "3,4" |

**约束**: roleId=1 的超级管理员不可删除

### 2.7 导出角色列表

```
POST /system/role/export
```

**权限标识**: `system:role:export`

**请求参数**: 同列表查询参数（Query）

**响应**: 返回 Excel 文件流

---

## 3. 数据权限接口

### 3.1 修改数据权限

```
PUT /system/role/dataScope
```

**权限标识**: `system:role:edit`

**请求体**:

```json
{
  "roleId": 3,
  "roleName": "管理员",
  "roleKey": "admin",
  "dataScope": "2",
  "deptCheckStrictly": true,
  "deptIds": [100, 102, 103]
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleId | Long | 是 | 角色ID |
| dataScope | String | 是 | 数据范围（1-6） |
| deptIds | Array<Long> | 否 | 自定义数据权限的部门ID列表（dataScope=2时需要） |
| deptCheckStrictly | Boolean | 否 | 部门树父子联动 |

**数据范围枚举**:

| 值 | 含义 |
|----|------|
| 1 | 全部数据权限 |
| 2 | 自定义数据权限 |
| 3 | 本部门数据权限 |
| 4 | 本部门及以下数据权限 |
| 5 | 仅本人数据权限 |
| 6 | 部门及以下或本人数据权限 |

### 3.2 查询角色部门树

```
GET /system/role/deptTree/{roleId}
```

**权限标识**: `system:role:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleId | Long | 是 | 角色ID |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "checkedKeys": [100, 102],
    "depts": [
      {
        "id": "100",
        "label": "根机构",
        "parentId": "0",
        "weight": 0,
        "children": [
          {
            "id": "102",
            "label": "长沙分公司",
            "parentId": "100",
            "weight": 1
          }
        ]
      }
    ]
  }
}
```

---

## 4. 状态管理接口

### 4.1 修改角色状态

```
PUT /system/role/changeStatus
```

**权限标识**: `system:role:edit`

**请求体**:

```json
{
  "roleId": 3,
  "status": "1"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleId | Long | 是 | 角色ID |
| status | String | 是 | 状态（0=正常，1=停用） |

---

## 5. 用户分配接口

### 5.1 查询已分配用户列表

```
GET /system/role/authUser/allocatedList
```

**权限标识**: `system:role:list`

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| roleId | Long | 是 | Query | 角色ID |
| userName | String | 否 | Query | 用户名称（模糊匹配） |
| phonenumber | String | 否 | Query | 手机号 |
| pageNum | Integer | 否 | Query | 页码 |
| pageSize | Integer | 否 | Query | 每页条数 |

**响应**: 返回 `UserVO[]` 分页数据

### 5.2 查询未分配用户列表

```
GET /system/role/authUser/unallocatedList
```

**权限标识**: `system:role:list`

**请求参数**: 同已分配用户列表

**响应**: 返回 `UserVO[]` 分页数据

### 5.3 取消用户授权

```
PUT /system/role/authUser/cancel
```

**权限标识**: `system:role:edit`

**请求体**:

```json
{
  "userId": 3,
  "roleId": 4
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| roleId | Long | 是 | 角色ID |

### 5.4 批量取消授权

```
PUT /system/role/authUser/cancelAll
```

**权限标识**: `system:role:edit`

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| roleId | Long | 是 | Query | 角色ID |
| userIds | String | 是 | Query | 用户ID列表，逗号分隔 |

### 5.5 批量授权用户

```
PUT /system/role/authUser/selectAll
```

**权限标识**: `system:role:edit`

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| roleId | Long | 是 | Query | 角色ID |
| userIds | String | 是 | Query | 用户ID列表，逗号分隔 |

---

## 6. 前端API客户端

### 6.1 模块路径

```
src/api/system/role/index.ts
```

### 6.2 导出函数清单

| 函数名 | 对应接口 | 说明 |
|--------|----------|------|
| `listRole` | GET /system/role/list | 分页查询角色列表 |
| `optionSelect` | GET /system/role/optionselect | 查询角色下拉选项 |
| `getRole` | GET /system/role/{roleId} | 查询角色详情 |
| `addRole` | POST /system/role | 新增角色 |
| `updateRole` | PUT /system/role | 修改角色 |
| `dataScope` | PUT /system/role/dataScope | 修改数据权限 |
| `changeRoleStatus` | PUT /system/role/changeStatus | 修改角色状态 |
| `delRole` | DELETE /system/role/{roleIds} | 删除角色 |
| `allocatedUserList` | GET /system/role/authUser/allocatedList | 查询已分配用户 |
| `unallocatedUserList` | GET /system/role/authUser/unallocatedList | 查询未分配用户 |
| `authUserCancel` | PUT /system/role/authUser/cancel | 取消用户授权 |
| `authUserCancelAll` | PUT /system/role/authUser/cancelAll | 批量取消授权 |
| `authUserSelectAll` | PUT /system/role/authUser/selectAll | 批量授权用户 |
| `deptTreeSelect` | GET /system/role/deptTree/{roleId} | 查询角色部门树 |

### 6.3 类型定义

```typescript
// src/api/system/role/types.ts

export interface DeptTreeOption {
  id: string;
  label: string;
  parentId: string;
  weight: number;
  children?: DeptTreeOption[];
}

export interface RoleDeptTree {
  checkedKeys: string[];
  depts: DeptTreeOption[];
}

export interface RoleVO extends BaseEntity {
  roleId: string | number;
  roleName: string;
  roleKey: string;
  roleSort: number;
  dataScope: string;
  menuCheckStrictly: boolean;
  deptCheckStrictly: boolean;
  status: string;
  delFlag: string;
  remark?: any;
  flag: boolean;
  menuIds?: Array<string | number>;
  deptIds?: Array<string | number>;
  admin: boolean;
}

export interface RoleQuery extends PageQuery {
  roleName: string;
  roleKey: string;
  status: string;
}

export interface RoleForm {
  roleName: string;
  roleKey: string;
  roleSort: number;
  status: string;
  menuCheckStrictly: boolean;
  deptCheckStrictly: boolean;
  remark: string;
  dataScope?: string;
  roleId: string | undefined;
  menuIds: Array<string | number>;
  deptIds: Array<string | number>;
}
```

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，角色管理API接口定义 |
