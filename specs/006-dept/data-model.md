# 部门管理数据模型文档 (data-model.md)

> magic-ruoyi 部门管理模块的数据模型定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 实体清单

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `sys_dept` | SysDept | `dept_id` BIGINT | 部门信息表（树形结构） |

---

## 2. sys_dept（部门信息表）

### 2.1 表结构

```sql
create table sys_dept (
    dept_id           bigint(20)      not null                   comment '部门ID',
    tenant_id         varchar(20)     default '000000'           comment '租户编号',
    parent_id         bigint(20)      default 0                  comment '父部门ID',
    ancestors         varchar(500)    default ''                 comment '祖级列表',
    dept_name         varchar(30)     default ''                 comment '部门名称',
    dept_category     varchar(100)    default null               comment '部门类别编码',
    order_num         int(4)          default 0                  comment '显示顺序',
    leader            bigint(20)      default null               comment '负责人用户ID',
    phone             varchar(11)     default null               comment '联系电话',
    email             varchar(50)     default null               comment '邮箱',
    status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
    del_flag          char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    update_time       datetime                                   comment '更新时间',
    primary key (dept_id)
) engine=innodb comment = '部门表';
```

### 2.2 字段说明

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| dept_id | BIGINT(20) | 是 | - | 部门ID，雪花算法生成 |
| tenant_id | VARCHAR(20) | 是 | '000000' | 租户编号，用于多租户隔离 |
| parent_id | BIGINT(20) | 是 | 0 | 父部门ID，0表示根部门 |
| ancestors | VARCHAR(500) | 是 | '' | 祖级列表，逗号分隔的部门ID路径，如 "0,100,102" |
| dept_name | VARCHAR(30) | 是 | '' | 部门名称 |
| dept_category | VARCHAR(100) | 否 | null | 部门类别编码，用于标识部门类型 |
| order_num | INT(4) | 是 | 0 | 显示顺序，数值越小越靠前 |
| leader | BIGINT(20) | 否 | null | 负责人用户ID，关联 sys_user.user_id |
| phone | VARCHAR(11) | 否 | null | 联系电话 |
| email | VARCHAR(50) | 否 | null | 邮箱地址 |
| status | CHAR(1) | 是 | '0' | 部门状态，0=正常，1=停用 |
| del_flag | CHAR(1) | 是 | '0' | 逻辑删除标志，0=正常，1=已删除 |
| create_dept | BIGINT(20) | 否 | null | 创建者所属部门ID |
| create_by | BIGINT(20) | 否 | null | 创建者用户ID |
| create_time | DATETIME | 否 | - | 创建时间 |
| update_by | BIGINT(20) | 否 | null | 最后更新者用户ID |
| update_time | DATETIME | 否 | - | 最后更新时间 |

### 2.3 枚举值

**status（部门状态）**:

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
insert into sys_dept values(100, '000000', 0, '0', '根机构', null, 0, null, '', '', '0', '0', 103, 1, sysdate(), null, null);
```

---

## 3. 树形结构说明

### 3.1 自引用关系

部门通过 `parent_id` 字段自引用形成树形结构：

```
sys_dept.parent_id ──▶ sys_dept.dept_id (自引用)
```

### 3.2 祖级列表（ancestors）

`ancestors` 字段存储从根节点到当前节点的完整路径，以逗号分隔：

```
根机构 (dept_id=100, ancestors="0")
  └── 研发部 (dept_id=102, ancestors="0,100")
        └── 前端组 (dept_id=103, ancestors="0,100,102")
```

**维护规则**:
- 新增部门时，ancestors = 父部门.ancestors + "," + 父部门.dept_id
- 修改部门的 parent_id 时，需递归更新该部门及所有子孙部门的 ancestors 字段

### 3.3 树形结构示例

```
根机构 (dept_id=100)
  ├── 研发部 (dept_id=102)
  │     ├── 前端组 (dept_id=103)
  │     └── 后端组 (dept_id=104)
  ├── 市场部 (dept_id=105)
  │     └── 销售组 (dept_id=106)
  └── 财务部 (dept_id=108)
