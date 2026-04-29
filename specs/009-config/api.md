# 参数配置模块 API 文档（009-config/api.md）

> magic-ruoyi 参数配置模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 模块前缀 | `/system/config` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 参数配置接口

### 2.1 查询参数列表

```
GET /system/config/list
```

**认证**: 需要 Token
**权限**: `system:config:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| configName | String | 否 | 参数名称（模糊匹配） |
| configKey | String | 否 | 参数键名（模糊匹配） |
| configType | String | 否 | 系统内置（Y=是, N=否） |
| beginTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "configId": 1,
      "configName": "主框架页-菜单侧栏颜色",
      "configKey": "sys.sideMenuTheme",
      "configValue": "theme-dark",
      "configType": "Y",
      "remark": "侧栏菜单主题",
      "createTime": "2026-01-01 00:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.2 查询参数详情

```
GET /system/config/{configId}
```

**认证**: 需要 Token
**权限**: `system:config:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| configId | Long | 是 | 参数 ID |

---

### 2.3 新增参数配置

```
POST /system/config
```

**认证**: 需要 Token
**权限**: `system:config:add`

**请求体**:

```json
{
  "configName": "注册开关",
  "configKey": "sys.register",
  "configValue": "false",
  "configType": "Y",
  "remark": "是否允许用户注册"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| configName | String | 是 | 参数名称 |
| configKey | String | 是 | 参数键名 |
| configValue | String | 是 | 参数键值 |
| configType | String | 否 | 系统内置，默认 "Y" |
| remark | String | 否 | 备注 |

---

### 2.4 修改参数配置

```
PUT /system/config
```

**认证**: 需要 Token
**权限**: `system:config:edit`

**请求体**:

```json
{
  "configId": 1,
  "configName": "注册开关",
  "configKey": "sys.register",
  "configValue": "true",
  "configType": "Y",
  "remark": "已开启注册"
}
```

---

### 2.5 删除参数配置

```
DELETE /system/config/{configIds}
```

**认证**: 需要 Token
**权限**: `system:config:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| configIds | String | 是 | 参数 ID，多个用逗号分隔 |

---

### 2.6 按参数键名查询参数值

```
GET /system/config/configKey/{configKey}
```

**认证**: 需要 Token

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| configKey | String | 是 | 参数键名 |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": "false"
}
```

---

### 2.7 按参数键名更新参数值

```
PUT /system/config/updateByKey
```

**认证**: 需要 Token

**请求体**:

```json
{
  "configKey": "sys.register",
  "configValue": "true"
}
```

---

### 2.8 刷新参数缓存

```
DELETE /system/config/refreshCache
```

**认证**: 需要 Token
**权限**: `system:config:remove`

---

### 2.9 导出参数数据

```
POST /system/config/export
```

**认证**: 需要 Token
**权限**: `system:config:export`

**响应**: Excel 文件流

---

## 3. 前端 API 封装

### 3.1 `@/api/system/config/index.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listConfig(query)` | GET | `/system/config/list` | 查询参数列表 |
| `getConfig(configId)` | GET | `/system/config/{configId}` | 查询参数详情 |
| `getConfigKey(configKey)` | GET | `/system/config/configKey/{configKey}` | 按键名查询值 |
| `addConfig(data)` | POST | `/system/config` | 新增参数 |
| `updateConfig(data)` | PUT | `/system/config` | 修改参数 |
| `updateConfigByKey(key, value)` | PUT | `/system/config/updateByKey` | 按键名更新 |
| `delConfig(configIds)` | DELETE | `/system/config/{configIds}` | 删除参数 |
| `refreshCache()` | DELETE | `/system/config/refreshCache` | 刷新缓存 |

### 3.2 类型定义（`@/api/system/config/types.ts`）

```typescript
export interface ConfigVO extends BaseEntity {
  configId: number | string;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  remark: string;
}

export interface ConfigForm {
  configId: number | string | undefined;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  remark: string;
}

export interface ConfigQuery extends PageQuery {
  configName: string;
  configKey: string;
  configType: string;
}
```

---

## 4. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
