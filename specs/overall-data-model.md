# 整体数据模型 (Overall Data Model)

> 基于 RuoYi-Vue-Plus 5.6.0 + magic-api 2.2.2 的数据模型规范。

---

## 1. 实体清单

### 1.1 用户与权限域

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_user` | SysUser | `user_id` BIGINT | 用户信息表 |
| `sys_role` | SysRole | `role_id` BIGINT | 角色信息表 |
| `sys_menu` | SysMenu | `menu_id` BIGINT | 菜单权限表（目录/菜单/按钮） |
| `sys_dept` | SysDept | `dept_id` BIGINT | 部门表（树形结构） |
| `sys_post` | SysPost | `post_id` BIGINT | 岗位信息表 |

### 1.2 租户域

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_tenant` | SysTenant | `id` BIGINT | 租户表 |
| `sys_tenant_package` | SysTenantPackage | `package_id` BIGINT | 租户套餐表 |

### 1.3 字典与参数域

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_dict_type` | SysDictType | `dict_id` BIGINT | 字典类型表 |
| `sys_dict_data` | SysDictData | `dict_code` BIGINT | 字典数据表 |
| `sys_config` | SysConfig | `config_id` BIGINT | 系统参数配置表 |

### 1.4 日志域

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_oper_log` | SysOperLog | `oper_id` BIGINT | 操作日志记录 |
| `sys_logininfor` | SysLogininfor | `info_id` BIGINT | 系统访问记录（登录日志） |

### 1.5 文件存储域

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_oss` | SysOss | `oss_id` BIGINT | OSS 对象存储表 |
| `sys_oss_config` | SysOssConfig | `oss_config_id` BIGINT | OSS 对象存储配置表 |

### 1.6 客户端与社交登录域

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_client` | SysClient | `id` BIGINT | OAuth 客户端授权表 |
| `sys_social` | SysSocial | `id` BIGINT | 第三方平台社会化关系表 |

### 1.7 关联表

| 表名 | 联合主键 | 描述 |
|------|----------|------|
| `sys_user_role` | `(user_id, role_id)` | 用户-角色关联（N:M） |
| `sys_role_menu` | `(role_id, menu_id)` | 角色-菜单关联（1:N） |
| `sys_role_dept` | `(role_id, dept_id)` | 角色-部门数据权限关联（1:N） |
| `sys_user_post` | `(user_id, post_id)` | 用户-岗位关联（1:N） |

### 1.8 其他系统表

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_notice` | SysNotice | `notice_id` BIGINT | 通知公告表 |
| `gen_table` | GenTable | `table_id` BIGINT | 代码生成业务表 |
| `gen_table_column` | GenTableColumn | `column_id` BIGINT | 代码生成业务表字段 |

### 1.9 Magic API 数据模型

magic-api 的资源存储支持两种模式，数据模型如下：

| 资源类型 | 存储方式 | 描述 |
|----------|----------|------|
| 接口定义 | 数据库表 `magic_api_file` 或文件系统 | 动态 API 接口脚本、路径、方法、参数定义 |
| 函数定义 | 同上 | 可复用函数脚本 |
| 数据源配置 | 同上 | 多数据源连接配置 |
| 定时任务 | 同上 | 定时执行的脚本任务 |
| 全局配置 | 同上 | 全局变量、常量定义 |

> magic-api 默认将资源以 JSON 格式存储在数据库表 `magic_api_file` 中，也可配置为文件系统存储。每条记录包含 `path`（资源路径）、`name`（资源名称）、`type`（资源类型）、`script`（脚本内容）、`create_time`、`update_time` 等字段。

---

## 2. 实体关系图

### 2.1 核心关系（文字描述）

```
┌─────────────────────────────────────────────────────────────────┐
│                        租户域                                    │
│                                                                 │
│  sys_tenant ──(package_id)──▶ sys_tenant_package                │
│       │                                                           │
│       │ tenant_id (逻辑隔离所有业务表)                             │
│       ▼                                                           │
└───────┼───────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────┐
│                      用户与权限域                                 │
│                                                                 │
│  sys_dept ◀──(parent_id 自引用树)── sys_dept                    │
│       │                                                         │
│       │ dept_id                                                 │
│       ▼                                                         │
│  sys_user ──(dept_id)──▶ sys_dept                               │
│       │                                                         │
│       │ user_id                                                 │
│       ├──────▶ sys_user_role ◀────── sys_role                   │
│       │                              │                          │
│       │                              │ role_id                  │
│       │                              ├──────▶ sys_role_menu ◀──▶│
│       │                              │                    sys_menu
│       │                              │                          │
│       │                              └──────▶ sys_role_dept ◀──▶│
│       │                                                   sys_dept
│       │                                                         │
│       └──────▶ sys_user_post ◀────── sys_post                   │
│                          │                                      │
│                          └──(dept_id)──▶ sys_dept               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     客户端与社交登录域                             │
│                                                                 │
│  sys_client (OAuth 客户端配置)                                   │
│       │                                                         │
│       │ 认证流程                                                 │
│       ▼                                                         │
│  sys_social ──(user_id)──▶ sys_user                             │
│       │                                                         │
│       └── auth_id = source + open_id (唯一标识)                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       文件存储域                                  │
│                                                                 │
│  sys_oss_config (存储配置: MinIO/S3/阿里云等)                     │
│       │                                                         │
│       │ service 字段关联                                         │
│       ▼                                                         │
│  sys_oss (文件记录, url 指向实际文件)                             │
│                                                                 │
│  sys_user.avatar ──(BIGINT)──▶ sys_oss.oss_id                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        日志域                                     │
│                                                                 │
│  sys_oper_log (操作日志, 记录用户操作行为)                         │
│       │                                                         │
│       └── oper_name 关联 sys_user.user_name                      │
│                                                                 │
│  sys_logininfor (登录日志, 记录登录成功/失败)                      │
│       │                                                         │
│       └── user_name 关联 sys_user.user_name                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     Magic API 资源模型                            │
│                                                                 │
│  magic_api_file (或文件系统)                                     │
│       │                                                         │
│       ├── type = 'api'     → 接口定义                            │
│       ├── type = 'function' → 函数定义                           │
│       ├── type = 'datasource' → 数据源配置                       │
│       ├── type = 'task'    → 定时任务                            │
│       └── type = 'config'  → 全局配置                            │
│                                                                 │
│  接口可引用 sys_config / sys_dict_data 作为参数                  │
│  接口可使用 sys_oss 进行文件上传下载                              │
│  接口受 sys_menu 权限标识 (perms) 控制                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 菜单树形结构

