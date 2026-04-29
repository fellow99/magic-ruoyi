# 003-User 用户管理模块 - API 规格

> 本文档定义用户管理模块的全部 REST API 接口。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 通用约定

### 1.1 响应格式

所有 API 响应统一使用 `R<T>` 包装：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

分页响应使用 `TableDataInfo<T>` 包装：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [],
  "total": 0
}
```

### 1.2 认证方式

- 所有接口通过 JWT Token 认证
- Token 放在请求头 `Authorization` 中
- 非白名单接口必须通过权限校验

### 1.3 请求加密

- 全局接口加密默认启用（RSA 非对称加密）
- 密码相关接口额外标注 `@ApiEncrypt`
- 前端通过 `isEncrypt: true` 请求头触发加密

---

## 2. 用户管理接口

### 2.1 分页查询用户列表

```
GET /system/user/list
```

**权限**: `system:user:list`

**请求参数** (Query):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | string | 否 | 用户名称（模糊匹配） |
| nickName | string | 否 | 用户昵称（模糊匹配） |
| phonenumber | string | 否 | 手机号码（模糊匹配） |
| status | string | 否 | 用户状态（0=正常, 1=停用） |
| deptId | string/number | 否 | 部门ID |
| roleId | string/number | 否 | 角色ID |
| userIds | string/number/array | 否 | 用户ID列表 |
| pageNum | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 10 |
| beginTime | string | 否 | 开始时间（YYYY-MM-DD HH:mm:ss） |
| endTime | string | 否 | 结束时间（YYYY-MM-DD HH:mm:ss） |

**响应** (`TableDataInfo<UserVO>`):

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "userId": 1,
      "tenantId": "000000",
      "deptId": 103,
      "userName": "admin",
      "nickName": "管理员",
      "userType": "sys_user",
      "email": "",
      "phonenumber": "",
      "sex": "1",
      "avatar": null,
      "status": "0",
      "delFlag": "0",
      "loginIp": "127.0.0.1",
      "loginDate": "2026-04-28 10:00:00",
      "remark": "管理员",
      "deptName": "研发部门",
      "roles": [],
      "roleIds": null,
      "postIds": null,
      "roleId": null,
      "admin": true,
      "createTime": "2026-04-28 10:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.2 获取用户详情

```
GET /system/user/{userId}
```

**权限**: `system:user:query`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | number | 否 | 用户ID，不传时返回初始化数据（用于新增表单） |

**响应** (`UserInfoVO`):

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "user": {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      "deptId": 103,
      "status": "0",
      "sex": "1",
      "email": "",
      "phonenumber": "",
      "remark": "管理员",
      "roleIds": [1],
      "postIds": [1],
      "roles": []
    },
    "roles": [],
    "roleIds": [1],
    "posts": [],
    "postIds": [1],
    "roleGroup": "管理员",
    "postGroup": "董事长"
  }
}
```

---

### 2.3 获取登录用户信息

```
GET /system/user/getInfo
```

**权限**: 无（登录即可）

**响应** (`UserInfo`):

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "user": {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      "deptId": 103,
      "deptName": "研发部门",
      "status": "0",
      "sex": "1",
      "email": "",
      "phonenumber": "",
      "avatar": null,
      "createTime": "2026-04-28 10:00:00"
    },
    "roles": ["admin"],
    "permissions": ["*:*:*"]
  }
}
```

---

### 2.4 新增用户

```
POST /system/user
```

**权限**: `system:user:add`

**请求体** (`UserForm`):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | string | 是 | 用户名称（2-20字符） |
| nickName | string | 是 | 用户昵称 |
| password | string | 是 | 用户密码（5-20字符） |
| deptId | number | 否 | 部门ID |
| phonenumber | string | 否 | 手机号码 |
| email | string | 否 | 邮箱地址 |
| sex | string | 否 | 性别（0=男, 1=女, 2=未知） |
| status | string | 是 | 状态（0=正常, 1=停用），默认 0 |
| remark | string | 否 | 备注 |
| roleIds | string[] | 是 | 角色ID列表 |
| postIds | string[] | 否 | 岗位ID列表 |

**请求示例**:

```json
{
  "userName": "zhangsan",
  "nickName": "张三",
  "password": "123456",
  "deptId": 103,
  "phonenumber": "13800138000",
  "email": "zhangsan@example.com",
  "sex": "0",
  "status": "0",
  "remark": "新员工",
  "roleIds": ["4"],
  "postIds": ["4"]
}
```

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

### 2.5 修改用户

```
PUT /system/user
```

**权限**: `system:user:edit`

**请求体** (`UserForm`):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | string | 是 | 用户ID |
| userName | string | 是 | 用户名称 |
| nickName | string | 是 | 用户昵称 |
| deptId | number | 否 | 部门ID（编辑自己时忽略） |
| phonenumber | string | 否 | 手机号码 |
| email | string | 否 | 邮箱地址 |
| sex | string | 否 | 性别 |
| status | string | 是 | 状态 |
| remark | string | 否 | 备注 |
| roleIds | string[] | 否 | 角色ID列表（编辑自己时忽略） |
| postIds | string[] | 否 | 岗位ID列表（编辑自己时忽略） |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

### 2.6 删除用户

```
DELETE /system/user/{userIds}
```

**权限**: `system:user:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userIds | string | 是 | 用户ID，多个用逗号分隔 |

**约束**:

- 超级管理员（userId=1）不可删除
- 支持批量删除

**响应**:

```json
{
  "code": 200,
  "msg": "删除成功"
}
```

---

### 2.7 重置密码

