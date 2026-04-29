# 部门管理API接口文档 (api.md)

> magic-ruoyi 部门管理模块的API接口定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 模块路径 | `/system/dept` |
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

---

## 2. 部门CRUD接口

### 2.1 查询部门列表（树形）

```
GET /system/dept/list
```

**权限标识**: `system:dept:list`

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| deptName | String | 否 | Query | 部门名称（模糊匹配） |
| deptCategory | String | 否 | Query | 类别编码（模糊匹配） |
| status | String | 否 | Query | 状态（0=正常，1=停用） |

**响应示例**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "deptId": 100,
      "parentId": 0,
      "parentName": "",
      "ancestors": "0",
      "deptName": "根机构",
      "deptCategory": null,
      "orderNum": 0,
      "leader": null,
      "phone": "",
      "email": "",
      "status": "0",
      "delFlag": "0",
      "createTime": "2026-04-28 10:00:00",
      "children": []
    }
  ]
}
```

> 返回扁平列表，前端通过 `handleTree` 工具函数转换为树形结构。

### 2.2 获取部门详情

```
GET /system/dept/{deptId}
```

**权限标识**: `system:dept:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| deptId | Long | 是 | 部门ID |

**响应**: 返回 `DeptVO` 对象（同列表中的单条数据结构）

### 2.3 查询部门下拉选项

```
GET /system/dept/optionselect
```

**权限标识**: 无（内部调用）

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| deptIds | Array<Long> | 否 | Query | 部门ID列表 |

**响应**: 返回 `DeptVO[]` 数组

### 2.4 查询部门列表（排除指定节点）

```
GET /system/dept/list/exclude/{deptId}
```

**权限标识**: `system:dept:list`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| deptId | Long | 是 | 需要排除的部门ID及其子部门 |

**用途**: 修改部门时，上级部门选择需排除当前部门及其子部门，防止循环引用。

**响应**: 返回 `DeptVO[]` 数组（不包含指定部门及其子孙部门）

### 2.5 新增部门

```
POST /system/dept
```

**权限标识**: `system:dept:add`

**请求体**:

```json
{
  "parentId": 100,
  "deptName": "研发部",
  "deptCategory": "R&D",
  "orderNum": 1,
  "leader": 1,
  "phone": "13800138000",
  "email": "dev@example.com",
  "status": "0"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| parentId | Long | 是 | 上级部门ID |
| deptName | String | 是 | 部门名称 |
| deptCategory | String | 否 | 部门类别编码 |
| orderNum | Integer | 是 | 显示顺序 |
| leader | Long | 否 | 负责人用户ID |
| phone | String | 否 | 联系电话 |
| email | String | 否 | 邮箱 |
| status | String | 是 | 状态（0=正常，1=停用） |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### 2.6 修改部门

```
PUT /system/dept
```

**权限标识**: `system:dept:edit`

**请求体**:

```json
{
  "deptId": 102,
  "parentId": 100,
  "deptName": "研发中心",
  "deptCategory": "R&D",
  "orderNum": 1,
  "leader": 1,
  "phone": "13800138000",
  "email": "dev@example.com",
  "status": "0"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| deptId | Long | 是 | 部门ID |
| parentId | Long | 是 | 上级部门ID |
| deptName | String | 是 | 部门名称 |
| deptCategory | String | 否 | 部门类别编码 |
| orderNum | Integer | 是 | 显示顺序 |
| leader | Long | 否 | 负责人用户ID |
| phone | String | 否 | 联系电话 |
| email | String | 否 | 邮箱 |
| status | String | 是 | 状态 |

**约束**: parentId 不能是当前部门自身或其子部门。

### 2.7 删除部门

```
DELETE /system/dept/{deptId}
```

**权限标识**: `system:dept:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| deptId | Long | 是 | 部门ID |

**约束**:
- 存在子部门时不可删除
- 存在关联用户时不可删除
- 删除操作为逻辑删除（del_flag = '1'）

---

## 3. 前端API客户端

### 3.1 模块路径

```
src/api/system/dept/index.ts
```

### 3.2 导出函数清单

| 函数名 | 对应接口 | 说明 |
|--------|----------|------|
| `listDept` | GET /system/dept/list | 查询部门列表 |
| `optionSelect` | GET /system/dept/optionselect | 查询部门下拉选项 |
| `listDeptExcludeChild` | GET /system/dept/list/exclude/{deptId} | 查询部门列表（排除指定节点） |
| `getDept` | GET /system/dept/{deptId} | 查询部门详情 |
| `addDept` | POST /system/dept | 新增部门 |
| `updateDept` | PUT /system/dept | 修改部门 |
| `delDept` | DELETE /system/dept/{deptId} | 删除部门 |

### 3.3 类型定义

```typescript
// src/api/system/dept/types.ts

/**
 * 部门查询参数
 */
export interface DeptQuery extends PageQuery {
  deptName?: string;
  deptCategory?: string;
  status?: number;
}

/**
 * 部门视图对象
 */
export interface DeptVO extends BaseEntity {
  id: number | string;
  parentName: string;
  parentId: number | string;
  children: DeptVO[];
  deptId: number | string;
  deptName: string;
  deptCategory: string;
  orderNum: number;
  leader: string;
  phone: string;
  email: string;
  status: string;
  delFlag: string;
  ancestors: string;
  menuId: string | number;
}

/**
 * 部门树视图对象
 */
export interface DeptTreeVO extends BaseEntity {
  id: number | string;
  label: string;
  parentId: number | string;
  weight: number;
  children: DeptTreeVO[];
  disabled: boolean;
}

/**
 * 部门表单对象
 */
export interface DeptForm {
  parentName?: string;
  parentId?: number | string;
  children?: DeptForm[];
  deptId?: number | string;
  deptName?: string;
  deptCategory?: string;
  orderNum?: number;
  leader?: string;
  phone?: string;
  email?: string;
  status?: string;
  delFlag?: string;
  ancestors?: string;
}
```

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，部门管理API接口定义 |
