# 003-User 用户管理模块 - 数据模型

> 本文档定义用户管理模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_user - 用户信息表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| user_id | bigint(20) | NOT NULL | - | 用户ID（主键，雪花算法） |
| tenant_id | varchar(20) | YES | '000000' | 租户编号 |
| dept_id | bigint(20) | YES | NULL | 部门ID |
| user_name | varchar(30) | NOT NULL | - | 用户账号 |
| nick_name | varchar(30) | NOT NULL | - | 用户昵称 |
| user_type | varchar(10) | YES | 'sys_user' | 用户类型（sys_user=系统用户） |
| email | varchar(50) | YES | '' | 用户邮箱 |
| phonenumber | varchar(11) | YES | '' | 手机号码 |
| sex | char(1) | YES | '0' | 用户性别（0=男, 1=女, 2=未知） |
| avatar | bigint(20) | YES | NULL | 头像地址（OSS文件ID） |
| password | varchar(100) | YES | '' | 密码（BCrypt 加密） |
| status | char(1) | YES | '0' | 帐号状态（0=正常, 1=停用） |
| del_flag | char(1) | YES | '0' | 删除标志（0=存在, 1=删除） |
| login_ip | varchar(128) | YES | '' | 最后登录IP |
| login_date | datetime | YES | NULL | 最后登录时间 |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `user_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_user_name | user_name | 唯一 | 用户账号唯一 |
| idx_tenant_dept | tenant_id, dept_id | 普通 | 租户+部门查询 |
| idx_status | status | 普通 | 状态过滤 |

**约束**:

- `user_name` 在租户内唯一
- `email` 在租户内唯一（非空时）
- `phonenumber` 在租户内唯一（非空时）

---

### 1.2 sys_user_role - 用户和角色关联表

| 列名 | 类型 | 可空 | 说明 |
|------|------|------|------|
| user_id | bigint(20) | NOT NULL | 用户ID |
| role_id | bigint(20) | NOT NULL | 角色ID |

**主键**: `(user_id, role_id)`

**关系**: 用户 N-1 角色（一个用户可拥有多个角色）

---

### 1.3 sys_user_post - 用户与岗位关联表

| 列名 | 类型 | 可空 | 说明 |
|------|------|------|------|
| user_id | bigint(20) | NOT NULL | 用户ID |
| post_id | bigint(20) | NOT NULL | 岗位ID |

**主键**: `(user_id, post_id)`

**关系**: 用户 1-N 岗位（一个用户可担任多个岗位）

---

## 2. 实体关系图

```
+-------------+       +----------------+       +-------------+
|  sys_user   |       | sys_user_role  |       |  sys_role   |
+-------------+       +----------------+       +-------------+
| PK user_id  |<----+ | FK user_id     |    +->| PK role_id  |
|    dept_id  |--+   | PK role_id     |    |  |    role_name|
|    user_name|  |   +----------------+    |  |    role_key |
|    nick_name|  |                         |  |    status   |
|    password |  |   +----------------+    |  +-------------+
|    status   |  |   | sys_user_post  |    |
|    del_flag |  |   +----------------+    |  +-------------+
|    ...      |  +-->| FK user_id     |    |  |  sys_post   |
+-------------+      | PK post_id     |    +->+-------------+
                     +----------------+       | PK post_id  |
                                              |    post_code|
                     +-------------+          |    post_name|
                     |  sys_dept   |          |    status   |
                     +-------------+          +-------------+
                     | PK dept_id  |
                     |    dept_name|
                     |    status   |
                     +-------------+
```

---

## 3. 后端对象

### 3.1 SysUser - 实体类 (Entity)

对应 `sys_user` 表，继承 `BaseEntity`。

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private Long userId;
    private String tenantId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String userType;
    private String email;
    private String phonenumber;
    private String sex;
    private Long avatar;
    private String password;
    private String status;
    private String delFlag;
    private String loginIp;
    private Date loginDate;
    private Long createDept;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    private String remark;
}
```

### 3.2 UserVO - 视图对象

用于前端展示数据。

```java
@Data
public class UserVO {
    private Long userId;
    private String tenantId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String userType;
    private String email;
    private String phonenumber;
    private String sex;
    private String avatar;
    private String status;
    private String delFlag;
    private String loginIp;
    private Date loginDate;
    private String remark;
    private String deptName;
    private List<RoleVO> roles;
    private Object roleIds;
    private Object postIds;
    private Object roleId;
    private boolean admin;
    private Date createTime;
}
```

### 3.3 UserBo - 业务对象

用于新增/修改用户时的数据传输。

```java
@Data
public class UserBo {
    private Long userId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String password;
    private String phonenumber;
    private String email;
    private String sex;
    private String status;
    private String remark;
    private Long[] roleIds;
    private Long[] postIds;
}
```