```
sys_menu (自引用树, parent_id → menu_id)

M (目录) ──▶ C (菜单) ──▶ F (按钮)
  │            │            │
  │            │            └── perms: 权限标识
  │            │                如 "system:user:add"
  │            │
  │            └── component: Vue 组件路径
  │                path: 路由地址
  │
  └── 可嵌套多层目录
```

### 2.3 部门树形结构

```
sys_dept (自引用树, parent_id → dept_id)

根机构 (dept_id=100)
  ├── 部门 A (dept_id=102)
  │     ├── 子部门 A1
  │     └── 子部门 A2
  └── 部门 B (dept_id=108)
        └── 子部门 B1

ancestors 字段存储祖级列表, 如 "0,100,102"
```

---

## 3. 审计字段设计

### 3.1 标准审计字段模板

所有业务实体表统一采用以下审计字段规范：

| 字段名 | 类型 | 说明 | 填充时机 |
|--------|------|------|----------|
| `create_dept` | BIGINT(20) | 创建部门 | 记录创建时自动填充当前用户所属部门 |
| `create_by` | BIGINT(20) | 创建者 | 记录创建时自动填充当前用户 ID |
| `create_time` | DATETIME | 创建时间 | 记录创建时自动填充当前时间 |
| `update_by` | BIGINT(20) | 更新者 | 记录每次更新时自动填充当前用户 ID |
| `update_time` | DATETIME | 更新时间 | 记录每次更新时自动填充当前时间 |

### 3.2 审计字段覆盖范围

| 表名 | create_dept | create_by | create_time | update_by | update_time | 备注 |
|------|:-----------:|:---------:|:-----------:|:---------:|:-----------:|------|
| `sys_tenant` | ✅ | ✅ | ✅ | ✅ | ✅ | 租户管理 |
| `sys_tenant_package` | ✅ | ✅ | ✅ | ✅ | ✅ | 租户套餐 |
| `sys_dept` | ✅ | ✅ | ✅ | ✅ | ✅ | 部门管理 |
| `sys_user` | ✅ | ✅ | ✅ | ✅ | ✅ | 用户管理 |
| `sys_post` | ✅ | ✅ | ✅ | ✅ | ✅ | 岗位管理 |
| `sys_role` | ✅ | ✅ | ✅ | ✅ | ✅ | 角色管理 |
| `sys_menu` | ✅ | ✅ | ✅ | ✅ | ✅ | 菜单管理 |
| `sys_dict_type` | ✅ | ✅ | ✅ | ✅ | ✅ | 字典类型 |
| `sys_dict_data` | ✅ | ✅ | ✅ | ✅ | ✅ | 字典数据 |
| `sys_config` | ✅ | ✅ | ✅ | ✅ | ✅ | 系统参数 |
| `sys_notice` | ✅ | ✅ | ✅ | ✅ | ✅ | 通知公告 |
| `sys_oss` | ✅ | ✅ | ✅ | ✅ | ✅ | 文件存储 |
| `sys_oss_config` | ✅ | ✅ | ✅ | ✅ | ✅ | OSS 配置 |
| `sys_client` | ✅ | ✅ | ✅ | ✅ | ✅ | OAuth 客户端 |
| `sys_social` | ✅ | ✅ | ✅ | ✅ | ✅ | 社交登录 |
| `gen_table` | ✅ | ✅ | ✅ | ✅ | ✅ | 代码生成 |
| `gen_table_column` | ✅ | ✅ | ✅ | ✅ | ✅ | 代码生成字段 |
| `sys_oper_log` | ❌ | ❌ | ❌ | ❌ | ❌ | 日志表, 仅 `oper_time` |
| `sys_logininfor` | ❌ | ❌ | ❌ | ❌ | ❌ | 日志表, 仅 `login_time` |
| `sys_user_role` | ❌ | ❌ | ❌ | ❌ | ❌ | 关联表 |
| `sys_role_menu` | ❌ | ❌ | ❌ | ❌ | ❌ | 关联表 |
| `sys_role_dept` | ❌ | ❌ | ❌ | ❌ | ❌ | 关联表 |
| `sys_user_post` | ❌ | ❌ | ❌ | ❌ | ❌ | 关联表 |

