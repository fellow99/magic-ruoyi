# 通知公告模块 API 文档（010-notice/api.md）

> magic-ruoyi 通知公告模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| 模块前缀 | `/system/notice` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 公告接口

### 2.1 查询公告列表

```
GET /system/notice/list
```

**认证**: 需要 Token
**权限**: `system:notice:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| noticeTitle | String | 否 | 公告标题（模糊匹配） |
| createByName | String | 否 | 创建者名称 |
| status | String | 否 | 状态（0=正常, 1=关闭） |
| noticeType | String | 否 | 公告类型（1=通知, 2=公告） |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "noticeId": 1,
      "noticeTitle": "系统维护通知",
      "noticeType": "1",
      "noticeContent": "<p>系统将于...</p>",
      "status": "0",
      "createByName": "admin",
      "remark": "",
      "createTime": "2026-01-01 00:00:00"
    }
  ],
  "total": 1
}
```

---

### 2.2 查询公告详情

```
GET /system/notice/{noticeId}
```

**认证**: 需要 Token
**权限**: `system:notice:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| noticeId | Long | 是 | 公告 ID |

---

### 2.3 新增公告

```
POST /system/notice
```

**认证**: 需要 Token
**权限**: `system:notice:add`

**请求体**:

```json
{
  "noticeTitle": "系统维护通知",
  "noticeType": "1",
  "noticeContent": "<p>系统将于今晚进行维护...</p>",
  "status": "0",
  "remark": ""
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| noticeTitle | String | 是 | 公告标题 |
| noticeType | String | 是 | 公告类型 |
| noticeContent | String | 否 | 公告内容（HTML） |
| status | String | 否 | 状态，默认 "0" |
| remark | String | 否 | 备注 |

---

### 2.4 修改公告

```
PUT /system/notice
```

**认证**: 需要 Token
**权限**: `system:notice:edit`

**请求体**:

```json
{
  "noticeId": 1,
  "noticeTitle": "系统维护通知（更新）",
  "noticeType": "1",
  "noticeContent": "<p>更新后的内容...</p>",
  "status": "0",
  "remark": ""
}
```

---

### 2.5 删除公告

```
DELETE /system/notice/{noticeIds}
```

**认证**: 需要 Token
**权限**: `system:notice:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| noticeIds | String | 是 | 公告 ID，多个用逗号分隔 |

---

## 3. 前端 API 封装

### 3.1 `@/api/system/notice/index.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listNotice(query)` | GET | `/system/notice/list` | 查询公告列表 |
| `getNotice(noticeId)` | GET | `/system/notice/{noticeId}` | 查询公告详情 |
| `addNotice(data)` | POST | `/system/notice` | 新增公告 |
| `updateNotice(data)` | PUT | `/system/notice` | 修改公告 |
| `delNotice(noticeIds)` | DELETE | `/system/notice/{noticeIds}` | 删除公告 |

### 3.2 类型定义（`@/api/system/notice/types.ts`）

```typescript
export interface NoticeVO extends BaseEntity {
  noticeId: number;
  noticeTitle: string;
  noticeType: string;
  noticeContent: string;
  status: string;
  remark: string;
  createByName: string;
}

export interface NoticeForm {
  noticeId: number | string | undefined;
  noticeTitle: string;
  noticeType: string;
  noticeContent: string;
  status: string;
  remark: string;
  createByName: string;
}

export interface NoticeQuery extends PageQuery {
  noticeTitle: string;
  createByName: string;
  status: string;
  noticeType: string;
}
```

---

## 4. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
