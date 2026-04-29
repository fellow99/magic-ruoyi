# 010-Notice 通知公告模块 - 数据模型

> 本文档定义通知公告模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_notice - 通知公告表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| notice_id | bigint(20) | NOT NULL | - | 公告 ID（主键，雪花算法） |
| notice_title | varchar(50) | NOT NULL | - | 公告标题 |
| notice_type | char(1) | NOT NULL | - | 公告类型（1=通知, 2=公告） |
| notice_content | longblob | YES | NULL | 公告内容（富文本 HTML） |
| status | char(1) | YES | '0' | 状态（0=正常, 1=关闭） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(255) | YES | NULL | 备注 |

**主键**: `notice_id`

---

## 2. 实体关系图

```
+------------------+
|   sys_notice     |
+------------------+
| PK notice_id     |
|    notice_title  |
|    notice_type   |
|    notice_content|
|    status        |
|    create_by     |
|    remark        |
+------------------+
```

---

## 3. 后端对象

### 3.1 SysNotice - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {
    private Long noticeId;
    private String noticeTitle;
    private String noticeType;
    private String noticeContent;
    private String status;
    private String remark;
}
```

### 3.2 NoticeVO - 视图对象

```java
@Data
public class NoticeVO {
    private Long noticeId;
    private String noticeTitle;
    private String noticeType;
    private String noticeContent;
    private String status;
    private String createByName;  // 关联查询：创建者名称
    private String remark;
    private Date createTime;
}
```

---

## 4. 前端类型

### 4.1 NoticeVO - 公告返回对象

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
```

### 4.2 NoticeForm - 公告表单对象

```typescript
export interface NoticeForm {
  noticeId: number | string | undefined;
  noticeTitle: string;
  noticeType: string;
  noticeContent: string;
  status: string;
  remark: string;
  createByName: string;
}
```

### 4.3 NoticeQuery - 公告查询对象

```typescript
export interface NoticeQuery extends PageQuery {
  noticeTitle: string;
  createByName: string;
  status: string;
  noticeType: string;
}
```

---

## 5. 字段枚举值

### 5.1 公告类型 (notice_type)

| 值 | 含义 | 字典类型 |
|----|------|----------|
| '1' | 通知 | `sys_notice_type` |
| '2' | 公告 | `sys_notice_type` |

### 5.2 公告状态 (status)

| 值 | 含义 | 字典类型 |
|----|------|----------|
| '0' | 正常 | `sys_notice_status` |
| '1' | 关闭 | `sys_notice_status` |

---

## 6. 数据流转

### 6.1 新增公告

```
前端 NoticeForm
  → POST /system/notice
    → Service 插入 sys_notice
    → 自动填充 createByName
  → 返回 R.ok()
```

### 6.2 修改公告

```
前端 NoticeForm
  → PUT /system/notice
    → Service 更新 sys_notice
  → 返回 R.ok()
```

### 6.3 删除公告

```
前端 noticeIds
  → DELETE /system/notice/{noticeIds}
    → Service 删除 sys_notice
  → 返回 R.ok()
```