```

---

## 4. 关联关系

### 4.1 与用户的关联

```
sys_user.dept_id ──▶ sys_dept.dept_id
```

- 一个部门可以有多个用户
- 一个用户属于一个部门
- 部门负责人（sys_dept.leader）是 sys_user.user_id 的引用

### 4.2 与角色的关联（数据权限）

```
sys_role_dept.dept_id ──▶ sys_dept.dept_id
```

- 角色的自定义数据权限（data_scope=2）通过 sys_role_dept 表关联具体部门
- 一个角色可以关联多个部门
- 一个部门可以被多个角色关联

### 4.3 与岗位的关联

```
sys_post.dept_id ──▶ sys_dept.dept_id
```

- 岗位属于特定部门

---

## 5. 前端类型定义

### 5.1 DeptVO（部门视图对象）

```typescript
interface DeptVO extends BaseEntity {
  id: number | string;              // 内部ID
  parentName: string;               // 上级部门名称
  parentId: number | string;        // 上级部门ID
  children: DeptVO[];               // 子部门列表
  deptId: number | string;          // 部门ID
  deptName: string;                 // 部门名称
  deptCategory: string;             // 部门类别编码
  orderNum: number;                 // 显示顺序
  leader: string;                   // 负责人用户ID
  phone: string;                    // 联系电话
  email: string;                    // 邮箱
  status: string;                   // 状态
  delFlag: string;                  // 删除标志
  ancestors: string;                // 祖级列表
  menuId: string | number;          // 菜单ID（用于树选择）
}
```

### 5.2 DeptTreeVO（部门树视图对象）

```typescript
interface DeptTreeVO extends BaseEntity {
  id: number | string;              // 节点ID
  label: string;                    // 节点显示名称
  parentId: number | string;        // 父节点ID
  weight: number;                   // 权重/排序
  children: DeptTreeVO[];           // 子节点列表
  disabled: boolean;                // 是否禁用选择
}
```

### 5.3 DeptQuery（部门查询参数）

```typescript
interface DeptQuery extends PageQuery {
  deptName?: string;                // 部门名称
  deptCategory?: string;            // 类别编码
  status?: number;                  // 状态
}
```

### 5.4 DeptForm（部门表单对象）

```typescript
interface DeptForm {
  parentName?: string;              // 上级部门名称
  parentId?: number | string;       // 上级部门ID
  children?: DeptForm[];            // 子部门列表
  deptId?: number | string;         // 部门ID
  deptName?: string;                // 部门名称
  deptCategory?: string;            // 部门类别编码
  orderNum?: number;                // 显示顺序
  leader?: string;                  // 负责人用户ID
  phone?: string;                   // 联系电话
  email?: string;                   // 邮箱
  status?: string;                  // 状态
  delFlag?: string;                 // 删除标志
  ancestors?: string;               // 祖级列表
}
```

### 5.5 DeptOptionsType（部门选择器类型）

```typescript
interface DeptOptionsType {
  deptId: number | string;          // 部门ID
  deptName: string;                 // 部门名称
  children: DeptOptionsType[];      // 子部门列表
}
```

---

## 6. 审计字段

sys_dept 表包含完整的审计字段，由 MyBatis-Plus MetaObjectHandler 自动填充：

| 字段 | 填充时机 | 数据来源 |
|------|----------|----------|
| create_dept | INSERT | 当前用户所属部门ID |
| create_by | INSERT | 当前用户ID |
| create_time | INSERT | 当前时间 |
| update_by | UPDATE | 当前用户ID |
| update_time | UPDATE | 当前时间 |

---

## 7. 多租户隔离

| 表 | tenant_id | 隔离方式 |
|----|-----------|----------|
| sys_dept | 有 | 直接隔离，MyBatis-Plus 租户插件自动过滤 |

---

## 8. 索引设计

| 索引类型 | 字段 | 用途 |
|----------|------|------|
| 主键索引 | dept_id | 主键查询 |
| 普通索引（建议） | parent_id | 按父部门查询子部门 |
| 普通索引（建议） | ancestors | 按祖级列表查询子孙部门 |
| 普通索引（建议） | (tenant_id, dept_name) | 租户内部门名称查询 |

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，部门管理数据模型定义 |
