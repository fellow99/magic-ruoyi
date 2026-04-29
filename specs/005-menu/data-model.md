# 菜单管理数据模型文档 (data-model.md)

> magic-ruoyi 菜单管理模块的数据模型定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 实体清单

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_menu` | SysMenu | `menu_id` BIGINT | 菜单权限表 |
| `sys_role_menu` | - | `(role_id, menu_id)` | 角色-菜单关联表 |

---

## 2. sys_menu（菜单权限表）

### 2.1 表结构

```sql
create table sys_menu (
    menu_id           bigint(20)      not null                   comment '菜单ID',
    menu_name         varchar(50)     not null                   comment '菜单名称',
    parent_id         bigint(20)      default 0                  comment '父菜单ID',
    order_num         int(4)          default 0                  comment '显示顺序',
    path              varchar(200)    default ''                 comment '路由地址',
    component         varchar(255)    default null               comment '组件路径',
    query_param       varchar(255)    default null               comment '路由参数',
    is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
    is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
    menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
    visible           char(1)         default 0                  comment '显示状态（0显示 1隐藏）',
    status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
    perms             varchar(100)    default null               comment '权限标识',
    icon              varchar(100)    default '#'                comment '菜单图标',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    update_time       datetime                                   comment '更新时间',
    remark            varchar(500)    default ''                 comment '备注',
    primary key (menu_id)
) engine=innodb comment = '菜单权限表';
```

### 2.2 字段说明

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| menu_id | BIGINT(20) | 是 | - | 菜单ID，雪花算法生成 |
| menu_name | VARCHAR(50) | 是 | - | 菜单名称 |
| parent_id | BIGINT(20) | 是 | 0 | 父菜单ID，0表示顶级菜单 |
| order_num | INT(4) | 是 | 0 | 显示顺序，数值越小越靠前 |
| path | VARCHAR(200) | 条件 | '' | 路由地址，目录/菜单类型必填 |
| component | VARCHAR(255) | 条件 | null | 组件路径，菜单类型必填，如 system/user/index |
| query_param | VARCHAR(255) | 否 | null | 路由参数，JSON格式 |
| is_frame | INT(1) | 是 | 1 | 是否外链，0=是，1=否 |
| is_cache | INT(1) | 是 | 0 | 是否缓存，0=缓存，1=不缓存 |
| menu_type | CHAR(1) | 是 | '' | 菜单类型，M=目录，C=菜单，F=按钮 |
| visible | CHAR(1) | 是 | 0 | 显示状态，0=显示，1=隐藏 |
| status | CHAR(1) | 是 | 0 | 菜单状态，0=正常，1=停用 |
| perms | VARCHAR(100) | 条件 | null | 权限标识，如 system:user:list |
| icon | VARCHAR(100) | 否 | '#' | 菜单图标名称 |
| create_dept | BIGINT(20) | 否 | null | 创建者所属部门ID |
| create_by | BIGINT(20) | 否 | null | 创建者用户ID |
| create_time | DATETIME | 否 | - | 创建时间 |
| update_by | BIGINT(20) | 否 | null | 最后更新者用户ID |
| update_time | DATETIME | 否 | - | 最后更新时间 |
| remark | VARCHAR(500) | 否 | '' | 备注，隐藏菜单时存储激活路由 |

### 2.3 枚举值

**menu_type（菜单类型）**:

| 值 | 含义 | 说明 |
|----|------|------|
| M | 目录 | 导航分组，不包含具体页面 |
| C | 菜单 | 具体页面，对应前端组件 |
| F | 按钮 | 操作权限点，不显示在导航中 |

**is_frame（是否外链）**:

| 值 | 含义 |
|----|------|
| 0 | 是外链 |
| 1 | 非外链 |

**is_cache（是否缓存）**:

| 值 | 含义 |
|----|------|
| 0 | 缓存（keep-alive） |
| 1 | 不缓存 |

**visible（显示状态）**:

| 值 | 含义 |
|----|------|
| 0 | 显示 |
| 1 | 隐藏 |

**status（菜单状态）**:

| 值 | 含义 |
|----|------|
| 0 | 正常 |
| 1 | 停用 |

### 2.4 初始数据

```sql
-- 一级目录
insert into sys_menu values('1', '系统管理', '0', '1', 'system', null, '', 1, 0, 'M', '0', '0', '', 'system', 103, 1, sysdate(), null, null, '系统管理目录');
insert into sys_menu values('6', '租户管理', '0', '2', 'tenant', null, '', 1, 0, 'M', '0', '1', '', 'chart', 103, 1, sysdate(), null, null, '租户管理目录');
insert into sys_menu values('2', '系统监控', '0', '3', 'monitor', null, '', 1, 0, 'M', '0', '0', '', 'monitor', 103, 1, sysdate(), null, null, '系统监控目录');
insert into sys_menu values('3', '系统工具', '0', '4', 'tool', null, '', 1, 0, 'M', '0', '0', '', 'tool', 103, 1, sysdate(), null, null, '系统工具目录');
insert into sys_menu values('4', '网站链接', '0', '5', 'https://www.baidu.com', null, '', 0, 0, 'M', '1', '0', '', 'guide', 103, 1, sysdate(), null, null, 'RuoYi-Vue-Plus官网地址');
insert into sys_menu values('5', '测试菜单', '0', '5', 'demo', null, '', 1, 0, 'M', '0', '0', '', 'star', 103, 1, sysdate(), null, null, '测试菜单');