### 3.3 审计字段自动填充机制

通过 MyBatis-Plus `MetaObjectHandler` 实现自动填充：

- **插入时**：自动设置 `create_dept`、`create_by`、`create_time`
- **更新时**：自动设置 `update_by`、`update_time`
- **来源**：从 Sa-Token 当前登录用户上下文获取用户 ID 和部门 ID

### 3.4 逻辑删除字段

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `del_flag` | CHAR(1) | `'0'` | 删除标志：`0` = 正常，`1` = 已删除 |

覆盖表：`sys_tenant`、`sys_tenant_package`、`sys_dept`、`sys_user`、`sys_post`、`sys_role`、`sys_client`、`sys_social`、`test_demo`、`test_tree`

> 日志表（`sys_oper_log`、`sys_logininfor`）和关联表不使用逻辑删除。

---

## 4. 多租户数据隔离

### 4.1 隔离策略

采用 **行级逻辑隔离** 模式（共享数据库、共享 Schema、按 `tenant_id` 行级过滤）。

### 4.2 租户编号规则

| 属性 | 值 |
|------|-----|
| 字段名 | `tenant_id` |
| 类型 | VARCHAR(20) |
| 默认值 | `'000000'` |
| 说明 | 超级租户（主户）固定为 `000000` |

### 4.3 租户字段覆盖范围

以下表包含 `tenant_id` 字段，受租户隔离约束：

| 域 | 表 |
|----|-----|
| 租户 | `sys_tenant` |
| 组织 | `sys_dept`, `sys_user`, `sys_post`, `sys_role` |
| 字典参数 | `sys_dict_type`, `sys_dict_data`, `sys_config` |
| 日志 | `sys_oper_log`, `sys_logininfor` |
| 文件 | `sys_oss`, `sys_oss_config` |
| 通知 | `sys_notice` |
| 社交 | `sys_social` |
| 测试 | `test_demo`, `test_tree` |

**不包含 `tenant_id` 的表**（全局共享）：

| 表 | 原因 |
|-----|------|
| `sys_menu` | 菜单全局共享, 通过租户套餐 (`menu_ids`) 控制可见性 |
| `sys_tenant_package` | 套餐定义全局共享 |
| `sys_client` | OAuth 客户端全局共享 |
| `sys_user_role`, `sys_role_menu`, `sys_role_dept`, `sys_user_post` | 关联表, 通过主表 `tenant_id` 间接隔离 |
| `gen_table`, `gen_table_column` | 代码生成器全局共享 |

### 4.4 自动租户过滤

通过 MyBatis-Plus `TenantLineInnerInterceptor` 实现：

1. **SQL 拦截**：自动在 SELECT/UPDATE/DELETE 语句中追加 `WHERE tenant_id = ?` 条件
2. **INSERT 填充**：自动在 INSERT 语句中注入当前租户的 `tenant_id`
3. **忽略表**：通过配置指定哪些表不参与租户过滤（如 `sys_menu`、`sys_tenant_package`）

### 4.5 租户套餐权限控制

```
sys_tenant.package_id ──▶ sys_tenant_package.menu_ids (逗号分隔的菜单ID列表)
                              │
                              ▼
                        控制该租户用户可访问的菜单范围
```

- `menu_ids`：存储该套餐允许访问的菜单 ID 列表（逗号分隔）
- `menu_check_strictly`：菜单树选择是否关联显示父子节点
- 用户实际权限 = 租户套餐菜单 AND 角色分配菜单

### 4.6 租户生命周期