```
PUT /system/user/resetPwd
```

**权限**: `system:user:resetPwd`

**请求头**: `isEncrypt: true`, `repeatSubmit: false`

**请求体**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | number/string | 是 | 用户ID |
| password | string | 是 | 新密码（5-20字符） |

**请求示例**:

```json
{
  "userId": 3,
  "password": "newPassword123"
}
```

**响应**:

```json
{
  "code": 200,
  "msg": "重置成功"
}
```

---

### 2.8 修改用户状态

```
PUT /system/user/changeStatus
```

**权限**: `system:user:edit`

**请求体**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | number/string | 是 | 用户ID |
| status | string | 是 | 状态（0=正常, 1=停用） |

**请求示例**:

```json
{
  "userId": 3,
  "status": "1"
}
```

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

### 2.9 用户授权角色

```
PUT /system/user/authRole
```

**权限**: `system:user:edit`

**请求参数** (Query):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | string | 是 | 用户ID |
| roleIds | string | 是 | 角色ID列表，逗号分隔 |

**响应**:

```json
{
  "code": 200,
  "msg": "授权成功"
}
```

---

### 2.10 查询授权角色

```
GET /system/user/authRole/{userId}
```

**权限**: `system:user:edit`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | string/number | 是 | 用户ID |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "user": {
      "userId": 3,
      "userName": "test1",
      "nickName": "测试账号1",
      "roles": [{"roleId": 4, "roleName": "普通角色", "flag": true}]
    },
    "roles": [
      {"roleId": 1, "roleName": "超级管理员", "roleKey": "admin", "status": "0", "createTime": "..."},
      {"roleId": 4, "roleName": "普通角色", "roleKey": "common", "status": "0", "createTime": "..."}
    ]
  }
}
```

---

### 2.11 导出用户

```
POST /system/user/export
```

**权限**: `system:user:export`

**请求参数** (Query): 与分页查询参数相同（不含分页参数）

**响应**: Excel 文件流（`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`）

---

### 2.12 导入用户

```
POST /system/user/importData
```

**权限**: `system:user:import`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | Excel 文件（.xls/.xlsx） |
| updateSupport | boolean | 否 | 是否更新已存在数据，默认 false |

**响应**:

```json
{
  "code": 200,
  "msg": "导入成功：新增 5 条，更新 2 条，失败 0 条"
}
```

---

### 2.13 下载导入模板

```
GET /system/user/importTemplate
```

**权限**: `system:user:import`

**响应**: Excel 模板文件流

---

### 2.14 查询部门下拉树

```
GET /system/user/deptTree
```

**权限**: `system:user:list`

**响应** (`DeptTreeVO[]`):

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 100,
      "label": "总公司",
      "disabled": false,
      "children": [
        {
          "id": 103,
          "label": "研发部门",
          "disabled": false,
          "children": []
        }
      ]
    }
  ]
}
```

---

### 2.15 查询部门下所有用户

```
GET /system/user/list/dept/{deptId}
```

**权限**: `system:user:list`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deptId | number/string | 是 | 部门ID |

**响应** (`UserVO[]`):

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      "deptId": 103,
      "status": "0"
    }
  ]
}
```

---

### 2.16 批量查询用户

```
GET /system/user/optionselect?userIds=1,3,4
```

**权限**: `system:user:query`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userIds | string | 是 | 用户ID列表，逗号分隔 |

**响应** (`UserVO[]`):

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {"userId": 1, "userName": "admin", "nickName": "管理员"},
    {"userId": 3, "userName": "test1", "nickName": "测试账号1"}
  ]
}
```

---

## 3. 个人中心接口

### 3.1 获取个人信息

```
GET /system/user/profile
```

**权限**: 无（登录即可）

**响应** (`UserInfoVO`):

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "user": {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      "deptId": 103,
      "deptName": "研发部门",
      "phonenumber": "13800138000",
      "email": "admin@example.com",
      "sex": "1",
      "avatar": null,
      "status": "0",
      "createTime": "2026-04-28 10:00:00"
    },
    "roleGroup": "管理员",
    "postGroup": "董事长"
  }
}
```

---

### 3.2 修改个人信息

```
PUT /system/user/profile
```

**权限**: 无（登录即可）

**请求体**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickName | string | 是 | 用户昵称 |
| phonenumber | string | 是 | 手机号码 |
| email | string | 是 | 邮箱地址 |
| sex | string | 否 | 性别 |

**响应**:

```json
{
  "code": 200,
  "msg": "修改成功"
}
```

---

### 3.3 修改个人密码

```
PUT /system/user/profile/updatePwd
```

**权限**: 无（登录即可）

**请求头**: `isEncrypt: true`, `repeatSubmit: false`

**请求体**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 旧密码 |
| newPassword | string | 是 | 新密码（6-20字符） |

**响应**:

```json
{
  "code": 200,
  "msg": "修改成功"
}
```

---

### 3.4 上传头像

```
POST /system/user/profile/avatar
```

**权限**: 无（登录即可）

**请求体**: `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| avatarfile | file | 是 | 头像图片文件 |

**响应**:

```json
{
  "code": 200,
  "msg": "修改成功",
  "data": {
    "imgUrl": "https://example.com/avatar/123456.png"
  }
}
```

---

## 4. 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 500 | 系统内部错误 |
| 401 | 未认证，token 无效或过期 |
| 403 | 无权限 |
| 601 | 用户名称已存在 |
| 602 | 手机号已存在 |
| 603 | 邮箱已存在 |
| 604 | 用户不可删除（超级管理员） |
| 605 | 旧密码不正确 |