### 3.4 UserInfoVO - 用户详情视图对象

用于用户详情接口返回。

```java
@Data
public class UserInfoVO {
    private SysUser user;
    private List<RoleVO> roles;
    private Long[] roleIds;
    private List<PostVO> posts;
    private Long[] postIds;
    private String roleGroup;
    private String postGroup;
}
```

---

## 4. 前端类型

### 4.1 UserVO - 用户返回对象

```typescript
export interface UserVO extends BaseEntity {
  userId: string | number;
  tenantId: string;
  deptId: number;
  userName: string;
  nickName: string;
  userType: string;
  email: string;
  phonenumber: string;
  sex: string;
  avatar: string;
  status: string;
  delFlag: string;
  loginIp: string;
  loginDate: string;
  remark: string;
  deptName: string;
  roles: RoleVO[];
  roleIds: any;
  postIds: any;
  roleId: any;
  admin: boolean;
}
```

### 4.2 UserQuery - 用户查询对象

```typescript
export interface UserQuery extends PageQuery {
  userName?: string;
  nickName?: string;
  phonenumber?: string;
  status?: string;
  deptId?: string | number;
  roleId?: string | number;
  userIds?: string | number | (string | number)[] | undefined;
}
```

### 4.3 UserForm - 用户表单对象

```typescript
export interface UserForm {
  id?: string;
  userId?: string;
  deptId?: number;
  userName: string;
  nickName?: string;
  password: string;
  phonenumber?: string;
  email?: string;
  sex?: string;
  status: string;
  remark?: string;
  postIds: string[];
  roleIds: string[];
}
```

### 4.4 UserInfoVO - 用户详情视图对象

```typescript
export interface UserInfoVO {
  user: UserVO;
  roles: RoleVO[];
  roleIds: string[];
  posts: PostVO[];
  postIds: string[];
  roleGroup: string;
  postGroup: string;
}
```

### 4.5 UserInfo - 登录用户信息

```typescript
export interface UserInfo {
  user: UserVO;
  roles: string[];
  permissions: string[];
}
```

### 4.6 ResetPwdForm - 密码修改表单

```typescript
export interface ResetPwdForm {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}
```

---

## 5. 字段枚举值

### 5.1 用户状态 (status)

| 值 | 含义 | 字典类型 |
|----|------|----------|
| '0' | 正常 | `sys_normal_disable` |
| '1' | 停用 | `sys_normal_disable` |

### 5.2 用户性别 (sex)

| 值 | 含义 | 字典类型 |
|----|------|----------|
| '0' | 男 | `sys_user_sex` |
| '1' | 女 | `sys_user_sex` |
| '2' | 未知 | `sys_user_sex` |

### 5.3 删除标志 (del_flag)

| 值 | 含义 |
|----|------|
| '0' | 存在 |
| '1' | 已删除 |

### 5.4 用户类型 (user_type)

| 值 | 含义 |
|----|------|
| 'sys_user' | 系统用户 |

---

## 6. 数据流转

### 6.1 新增用户

```
前端 UserForm
  → POST /system/user (RSA 加密 password)
    → Controller 接收
      → Service 校验 userName 唯一性
      → Service 加密 password (BCrypt)
      → Service 插入 sys_user
      → Service 批量插入 sys_user_role
      → Service 批量插入 sys_user_post
    → 返回 R.ok()
```

### 6.2 修改用户

```
前端 UserForm
  → PUT /system/user
    → Controller 接收
      → Service 校验 userName 唯一性（排除自身）
      → Service 更新 sys_user
      → Service 删除旧 sys_user_role，插入新关联
      → Service 删除旧 sys_user_post，插入新关联
    → 返回 R.ok()
```

### 6.3 删除用户

```
前端 userIds (逗号分隔)
  → DELETE /system/user/{userIds}
    → Controller 接收
      → Service 校验非超级管理员
      → Service 逻辑删除 sys_user (del_flag='1')
      → Service 删除 sys_user_role 关联
      → Service 删除 sys_user_post 关联
    → 返回 R.ok()
```

### 6.4 密码重置

```
前端 { userId, password }
  → PUT /system/user/resetPwd (RSA 加密)
    → Controller 接收并解密
      → Service 加密 password (BCrypt)
      → Service 更新 sys_user.password
    → 返回 R.ok()
```

---

## 7. 多租户说明

- `sys_user` 表包含 `tenant_id` 字段
- 通过 MyBatis-Plus 租户插件自动过滤
- 默认租户 ID: `000000`
- 关联表 `sys_user_role` 和 `sys_user_post` 不包含 `tenant_id`，通过 `user_id` 关联到用户表间接实现租户隔离