```
创建租户 → 分配套餐 → 生成 tenant_id → 初始化租户数据 → 激活
    │
    ├── 过期处理: expire_time 到期后 status 自动停用
    ├── 用户限额: account_count 控制最大用户数 (-1 不限制)
    └── 删除: del_flag = '1' 逻辑删除
```

---

## 5. 关键业务规则

### 5.1 用户认证链路

```
用户登录请求
    │
    ├── 1. 验证码校验 (Redis)
    ├── 2. 租户校验 (tenant_id 匹配 + status = '0')
    ├── 3. 客户端校验 (sys_client: client_key + client_secret)
    ├── 4. 用户校验 (sys_user: username + password + status = '0')
    ├── 5. 社交登录 (sys_social: auth_id 匹配 → 关联 sys_user)
    │
    └── 认证成功 → 生成 Sa-Token → 返回 token
```

### 5.2 权限校验链路

```
请求到达 → Sa-Token 拦截
    │
    ├── 1. Token 有效性校验
    ├── 2. 租户有效性校验
    ├── 3. 菜单权限校验 (perms 标识匹配 sys_menu.perms)
    │       │
    │       └── 用户 → sys_user_role → sys_role → sys_role_menu → sys_menu
    │
    ├── 4. 数据权限校验 (role.data_scope)
    │       │
    │       ├── 1: 全部数据
    │       ├── 2: 自定义数据 (sys_role_dept 指定部门)
    │       ├── 3: 本部门数据
    │       ├── 4: 本部门及以下数据
    │       ├── 5: 仅本人数据
    │       └── 6: 部门及以下或本人数据
    │
    └── 5. 按钮权限校验 (前端 v-hasPermi 指令)
```

### 5.3 数据范围 (Data Scope)

角色的 `data_scope` 字段控制该角色用户可查询的数据范围：

| 值 | 含义 | SQL 过滤条件 |
|----|------|-------------|
| `1` | 全部数据权限 | 无额外过滤 |
| `2` | 自定义数据权限 | `dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = ?)` |
| `3` | 本部门数据权限 | `dept_id = 当前用户部门ID` |
| `4` | 本部门及以下数据权限 | `dept_id IN (当前部门及所有子部门)` |
| `5` | 仅本人数据权限 | `user_id = 当前用户ID` |
| `6` | 部门及以下或本人数据权限 | `dept_id IN (当前部门及子部门) OR user_id = 当前用户ID` |

---

## 6. 索引设计

### 6.1 主键索引

所有表均使用 BIGINT 类型主键，由 MyBatis-Plus `ASSIGN_ID` 雪花算法生成。

### 6.2 业务索引

| 表 | 索引名 | 字段 | 用途 |
|----|--------|------|------|
| `sys_oper_log` | `idx_sys_oper_log_bt` | `business_type` | 按业务类型查询日志 |
| `sys_oper_log` | `idx_sys_oper_log_s` | `status` | 按操作状态查询日志 |
| `sys_oper_log` | `idx_sys_oper_log_ot` | `oper_time` | 按时间范围查询日志 |
| `sys_logininfor` | `idx_sys_logininfor_s` | `status` | 按登录状态查询 |
| `sys_logininfor` | `idx_sys_logininfor_lt` | `login_time` | 按时间范围查询 |
| `sys_dict_type` | `uk_tenant_dict_type` | `(tenant_id, dict_type)` | 唯一约束, 租户内字典类型唯一 |

---

## 7. 数据类型约定

| Java 类型 | MySQL 类型 | 用途 |
|-----------|-----------|------|
| `Long` | `BIGINT(20)` | 主键、外键引用 |
| `String` | `VARCHAR(n)` | 文本字段 |
| `String` | `CHAR(1)` | 单字符状态/标志字段 |
| `Integer` | `INT(n)` | 数值、排序、类型枚举 |
| `Date/LocalDateTime` | `DATETIME` | 时间戳 |
| `Boolean` | `TINYINT(1)` | 布尔标志 |

### 状态字段枚举约定

| 字段 | 值 | 含义 |
|------|-----|------|
| `status` (通用) | `'0'` | 正常 |
| | `'1'` | 停用 |
| `del_flag` | `'0'` | 存在 |
| | `'1'` | 已删除 |
| `menu_type` | `'M'` | 目录 |
| | `'C'` | 菜单 |
| | `'F'` | 按钮 |
| `visible` (菜单) | `'0'` | 显示 |
| | `'1'` | 隐藏 |
| `is_frame` (菜单) | `0` | 是外链 |
| | `1` | 否外链 |
| `is_cache` (菜单) | `0` | 缓存 |
| | `1` | 不缓存 |
| `sex` (用户) | `'0'` | 男 |
| | `'1'` | 女 |
| | `'2'` | 未知 |
| `user_type` | `'sys_user'` | 系统用户 |
| `access_policy` (OSS) | `'0'` | private |
| | `'1'` | public |
| | `'2'` | custom |
