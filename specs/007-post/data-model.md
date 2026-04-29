# 007-Post 岗位管理模块 - 数据模型

> 本文档定义岗位管理模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_post - 岗位信息表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| post_id | bigint(20) | NOT NULL | - | 岗位 ID（主键，雪花算法） |
| dept_id | bigint(20) | YES | NULL | 部门 ID |
| post_code | varchar(64) | NOT NULL | - | 岗位编码 |
| post_name | varchar(50) | NOT NULL | - | 岗位名称 |
| post_category | varchar(64) | YES | '' | 类别编码 |
| post_sort | int(11) | NOT NULL | 0 | 显示顺序 |
| status | char(1) | YES | '0' | 状态（0=正常, 1=停用） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `post_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_post_code | post_code | 唯一 | 岗位编码唯一 |
| idx_dept_id | dept_id | 普通 | 部门关联查询 |

---

### 1.2 sys_user_post - 用户与岗位关联表

| 列名 | 类型 | 可空 | 说明 |
|------|------|------|------|
| user_id | bigint(20) | NOT NULL | 用户 ID |
| post_id | bigint(20) | NOT NULL | 岗位 ID |

**主键**: `(user_id, post_id)`

**关系**: 用户 1-N 岗位（一个用户可担任多个岗位）

---

## 2. 实体关系图

```
+-------------+       +----------------+       +-------------+
|  sys_dept   |       |  sys_post      |       |  sys_user   |
+-------------+       +----------------+       +-------------+
| PK dept_id  |<------| FK dept_id     |       | PK user_id  |
|    dept_name|       | PK post_id     |       |    user_name|
|    status   |       |    post_code   |       |    nick_name|
+-------------+       |    post_name   |       +-------------+
                      |    post_sort   |              ^
                      |    status      |              |
                      +----------------+              |
                              ^                       |
                              |                       |
                      +----------------+              |
                      | sys_user_post  |--------------+
                      +----------------+
                      | FK user_id     |
                      | FK post_id     |
                      +----------------+
```

---

## 3. 后端对象

### 3.1 SysPost - 实体类 (Entity)

对应 `sys_post` 表，继承 `BaseEntity`。

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SysPost extends BaseEntity {
    private Long postId;
    private Long deptId;
    private String postCode;
    private String postName;
    private String postCategory;
    private Integer postSort;
    private String status;
    private String remark;
    private Long createDept;
}
```

### 3.2 PostVO - 视图对象

用于前端展示数据。

```java
@Data
public class PostVO {
    private Long postId;
    private Long deptId;
    private String postCode;
    private String postName;
    private String postCategory;
    private String deptName;      // 关联查询：部门名称
    private Integer postSort;
    private String status;
    private String remark;
    private Date createTime;
}
```

### 3.3 PostBo - 业务对象

用于新增/修改岗位时的数据传输。

```java
@Data
public class PostBo {
    private Long postId;
    private Long deptId;
    private String postCode;
    private String postName;
    private String postCategory;
    private Integer postSort;
    private String status;
    private String remark;
}
```

---

## 4. 前端类型

### 4.1 PostVO - 岗位返回对象

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
```

### 4.2 PostForm - 岗位表单对象

```typescript
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
```

### 4.3 PostQuery - 岗位查询对象

```typescript
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

## 5. 字段枚举值

### 5.1 岗位状态 (status)

| 值 | 含义 | 字典类型 |
|----|------|----------|
| '0' | 正常 | `sys_normal_disable` |
| '1' | 停用 | `sys_normal_disable` |

---

## 6. 数据流转

### 6.1 新增岗位

```
前端 PostForm
  → POST /system/post
    → Controller 接收
      → Service 校验 postCode 唯一性
      → Service 插入 sys_post
    → 返回 R.ok()
```

### 6.2 修改岗位

```
前端 PostForm
  → PUT /system/post
    → Controller 接收
      → Service 校验 postCode 唯一性（排除自身）
      → Service 更新 sys_post
    → 返回 R.ok()
```

### 6.3 删除岗位

```
前端 postIds (逗号分隔)
  → DELETE /system/post/{postIds}
    → Controller 接收
      → Service 校验是否被用户使用
      → Service 删除 sys_post
    → 返回 R.ok()
```

---

## 7. 多租户说明

- `sys_post` 表通过 `create_dept` 关联到部门，部门属于特定租户
- 岗位数据通过部门间接实现租户隔离
- 关联表 `sys_user_post` 不包含 `tenant_id`，通过 `user_id` 关联到用户表间接实现租户隔离
