# 文件存储模块 API 文档（011-oss/api.md）

> magic-ruoyi 文件存储模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 文件管理前缀 | `/resource/oss` |
| 配置管理前缀 | `/resource/oss/config` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 文件管理接口

### 2.1 查询文件列表

```
GET /resource/oss/list
```

**认证**: 需要 Token
**权限**: `system:oss:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| fileName | String | 否 | 文件名（模糊匹配） |
| originalName | String | 否 | 原名（模糊匹配） |
| fileSuffix | String | 否 | 文件后缀 |
| service | String | 否 | 服务商 |
| createTime | String | 否 | 创建时间范围 |
| orderByColumn | String | 否 | 排序字段 |
| isAsc | String | 否 | 排序方向 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "ossId": 1,
      "fileName": "2026/01/01/abc123.jpg",
      "originalName": "photo.jpg",
      "fileSuffix": ".jpg",
      "url": "https://oss.example.com/2026/01/01/abc123.jpg",
      "createByName": "admin",
      "service": "minio",
      "createTime": "2026-01-01 00:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.2 按 ID 列表查询文件

```
GET /resource/oss/listByIds/{ossIds}
```

**认证**: 需要 Token

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| ossIds | String | 是 | 文件 ID，多个用逗号分隔 |

---

### 2.3 删除文件

```
DELETE /resource/oss/{ossIds}
```

**认证**: 需要 Token
**权限**: `system:oss:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| ossIds | String | 是 | 文件 ID，多个用逗号分隔 |

---

## 3. 配置管理接口

### 3.1 查询配置列表

```
GET /resource/oss/config/list
```

**认证**: 需要 Token
**权限**: `system:ossConfig:list`

---

### 3.2 查询配置详情

```
GET /resource/oss/config/{ossConfigId}
```

**认证**: 需要 Token

---

### 3.3 新增配置

```
POST /resource/oss/config
```

**认证**: 需要 Token
**权限**: `system:ossConfig:add`

---

### 3.4 修改配置

```
PUT /resource/oss/config
```

**认证**: 需要 Token
**权限**: `system:ossConfig:edit`

---

### 3.5 删除配置

```
DELETE /resource/oss/config/{ossConfigIds}
```

**认证**: 需要 Token
**权限**: `system:ossConfig:remove`

---

## 4. 前端 API 封装

### 4.1 `@/api/system/oss/index.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listOss(query)` | GET | `/resource/oss/list` | 查询文件列表 |
| `listByIds(ossId)` | GET | `/resource/oss/listByIds/{ossId}` | 按 ID 查询 |
| `delOss(ossIds)` | DELETE | `/resource/oss/{ossIds}` | 删除文件 |

### 4.2 类型定义（`@/api/system/oss/types.ts`）

```typescript
export interface OssVO extends BaseEntity {
  ossId: string | number;
  fileName: string;
  originalName: string;
  fileSuffix: string;
  url: string;
  createByName: string;
  service: string;
}

export interface OssQuery extends PageQuery {
  fileName: string;
  originalName: string;
  fileSuffix: string;
  createTime: string;
  service: string;
  orderByColumn: string;
  isAsc: string;
}

export interface OssForm {
  file: undefined | string;
}
```

---

## 5. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
