# 角色管理数据模型文档 (data-model.md)

> magic-ruoyi 角色管理模块的数据模型定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 实体清单

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_role` | SysRole | `role_id` BIGINT | 角色信息表 |
| `sys_user_role` | - | `(user_id, role_id)` | 用户-角色关联表 |
| `sys_role_menu` | - | `(role_id, menu_id)` | 角色-菜单关联表 |
| `sys_role_dept` | - | `(role_id, dept_id)` | 角色-部门数据权限关联表 |

---

## 2. sys_role（角色信息表）

### 2.1 表结构

```sql
create table sys_role (
    role_id              bigint(20)      not null                   comment '角色ID',
    tenant_id            varchar(20)     default '000000'           comment '租户编号',
    role_name            varchar(30)     not null                   comment '角色名称',
    role_key             varchar(100)    not null                   comment '角色权限字符串',
    role_sort            int(4)          not null                   comment '显示顺序',
    data_scope           char(1)         default '1'                comment '数据范围',
    menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
    dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
    status               char(1)         not null                   comment '角色状态（0正常 1停用）',
    del_flag             char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    create_dept          bigint(20)      default null               comment '创建部门',
    create_by            bigint(20)      default null               comment '创建者',
    create_time          datetime                                   comment '创建时间',
    update_by            bigint(20)      default null               comment '更新者',
    update_time          datetime                                   comment '更新时间',
    remark               varchar(500)    default null               comment '备注',
    primary key (role_id)
) engine=innodb comment = '角色信息表';
```

### 2.2 字段说明

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| role_id | BIGINT(20) | 是 | - | 角色ID，雪花算法生成 |
| tenant_id | VARCHAR(20) | 是 | '000000' | 租户编号，用于多租户隔离 |
| role_name | VARCHAR(30) | 是 | - | 角色名称，租户内唯一 |
| role_key | VARCHAR(100) | 是 | - | 权限字符，用于代码权限校验，租户内唯一 |
| role_sort | INT(4) | 是 | - | 显示顺序，数值越小越靠前 |
| data_scope | CHAR(1) | 是 | '1' | 数据权限范围，见下方枚举 |
| menu_check_strictly | TINYINT(1) | 是 | 1 | 菜单树父子联动开关，1=联动，0=独立 |
| dept_check_strictly | TINYINT(1) | 是 | 1 | 部门树父子联动开关，1=联动，0=独立 |
| status | CHAR(1) | 是 | - | 角色状态，0=正常，1=停用 |
| del_flag | CHAR(1) | 是 | '0' | 逻辑删除标志，0=正常，1=已删除 |
| create_dept | BIGINT(20) | 否 | null | 创建者所属部门ID |
| create_by | BIGINT(20) | 否 | null | 创建者用户ID |
| create_time | DATETIME | 否 | - | 创建时间 |
| update_by | BIGINT(20) | 否 | null | 最后更新者用户ID |
| update_time | DATETIME | 否 | - | 最后更新时间 |
| remark | VARCHAR(500) | 否 | null | 备注信息 |

### 2.3 枚举值

**data_scope（数据权限范围）**:

| 值 | 含义 | SQL过滤条件 |
|----|------|-------------|
| 1 | 全部数据权限 | 无额外过滤 |
| 2 | 自定义数据权限 | `dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = ?)` |
| 3 | 本部门数据权限 | `dept_id = 当前用户部门ID` |
| 4 | 本部门及以下数据权限 | `dept_id IN (当前部门及所有子部门)` |
| 5 | 仅本人数据权限 | `user_id = 当前用户ID` |
| 6 | 部门及以下或本人数据权限 | `dept_id IN (当前部门及子部门) OR user_id = 当前用户ID` |

**status（角色状态）**:

| 值 | 含义 |
|----|------|
| 0 | 正常 |
| 1 | 停用 |

**del_flag（删除标志）**:

| 值 | 含义 |
|----|------|
| 0 | 正常 |
| 1 | 已删除 |

### 2.4 初始数据

```sql
insert into sys_role values(1, '000000', '超级管理员',  'superadmin',  1, 1, 1, 1, '0', '0', 103, 1, sysdate(), null, null, '超级管理员');
insert into sys_role values(3, '000000', '管理员', 'admin', 3, 4, 1, 1, '0', '0', 103, 1, sysdate(), null, null, '');
insert into sys_role values(4, '000000', '普通人员',      'user', 4, 5, 1, 1, '0', '0', 103, 1, sysdate(), null, null, '');
```

---

## 3. sys_user_role（用户-角色关联表）

### 3.1 表结构

```sql
create table sys_user_role (
    user_id   bigint(20) not null comment '用户ID',
    role_id   bigint(20) not null comment '角色ID',
    primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';
```

### 3.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| user_id | BIGINT(20) | 是 | 用户ID，外键引用 sys_user.user_id |
| role_id | BIGINT(20) | 是 | 角色ID，外键引用 sys_role.role_id |

### 3.3 关系说明

- 联合主键：(user_id, role_id)
- 多对多关系：一个用户可拥有多个角色，一个角色可分配给多个用户
- 无审计字段，无逻辑删除

### 3.4 初始数据

```sql
insert into sys_user_role values ('1', '1');   -- admin 拥有超级管理员角色
insert into sys_user_role values ('3', '4');   -- test1 拥有普通人员角色
insert into sys_user_role values ('4', '4');   -- test2 拥有普通人员角色
```

---

## 4. sys_role_menu（角色-菜单关联表）

### 4.1 表结构

```sql
create table sys_role_menu (
    role_id   bigint(20) not null comment '角色ID',
    menu_id   bigint(20) not null comment '菜单ID',
    primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';
```

### 4.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role_id | BIGINT(20) | 是 | 角色ID，外键引用 sys_role.role_id |
| menu_id | BIGINT(20) | 是 | 菜单ID，外键引用 sys_menu.menu_id |

### 4.3 关系说明

- 联合主键：(role_id, menu_id)
- 一对多关系：一个角色可关联多个菜单
- 关联的菜单包含目录（M）、菜单（C）、按钮（F）三种类型
- 无审计字段，无逻辑删除
- 不受租户隔离（全局共享表），通过主表 sys_role 的 tenant_id 间接隔离

---

## 5. sys_role_dept（角色-部门关联表）

### 5.1 表结构

```sql
create table sys_role_dept (
    role_id   bigint(20) not null comment '角色ID',
    dept_id   bigint(20) not null comment '部门ID',
    primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';
```

### 5.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role_id | BIGINT(20) | 是 | 角色ID，外键引用 sys_role.role_id |
| dept_id | BIGINT(20) | 是 | 部门ID，外键引用 sys_dept.dept_id |

### 5.3 关系说明

- 联合主键：(role_id, dept_id)
- 一对多关系：一个角色可关联多个部门（用于自定义数据权限）
- 仅当角色的 data_scope = '2'（自定义数据权限）时使用
- 无审计字段，无逻辑删除
- 不受租户隔离（全局共享表），通过主表 sys_role 的 tenant_id 间接隔离

---

## 6. 实体关系图

```
┌──────────────────────────────────────────────────────────────┐
│                        角色域                                  │
│                                                              │
│  sys_role                                                    │
│  ├── role_id (PK)                                            │
│  ├── tenant_id ──▶ 租户隔离                                   │
│  ├── role_name (租户内唯一)                                   │
│  ├── role_key (租户内唯一)                                    │
│  ├── data_scope ──▶ 数据权限范围 (1-6)                        │
│  ├── status (0=正常, 1=停用)                                  │
│  └── del_flag (0=正常, 1=删除)                                │
│       │                                                      │
│       │ role_id                                              │
│       ├──────▶ sys_user_role ◀────── sys_user                │
│       │                    │                                 │
│       │                    └── 用户拥有多个角色                │
│       │                                                      │
│       │ role_id                                              │
│       ├──────▶ sys_role_menu ◀────── sys_menu                │
│       │                    │                                 │
│       │                    └── 角色拥有多个菜单/按钮权限       │
│       │                                                      │
│       │ role_id                                              │
│       └──────▶ sys_role_dept ◀────── sys_dept                │
│                            │                                 │
│                            └── 自定义数据权限的部门范围        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 7. 前端类型定义

### 7.1 RoleVO（角色视图对象）

```typescript
interface RoleVO extends BaseEntity {
  roleId: string | number;          // 角色ID
  roleName: string;                 // 角色名称
  roleKey: string;                  // 权限字符
  roleSort: number;                 // 显示顺序
  dataScope: string;                // 数据权限范围
  menuCheckStrictly: boolean;       // 菜单树父子联动
  deptCheckStrictly: boolean;       // 部门树父子联动
  status: string;                   // 状态
  delFlag: string;                  // 删除标志
  remark?: any;                     // 备注
  flag: boolean;                    // 用户是否已分配该角色
  menuIds?: Array<string | number>; // 菜单ID列表
  deptIds?: Array<string | number>; // 部门ID列表
  admin: boolean;                   // 是否为超级管理员
}
```

### 7.2 RoleQuery（角色查询参数）

```typescript
interface RoleQuery extends PageQuery {
  roleName: string;    // 角色名称
  roleKey: string;     // 权限字符
  status: string;      // 状态
}
```

### 7.3 RoleForm（角色表单对象）

```typescript
interface RoleForm {
  roleName: string;                 // 角色名称
  roleKey: string;                  // 权限字符
  roleSort: number;                 // 显示顺序
  status: string;                   // 状态
  menuCheckStrictly: boolean;       // 菜单树父子联动
  deptCheckStrictly: boolean;       // 部门树父子联动
  remark: string;                   // 备注
  dataScope?: string;               // 数据权限范围
  roleId: string | undefined;       // 角色ID（编辑时）
  menuIds: Array<string | number>;  // 菜单ID列表
  deptIds: Array<string | number>;  // 部门ID列表
}
```

### 7.4 RoleDeptTree（角色部门树响应）

```typescript
interface DeptTreeOption {
  id: string;
  label: string;
  parentId: string;
  weight: number;
  children?: DeptTreeOption[];
}

interface RoleDeptTree {
  checkedKeys: string[];       // 已勾选的部门ID
  depts: DeptTreeOption[];     // 部门树数据
}
```

---

## 8. 审计字段

sys_role 表包含完整的审计字段，由 MyBatis-Plus MetaObjectHandler 自动填充：

| 字段 | 填充时机 | 数据来源 |
|------|----------|----------|
| create_dept | INSERT | 当前用户所属部门ID |
| create_by | INSERT | 当前用户ID |
| create_time | INSERT | 当前时间 |
| update_by | UPDATE | 当前用户ID |
| update_time | UPDATE | 当前时间 |

关联表（sys_user_role、sys_role_menu、sys_role_dept）不包含审计字段。

---

## 9. 多租户隔离

| 表 | tenant_id | 隔离方式 |
|----|-----------|----------|
| sys_role | 有 | 直接隔离，MyBatis-Plus 租户插件自动过滤 |
| sys_user_role | 无 | 通过 sys_role.tenant_id 间接隔离 |
| sys_role_menu | 无 | 通过 sys_role.tenant_id 间接隔离 |
| sys_role_dept | 无 | 通过 sys_role.tenant_id 间接隔离 |

---

## 10. 索引设计

| 表 | 索引类型 | 字段 | 用途 |
|----|----------|------|------|
| sys_role | 主键索引 | role_id | 主键查询 |
| sys_role | 唯一索引（建议） | (tenant_id, role_name) | 租户内角色名称唯一 |
| sys_role | 唯一索引（建议） | (tenant_id, role_key) | 租户内权限字符唯一 |
| sys_user_role | 主键索引 | (user_id, role_id) | 联合主键 |
| sys_user_role | 普通索引 | role_id | 按角色查询用户 |
| sys_role_menu | 主键索引 | (role_id, menu_id) | 联合主键 |
| sys_role_menu | 普通索引 | menu_id | 按菜单查询角色 |
| sys_role_dept | 主键索引 | (role_id, dept_id) | 联合主键 |
| sys_role_dept | 普通索引 | dept_id | 按部门查询角色 |

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，角色管理数据模型定义 |
