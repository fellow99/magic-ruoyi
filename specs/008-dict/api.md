# 字典管理模块 API 文档（008-dict/api.md）

> magic-ruoyi 字典管理模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 字典类型前缀 | `/system/dict/type` |
| 字典数据前缀 | `/system/dict/data` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 字典类型接口

### 2.1 查询字典类型列表

```
GET /system/dict/type/list
```

**认证**: 需要 Token
**权限**: `system:dict:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| dictName | String | 否 | 字典名称（模糊匹配） |
| dictType | String | 否 | 字典类型（模糊匹配） |
| beginTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "dictId": 1,
      "dictName": "正常停用",
      "dictType": "sys_normal_disable",
      "remark": "正常停用列表",
      "createTime": "2026-01-01 00:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.2 查询字典类型详情

```
GET /system/dict/type/{dictId}
```

**认证**: 需要 Token
**权限**: `system:dict:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| dictId | Long | 是 | 字典 ID |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "dictId": 1,
    "dictName": "正常停用",
    "dictType": "sys_normal_disable",
    "remark": "正常停用列表"
  }
}
```

---

### 2.3 新增字典类型

```
POST /system/dict/type
```

**认证**: 需要 Token
**权限**: `system:dict:add`

**请求体**:

```json
{
  "dictName": "通知类型",
  "dictType": "sys_notice_type",
  "remark": "通知类型列表"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| dictName | String | 是 | 字典名称 |
| dictType | String | 是 | 字典类型 |
| remark | String | 否 | 备注 |

---

### 2.4 修改字典类型

```
PUT /system/dict/type
```

**认证**: 需要 Token
**权限**: `system:dict:edit`

**请求体**:

```json
{
  "dictId": 1,
  "dictName": "正常停用",
  "dictType": "sys_normal_disable",
  "remark": "更新后的备注"
}
```

---

### 2.5 删除字典类型

```
DELETE /system/dict/type/{dictIds}
```

**认证**: 需要 Token
**权限**: `system:dict:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| dictIds | String | 是 | 字典 ID，多个用逗号分隔 |

---

### 2.6 刷新字典缓存

```
DELETE /system/dict/type/refreshCache
```

**认证**: 需要 Token
**权限**: `system:dict:remove`

**请求参数**: 无

---

### 2.7 字典类型下拉选择

```
GET /system/dict/type/optionselect
```

**认证**: 需要 Token

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "dictId": 1,
      "dictName": "正常停用",
      "dictType": "sys_normal_disable"
    }
  ]
}
```

---

### 2.8 导出字典类型

```
POST /system/dict/type/export
```

**认证**: 需要 Token
**权限**: `system:dict:export`

**响应**: Excel 文件流

---

## 3. 字典数据接口

### 3.1 查询字典数据列表

```
GET /system/dict/data/list
```

**认证**: 需要 Token
**权限**: `system:dict:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| dictName | String | 否 | 字典名称 |
| dictType | String | 否 | 字典类型 |
| dictLabel | String | 否 | 字典标签（模糊匹配） |

---

### 3.2 查询字典数据详情

```
GET /system/dict/data/{dictCode}
```

**认证**: 需要 Token
**权限**: `system:dict:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| dictCode | Long | 是 | 字典数据 ID |

---

### 3.3 新增字典数据

```
POST /system/dict/data
```

**认证**: 需要 Token
**权限**: `system:dict:add`

**请求体**:

```json
{
  "dictType": "sys_notice_type",
  "dictLabel": "通知",
  "dictValue": "1",
  "cssClass": "",
  "listClass": "primary",
  "dictSort": 1,
  "remark": ""
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| dictType | String | 是 | 字典类型 |
| dictLabel | String | 是 | 字典标签 |
| dictValue | String | 是 | 字典键值 |
| cssClass | String | 否 | 样式属性 |
| listClass | String | 否 | 回显样式 |
| dictSort | Integer | 是 | 排序 |
| remark | String | 否 | 备注 |

---

### 3.4 修改字典数据

```
PUT /system/dict/data
```

**认证**: 需要 Token
**权限**: `system:dict:edit`

**请求体**: 同新增，需包含 dictCode

---

### 3.5 删除字典数据

```
DELETE /system/dict/data/{dictCodes}
```

**认证**: 需要 Token
**权限**: `system:dict:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| dictCodes | String | 是 | 字典数据 ID，多个用逗号分隔 |

---

### 3.6 按字典类型查询字典数据

```
GET /system/dict/data/type/{dictType}
```

**认证**: 需要 Token

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| dictType | String | 是 | 字典类型标识 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "dictCode": "1",
      "dictLabel": "通知",
      "dictValue": "1",
      "listClass": "primary",
      "cssClass": "",
      "dictSort": 1
    },
    {
      "dictCode": "2",
      "dictLabel": "公告",
      "dictValue": "2",
      "listClass": "warning",
      "cssClass": "",
      "dictSort": 2
    }
  ]
}
```

**用途**: 前端页面通过此接口动态加载字典数据，用于下拉选项、标签展示等场景。

---

### 3.7 导出字典数据

```
POST /system/dict/data/export
```

**认证**: 需要 Token
**权限**: `system:dict:export`

**响应**: Excel 文件流

---

## 4. 前端 API 封装

### 4.1 字典类型 API（`@/api/system/dict/type/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listType(query)` | GET | `/system/dict/type/list` | 查询字典类型列表 |
| `getType(dictId)` | GET | `/system/dict/type/{dictId}` | 查询字典类型详情 |
| `addType(data)` | POST | `/system/dict/type` | 新增字典类型 |
| `updateType(data)` | PUT | `/system/dict/type` | 修改字典类型 |
| `delType(dictIds)` | DELETE | `/system/dict/type/{dictIds}` | 删除字典类型 |
| `refreshCache()` | DELETE | `/system/dict/type/refreshCache` | 刷新字典缓存 |
| `optionselect()` | GET | `/system/dict/type/optionselect` | 字典类型下拉 |

### 4.2 字典数据 API（`@/api/system/dict/data/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `getDicts(dictType)` | GET | `/system/dict/data/type/{dictType}` | 按类型查询字典数据 |
| `listData(query)` | GET | `/system/dict/data/list` | 查询字典数据列表 |
| `getData(dictCode)` | GET | `/system/dict/data/{dictCode}` | 查询字典数据详情 |
| `addData(data)` | POST | `/system/dict/data` | 新增字典数据 |
| `updateData(data)` | PUT | `/system/dict/data` | 修改字典数据 |
| `delData(dictCodes)` | DELETE | `/system/dict/data/{dictCodes}` | 删除字典数据 |

### 4.3 类型定义

**字典类型（`@/api/system/dict/type/types.ts`）**:

```typescript
export interface DictTypeVO extends BaseEntity {
  dictId: number | string;
  dictName: string;
  dictType: string;
  remark: string;
}

export interface DictTypeForm {
  dictId: number | string | undefined;
  dictName: string;
  dictType: string;
  remark: string;
}

export interface DictTypeQuery extends PageQuery {
  dictName: string;
  dictType: string;
}
```

**字典数据（`@/api/system/dict/data/types.ts`）**:

```typescript
export interface DictDataVO extends BaseEntity {
  dictCode: string;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  listClass: ElTagType;
  dictSort: number;
  remark: string;
}

export interface DictDataForm {
  dictType?: string;
  dictCode: string | undefined;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  listClass: ElTagType;
  dictSort: number;
  remark: string;
}

export interface DictDataQuery extends PageQuery {
  dictName: string;
  dictType: string;
  dictLabel: string;
}
```

---

## 5. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