-- 二级菜单（系统管理下）
insert into sys_menu values('100', '用户管理', '1', '1', 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 103, 1, sysdate(), null, null, '用户管理菜单');
insert into sys_menu values('101', '角色管理', '1', '2', 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 103, 1, sysdate(), null, null, '角色管理菜单');
insert into sys_menu values('102', '菜单管理', '1', '3', 'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 103, 1, sysdate(), null, null, '菜单管理菜单');
insert into sys_menu values('103', '部门管理', '1', '4', 'dept', 'system/dept/index', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 103, 1, sysdate(), null, null, '部门管理菜单');
insert into sys_menu values('104', '岗位管理', '1', '5', 'post', 'system/post/index', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 103, 1, sysdate(), null, null, '岗位管理菜单');
insert into sys_menu values('105', '字典管理', '1', '6', 'dict', 'system/dict/index', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 103, 1, sysdate(), null, null, '字典管理菜单');
insert into sys_menu values('106', '参数设置', '1', '7', 'config', 'system/config/index', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 103, 1, sysdate(), null, null, '参数设置菜单');
insert into sys_menu values('107', '通知公告', '1', '8', 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 103, 1, sysdate(), null, null, '通知公告菜单');
insert into sys_menu values('108', '日志管理', '1', '9', 'log', '', '', 1, 0, 'M', '0', '0', '', 'log', 103, 1, sysdate(), null, null, '日志管理菜单');

-- 按钮权限（用户管理下）
insert into sys_menu values('1001', '用户查询', '100', '1', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1002', '用户新增', '100', '2', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1003', '用户修改', '100', '3', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1004', '用户删除', '100', '4', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1005', '用户导出', '100', '5', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1006', '用户导入', '100', '6', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1007', '重置密码', '100', '7', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 103, 1, sysdate(), null, null, '');

-- 按钮权限（菜单管理下）
insert into sys_menu values('1013', '菜单查询', '102', '1', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1014', '菜单新增', '102', '2', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1015', '菜单修改', '102', '3', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1016', '菜单删除', '102', '4', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 103, 1, sysdate(), null, null, '');
```

---

## 3. sys_role_menu（角色-菜单关联表）

### 3.1 表结构

```sql
create table sys_role_menu (
    role_id   bigint(20) not null comment '角色ID',
    menu_id   bigint(20) not null comment '菜单ID',
    primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';
```

### 3.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role_id | BIGINT(20) | 是 | 角色ID，外键引用 sys_role.role_id |
| menu_id | BIGINT(20) | 是 | 菜单ID，外键引用 sys_menu.menu_id |

### 3.3 关系说明

- 联合主键：(role_id, menu_id)
- 一对多关系：一个角色可关联多个菜单
- 关联的菜单包含目录（M）、菜单（C）、按钮（F）三种类型
- 无审计字段，无逻辑删除
- 不受租户隔离（全局共享表），通过主表 sys_role 的 tenant_id 间接隔离
- 删除菜单时需同步删除此表中的关联记录

---

## 4. 实体关系图

```
┌──────────────────────────────────────────────────────────────┐
│                        菜单域                                  │
│                                                              │
│  sys_menu                                                    │
│  ├── menu_id (PK)                                            │
│  ├── menu_name                                               │
│  ├── parent_id ──▶ 自引用（树形结构）                          │
│  ├── order_num                                               │
│  ├── path (路由地址)                                          │
│  ├── component (组件路径)                                      │
│  ├── menu_type (M/C/F)                                       │
│  ├── visible (0=显示, 1=隐藏)                                 │
│  ├── status (0=正常, 1=停用)                                  │
│  ├── perms (权限标识)                                         │
│  ├── icon                                                    │
│  ├── is_frame (0=外链, 1=内链)                                │
│  ├── is_cache (0=缓存, 1=不缓存)                              │
│  └── remark (备注/隐藏菜单激活路由)                             │
│       │                                                      │
│       │ menu_id                                              │
│       └──────▶ sys_role_menu ◀────── sys_role                │
│                            │                                 │
│                            └── 角色拥有的菜单/按钮权限          │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 5. 前端类型定义

### 5.1 MenuTypeEnum（菜单类型枚举）

```typescript
export enum MenuTypeEnum {
  M = 'M',  // 目录
  C = 'C',  // 菜单
  F = 'F'   // 按钮
}
```

### 5.2 MenuVO（菜单视图对象）

```typescript
interface MenuVO extends BaseEntity {
  parentName: string;              // 父菜单名称
  parentId: string | number;       // 父菜单ID
  children: MenuVO[];              // 子菜单列表
  menuId: string | number;         // 菜单ID
  menuName: string;                // 菜单名称
  orderNum: number;                // 显示顺序
  path: string;                    // 路由地址
  component: string;               // 组件路径
  queryParam: string;              // 路由参数
  isFrame: string;                 // 是否外链
  isCache: string;                 // 是否缓存
  menuType: MenuTypeEnum;          // 菜单类型
  visible: string;                 // 显示状态
  status: string;                  // 菜单状态
  icon: string;                    // 菜单图标
  remark: string;                  // 备注
}
```

### 5.3 MenuForm（菜单表单对象）

```typescript
interface MenuForm {
  parentName?: string;             // 父菜单名称
  parentId?: string | number;      // 父菜单ID
  children?: MenuForm[];           // 子菜单列表
  menuId?: string | number;        // 菜单ID
  menuName: string;                // 菜单名称
  orderNum: number;                // 显示顺序
  path: string;                    // 路由地址
  component?: string;              // 组件路径
  queryParam?: string;             // 路由参数
  isFrame?: string;                // 是否外链
  isCache?: string;                // 是否缓存
  menuType?: MenuTypeEnum;         // 菜单类型
  visible?: string;                // 显示状态
  status?: string;                 // 菜单状态
  icon?: string;                   // 菜单图标
  remark?: string;                 // 备注
  query?: string;                  // 查询参数（别名）
  perms?: string;                  // 权限标识
}
```

### 5.4 MenuQuery（菜单查询参数）

```typescript
interface MenuQuery {
  keywords?: string;               // 关键字搜索
  menuName?: string;               // 菜单名称
  status?: string;                 // 状态
}
```

### 5.5 MenuTreeOption（菜单树选项）

```typescript
interface MenuTreeOption {
  id: string | number;             // 节点ID
  label: string;                   // 节点标签
  parentId: string | number;       // 父节点ID
  weight: number;                  // 权重/排序
  children?: MenuTreeOption[];     // 子节点
}
```

### 5.6 RoleMenuTree（角色菜单树响应）

```typescript
interface RoleMenuTree {
  menus: MenuTreeOption[];         // 菜单树数据
  checkedKeys: string[];           // 已勾选的菜单ID
}
```

---

## 6. 审计字段

sys_menu 表包含完整的审计字段，由 MyBatis-Plus MetaObjectHandler 自动填充：

| 字段 | 填充时机 | 数据来源 |
|------|----------|----------|
| create_dept | INSERT | 当前用户所属部门ID |
| create_by | INSERT | 当前用户ID |
| create_time | INSERT | 当前时间 |
| update_by | UPDATE | 当前用户ID |
| update_time | UPDATE | 当前时间 |

关联表 sys_role_menu 不包含审计字段。

---

## 7. 多租户隔离

| 表 | tenant_id | 隔离方式 |
|----|-----------|----------|
| sys_menu | 无 | 全局共享，不按租户隔离 |
| sys_role_menu | 无 | 通过 sys_role.tenant_id 间接隔离 |

菜单为全局共享资源，所有租户使用同一套菜单定义。租户通过租户套餐（tenant_package）限制可用菜单范围。

---

## 8. 索引设计

| 表 | 索引类型 | 字段 | 用途 |
|----|----------|------|------|
| sys_menu | 主键索引 | menu_id | 主键查询 |
| sys_menu | 普通索引（建议） | parent_id | 按父菜单查询子菜单 |
| sys_menu | 普通索引（建议） | status | 按状态过滤 |
| sys_role_menu | 主键索引 | (role_id, menu_id) | 联合主键 |
| sys_role_menu | 普通索引 | menu_id | 按菜单查询角色 |

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，菜单管理数据模型定义 |
