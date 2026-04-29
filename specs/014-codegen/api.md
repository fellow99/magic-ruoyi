# 代码生成模块 API 文档（014-codegen/api.md）

> magic-ruoyi 代码生成模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| API 模块前缀 | `/tool/gen` |
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
| Authorization | String | 是 | Bearer Token，格式 `Bearer {token}` |
| Content-Type | String | 是（POST/PUT） | `application/json` |

---

## 2. 接口列表

### 2.1 查询已导入表列表

```
GET /tool/gen/list
```

**认证**: 需要 Token
**权限**: `tool:gen:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| tableName | String | 否 | 表名称（模糊匹配） |
| tableComment | String | 否 | 表描述（模糊匹配） |
| dataName | String | 否 | 数据源名称 |
| beginTime | String | 否 | 开始时间（yyyy-MM-dd） |
| endTime | String | 否 | 结束时间（yyyy-MM-dd） |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "tableId": 1,
      "dataName": "master",
      "tableName": "sys_user",
      "tableComment": "用户信息表",
      "className": "SysUser",
      "tplCategory": "crud",
      "packageName": "org.fellow99.magic.ruoyi",
      "moduleName": "system",
      "businessName": "user",
      "functionName": "用户管理",
      "functionAuthor": "fellow99",
      "genType": "0",
      "genPath": "",
      "createTime": "2026-04-29 10:00:00",
      "updateTime": "2026-04-29 10:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.2 查询数据库表列表

```
GET /tool/gen/db/list
```

**认证**: 需要 Token
**权限**: `tool:gen:import`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| dataName | String | 否 | 数据源名称 |
| tableName | String | 否 | 表名称（模糊匹配） |
| tableComment | String | 否 | 表描述（模糊匹配） |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "tableName": "sys_config",
      "tableComment": "参数配置表",
      "createTime": "2026-01-01 00:00:00",
      "updateTime": "2026-01-01 00:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.3 获取表详情

```
GET /tool/gen/{tableId}
```

**认证**: 需要 Token
**权限**: `tool:gen:edit`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tableId | Long | 是 | 表 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "info": {
      "tableId": 1,
      "tableName": "sys_user",
      "tableComment": "用户信息表",
      "className": "SysUser",
      "columns": []
    },
    "rows": [
      {
        "columnId": 1,
        "columnName": "user_id",
        "columnComment": "用户ID",
        "columnType": "BIGINT",
        "javaType": "Long",
        "javaField": "userId",
        "isPk": "1",
        "isIncrement": "1",
        "isRequired": "0",
        "isInsert": "0",
        "isEdit": "0",
        "isList": "1",
        "isQuery": "0",
        "queryType": "EQ",
        "htmlType": "input",
        "dictType": "",
        "sort": 1
      }
    ],
    "tables": []
  }
}
```

---

### 2.4 修改代码生成配置

```
PUT /tool/gen
```

**认证**: 需要 Token
**权限**: `tool:gen:edit`

**请求体**:

```json
{
  "tableId": "1",
  "tableName": "sys_user",
  "tableComment": "用户信息表",
  "className": "SysUser",
  "tplCategory": "crud",
  "packageName": "org.fellow99.magic.ruoyi",
  "moduleName": "system",
  "businessName": "user",
  "functionName": "用户管理",
  "functionAuthor": "fellow99",
  "genType": "0",
  "genPath": "",
  "columns": [
    {
      "columnId": "1",
      "columnName": "user_id",
      "columnComment": "用户ID",
      "columnType": "BIGINT",
      "javaType": "Long",
      "javaField": "userId",
      "isPk": "1",
      "isIncrement": "1",
      "isRequired": "0",
      "isInsert": "0",
      "isEdit": "0",
      "isList": "1",
      "isQuery": "0",
      "queryType": "EQ",
      "htmlType": "input",
      "dictType": "",
      "sort": 1
    }
  ],
  "params": {
    "treeCode": "",
    "treeName": "",
    "treeParentCode": "",
    "parentMenuId": ""
  }
}
```

**响应**:

```json
{
  "code": 200,
  "msg": "修改成功",
  "data": null
}
```

---

### 2.5 导入表

```
POST /tool/gen/importTable
```

**认证**: 需要 Token
**权限**: `tool:gen:import`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tables | String | 是 | 表名列表，逗号分隔 |
| dataName | String | 是 | 数据源名称 |

**响应**:

```json
{
  "code": 200,
  "msg": "导入成功",
  "data": null
}
```

---

### 2.6 预览生成代码

```
GET /tool/gen/preview/{tableId}
```

**认证**: 需要 Token
**权限**: `tool:gen:preview`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tableId | Long | 是 | 表 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "domain.java": "package org.fellow99...\n\npublic class SysUser {...}",
    "mapper.java": "package org.fellow99...\n\npublic interface SysUserMapper {...}",
    "service.java": "...",
    "controller.java": "...",
    "mapper.xml": "...",
    "index.vue": "...",
    "index.ts": "...",
    "types.ts": "..."
  }
}
```

---

### 2.7 删除表

```
DELETE /tool/gen/{tableIds}
```

**认证**: 需要 Token
**权限**: `tool:gen:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tableIds | String | 是 | 表 ID 列表，逗号分隔 |

**响应**:

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

### 2.8 生成代码（自定义路径）

```
GET /tool/gen/genCode/{tableId}
```

**认证**: 需要 Token
**权限**: `tool:gen:code`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tableId | Long | 是 | 表 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "成功生成到自定义路径：/opt/project/",
  "data": null
}
```

---

### 2.9 批量生成代码（下载 ZIP）

```
GET /tool/gen/batchGenCode
```

**认证**: 需要 Token
**权限**: `tool:gen:code`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tableIdStr | String | 是 | 表 ID 列表，逗号分隔 |

**响应**: 二进制 ZIP 文件流，文件名 `ruoyi.zip`

---

### 2.10 同步数据库表结构

```
GET /tool/gen/synchDb/{tableId}
```

**认证**: 需要 Token
**权限**: `tool:gen:edit`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| tableId | Long | 是 | 表 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "同步成功",
  "data": null
}
```

---

### 2.11 获取数据源名称列表

```
GET /tool/gen/getDataNames
```

**认证**: 需要 Token

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": ["master", "slave1", "slave2"]
}
```

---

## 3. 前端 API 封装

### 3.1 `@/api/tool/gen/index.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listTable(query)` | GET | `/tool/gen/list` | 查询已导入表列表 |
| `listDbTable(query)` | GET | `/tool/gen/db/list` | 查询数据库表列表 |
| `getGenTable(tableId)` | GET | `/tool/gen/{tableId}` | 获取表详情 |
| `updateGenTable(data)` | PUT | `/tool/gen` | 修改代码生成配置 |
| `importTable(data)` | POST | `/tool/gen/importTable` | 导入表 |
| `previewTable(tableId)` | GET | `/tool/gen/preview/{tableId}` | 预览生成代码 |
| `delTable(tableId)` | DELETE | `/tool/gen/{tableId}` | 删除表 |
| `genCode(tableId)` | GET | `/tool/gen/genCode/{tableId}` | 生成代码（自定义路径） |
| `synchDb(tableId)` | GET | `/tool/gen/synchDb/{tableId}` | 同步数据库表结构 |
| `getDataNames()` | GET | `/tool/gen/getDataNames` | 获取数据源名称列表 |

### 3.2 类型定义（`@/api/tool/gen/types.ts`）

| 类型 | 说明 |
|------|------|
| `TableVO` | 已导入表视图对象 |
| `TableQuery` | 已导入表查询参数 |
| `DbTableVO` | 数据库表视图对象 |
| `DbTableQuery` | 数据库表查询参数 |
| `DbColumnVO` | 数据库字段视图对象 |
| `GenTableVO` | 生成表详情响应（含 info、rows、tables） |
| `DbTableForm` | 表编辑表单（含 columns 数组） |
| `DbColumnForm` | 字段编辑表单 |
| `DbParamForm` | 树形/菜单参数 |

---

## 4. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
