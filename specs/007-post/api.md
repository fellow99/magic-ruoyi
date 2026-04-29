# 岗位管理模块 API 文档（007-post/api.md）

> magic-ruoyi 岗位管理模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 模块前缀 | `/system/post` |
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

### 1.3 列表响应格式

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [],
  "total": 0
}
```

| 字段 | 类型 | 描述 |
|------|------|------|
| rows | Array | 数据列表 |
| total | Long | 总记录数 |

---

## 2. 岗位接口

### 2.1 查询岗位列表

```
GET /system/post/list
```

**认证**: 需要 Token
**权限**: `system:post:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| deptId | Long | 否 | 部门 ID |
| belongDeptId | Long | 否 | 所属部门 ID（左侧部门树点击时传入） |
| postCode | String | 否 | 岗位编码（模糊匹配） |
| postName | String | 否 | 岗位名称（模糊匹配） |
| postCategory | String | 否 | 类别编码（模糊匹配） |
| status | String | 否 | 状态（0=正常, 1=停用） |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "postId": 1,
      "deptId": 100,
      "postCode": "ceo",
      "postName": "董事长",
      "postCategory": "",
      "deptName": "总公司",
      "postSort": 1,
      "status": "0",
      "remark": "",
      "createTime": "2026-01-01 00:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.2 查询岗位详情

```
GET /system/post/{postId}
```

**认证**: 需要 Token
**权限**: `system:post:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| postId | Long | 是 | 岗位 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "postId": 1,
    "deptId": 100,
    "postCode": "ceo",
    "postName": "董事长",
    "postCategory": "",
    "deptName": "总公司",
    "postSort": 1,
    "status": "0",
    "remark": ""
  }
}
```

---

### 2.3 新增岗位

```
POST /system/post
```

**认证**: 需要 Token
**权限**: `system:post:add`

**请求体**:

```json
{
  "deptId": 100,
  "postCode": "manager",
  "postName": "部门经理",
  "postCategory": "management",
  "postSort": 2,
  "status": "0",
  "remark": ""
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| deptId | Long | 是 | 部门 ID |
| postCode | String | 是 | 岗位编码 |
| postName | String | 是 | 岗位名称 |
| postCategory | String | 否 | 类别编码 |
| postSort | Integer | 是 | 排序 |
| status | String | 否 | 状态，默认 "0" |
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

### 2.4 修改岗位

```
PUT /system/post
```

**认证**: 需要 Token
**权限**: `system:post:edit`

**请求体**:

```json
{
  "postId": 1,
  "deptId": 100,
  "postCode": "ceo",
  "postName": "董事长",
  "postCategory": "",
  "postSort": 1,
  "status": "0",
  "remark": "更新备注"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| postId | Long | 是 | 岗位 ID |
| 其他字段 | - | - | 同新增接口 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 2.5 删除岗位

```
DELETE /system/post/{postIds}
```

**认证**: 需要 Token
**权限**: `system:post:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| postIds | String | 是 | 岗位 ID，多个用逗号分隔 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 2.6 岗位下拉选择

```
GET /system/post/optionselect
```

**认证**: 需要 Token

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| deptId | Long | 否 | 部门 ID，按部门过滤 |
| postIds | Array | 否 | 岗位 ID 列表，用于回显已选岗位 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "postId": 1,
      "postCode": "ceo",
      "postName": "董事长",
      "deptName": "总公司",
      "postSort": 1,
      "status": "0"
    }
  ]
}
```

---

### 2.7 查询部门下拉树

```
GET /system/post/deptTree
```

**认证**: 需要 Token

**请求参数**: 无

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 100,
      "label": "总公司",
      "children": [
        {
          "id": 101,
          "label": "深圳分公司",
          "children": []
        }
      ]
    }
  ]
}
```

---

### 2.8 导出岗位数据

```
POST /system/post/export
```

**认证**: 需要 Token
**权限**: `system:post:export`

**请求体**: 同查询列表的 Query 参数

**响应**: Excel 文件流

---

## 3. 前端 API 封装

### 3.1 `@/api/system/post/index.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listPost(query)` | GET | `/system/post/list` | 查询岗位列表 |
| `getPost(postId)` | GET | `/system/post/{postId}` | 查询岗位详情 |
| `optionselect(deptId, postIds)` | GET | `/system/post/optionselect` | 岗位下拉选择 |
| `addPost(data)` | POST | `/system/post` | 新增岗位 |
| `updatePost(data)` | PUT | `/system/post` | 修改岗位 |
| `delPost(postIds)` | DELETE | `/system/post/{postIds}` | 删除岗位 |
| `deptTreeSelect()` | GET | `/system/post/deptTree` | 部门下拉树 |

### 3.2 类型定义（`@/api/system/post/types.ts`）

```typescript
export interface PostVO extends BaseEntity {
  postId: number | string;
  deptId: number | string;
  postCode: string;
  postName: string;
  postCategory: string;
  deptName: string;
  postSort: number;
  status: string;
  remark: string;
}

export interface PostForm {
  postId: number | string | undefined;
  deptId: number | string | undefined;
  postCode: string;
  postName: string;
  postCategory: string;
  postSort: number;
  status: string;
  remark: string;
}

export interface PostQuery extends PageQuery {
  deptId: number | string;
  belongDeptId: number | string;
  postCode: string;
  postName: string;
  postCategory: string;
  status: string;
}
```

---

## 4. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
