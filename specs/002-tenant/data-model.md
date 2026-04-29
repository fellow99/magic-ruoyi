# 租户管理模块数据模型文档

> magic-ruoyi 租户管理模块的数据模型定义，包含数据库表结构、实体类和前端类型。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 实体概览

| 表名 | 实体类 | 主键 | 描述 | 租户隔离 |
|------|--------|------|------|----------|
| `sys_tenant` | SysTenant | `id` BIGINT | 租户表 | 否（全局管理） |
| `sys_tenant_package` | SysTenantPackage | `package_id` BIGINT | 租户套餐表 | 否（全局共享） |

> 注：这两张表不参与租户过滤，由平台管理员在超级租户（000000）上下文中管理。

---

## 2. sys_tenant（租户表）

### 2.1 表结构

```sql
CREATE TABLE sys_tenant (
    id                BIGINT(20)    NOT NULL        COMMENT 'id',
    tenant_id         VARCHAR(20)   NOT NULL        COMMENT '租户编号',
    contact_user_name VARCHAR(20)                   COMMENT '联系人',
    contact_phone     VARCHAR(20)                   COMMENT '联系电话',
    company_name      VARCHAR(30)                   COMMENT '企业名称',
    license_number    VARCHAR(30)                   COMMENT '统一社会信用代码',
    address           VARCHAR(200)                  COMMENT '地址',
    intro             VARCHAR(200)                  COMMENT '企业简介',
    domain            VARCHAR(200)                  COMMENT '域名',
    remark            VARCHAR(200)                  COMMENT '备注',
    package_id        BIGINT(20)                    COMMENT '租户套餐编号',
    expire_time       DATETIME                      COMMENT '过期时间',
    account_count     INT           DEFAULT -1      COMMENT '用户数量（-1不限制）',
    status            CHAR(1)       DEFAULT '0'     COMMENT '租户状态（0正常 1停用）',
    del_flag          CHAR(1)       DEFAULT '0'     COMMENT '删除标志（0代表存在 1代表删除）',
    create_dept       BIGINT(20)                    COMMENT '创建部门',
    create_by         BIGINT(20)                    COMMENT '创建者',
    create_time       DATETIME                      COMMENT '创建时间',
    update_by         BIGINT(20)                    COMMENT '更新者',
    update_time       DATETIME                      COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT = '租户表';
```

### 2.2 字段说明

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT(20) | 是 | - | 主键，雪花算法生成 |
| tenant_id | VARCHAR(20) | 是 | - | 租户编号，系统自动生成 |
| contact_user_name | VARCHAR(20) | 否 | - | 联系人姓名 |
| contact_phone | VARCHAR(20) | 否 | - | 联系电话 |
| company_name | VARCHAR(30) | 否 | - | 企业名称 |
| license_number | VARCHAR(30) | 否 | - | 统一社会信用代码 |
| address | VARCHAR(200) | 否 | - | 企业地址 |
| intro | VARCHAR(200) | 否 | - | 企业简介 |
| domain | VARCHAR(200) | 否 | - | 绑定域名 |
| remark | VARCHAR(200) | 否 | - | 备注信息 |
| package_id | BIGINT(20) | 否 | - | 关联的租户套餐 ID |
| expire_time | DATETIME | 否 | - | 过期时间，空表示永不过期 |
| account_count | INT | 否 | -1 | 用户数量上限，-1 表示不限制 |
| status | CHAR(1) | 否 | '0' | 状态：0=正常，1=停用 |
| del_flag | CHAR(1) | 否 | '0' | 删除标志：0=存在，1=已删除 |
| create_dept | BIGINT(20) | 否 | - | 创建部门 ID |
| create_by | BIGINT(20) | 否 | - | 创建者用户 ID |
| create_time | DATETIME | 否 | - | 创建时间 |
| update_by | BIGINT(20) | 否 | - | 更新者用户 ID |
| update_time | DATETIME | 否 | - | 更新时间 |

### 2.3 索引设计

| 索引类型 | 字段 | 说明 |
|----------|------|------|
| 主键 | id | 雪花算法 BIGINT |
| 唯一索引 | tenant_id | 租户编号全局唯一 |

### 2.4 初始化数据

```sql
INSERT INTO sys_tenant VALUES (
    1, '000000', '主户', '', '主户',
    NULL, NULL, '后台管理系统', NULL, NULL, NULL, NULL,
    -1, '0', '0', 103, 1, SYSDATE(), NULL, NULL
);
```

---

## 3. sys_tenant_package（租户套餐表）

### 3.1 表结构

```sql
CREATE TABLE sys_tenant_package (
    package_id          BIGINT(20)    NOT NULL    COMMENT '租户套餐id',
    package_name        VARCHAR(20)               COMMENT '套餐名称',
    menu_ids            VARCHAR(3000)             COMMENT '关联菜单id',
    remark              VARCHAR(200)              COMMENT '备注',
    menu_check_strictly TINYINT(1)    DEFAULT 1   COMMENT '菜单树选择项是否关联显示',
    status              CHAR(1)       DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag            CHAR(1)       DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    create_dept         BIGINT(20)                COMMENT '创建部门',
    create_by           BIGINT(20)                COMMENT '创建者',
    create_time         DATETIME                  COMMENT '创建时间',
    update_by           BIGINT(20)                COMMENT '更新者',
    update_time         DATETIME                  COMMENT '更新时间',
    PRIMARY KEY (package_id)
) ENGINE=InnoDB COMMENT = '租户套餐表';
```

### 3.2 字段说明

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| package_id | BIGINT(20) | 是 | - | 主键，雪花算法生成 |
| package_name | VARCHAR(20) | 否 | - | 套餐名称 |
| menu_ids | VARCHAR(3000) | 否 | - | 关联菜单 ID 列表，逗号分隔 |
| remark | VARCHAR(200) | 否 | - | 备注信息 |
| menu_check_strictly | TINYINT(1) | 否 | 1 | 菜单树父子联动开关：1=联动，0=不联动 |
| status | CHAR(1) | 否 | '0' | 状态：0=正常，1=停用 |
| del_flag | CHAR(1) | 否 | '0' | 删除标志：0=存在，1=已删除 |
| create_dept | BIGINT(20) | 否 | - | 创建部门 ID |
| create_by | BIGINT(20) | 否 | - | 创建者用户 ID |
| create_time | DATETIME | 否 | - | 创建时间 |
| update_by | BIGINT(20) | 否 | - | 更新者用户 ID |
| update_time | DATETIME | 否 | - | 更新时间 |

### 3.3 索引设计

| 索引类型 | 字段 | 说明 |
|----------|------|------|
| 主键 | package_id | 雪花算法 BIGINT |

---

## 4. 实体关系

### 4.1 关系图

```
sys_tenant ──(package_id)──▶ sys_tenant_package
     │                              │
     │ 1:N                          │ menu_ids (逗号分隔的菜单ID)
     ▼                              ▼
  租户数据                      sys_menu (全局共享)
  (按 tenant_id 隔离)
```

### 4.2 关系说明

| 关系 | 说明 |
|------|------|
| sys_tenant → sys_tenant_package | 多对一，通过 `package_id` 外键关联 |
| sys_tenant_package → sys_menu | 多对多，通过 `menu_ids` 逗号分隔列表关联 |
| sys_tenant → 所有业务表 | 一对多，通过 `tenant_id` 实现行级数据隔离 |

### 4.3 权限继承链路

```
sys_tenant.package_id
    │
    ▼
sys_tenant_package.menu_ids (逗号分隔的菜单ID列表)
    │
    ▼
sys_menu (菜单权限表)
    │
    ▼
用户实际可用菜单 = 套餐菜单 AND 角色分配菜单
```

---

## 5. 前端 TypeScript 类型

### 5.1 租户类型（Tenant）

文件: `src/api/system/tenant/types.ts`

**TenantVO（视图对象）**:

```typescript
export interface TenantVO extends BaseEntity {
  id: number | string;
  tenantId: number | string;
  username: string;
  contactUserName: string;
  contactPhone: string;
  companyName: string;
  licenseNumber: string;
  address: string;
  domain: string;
  intro: string;
  remark: string;
  packageId: string | number;
  expireTime: string;
  accountCount: number;
  status: string;
}
```

**TenantQuery（查询参数）**:

```typescript
export interface TenantQuery extends PageQuery {
  tenantId: string | number;
  contactUserName: string;
  contactPhone: string;
  companyName: string;
}
```

**TenantForm（表单对象）**:

```typescript
export interface TenantForm {
  id: number | string | undefined;
  tenantId: number | string | undefined;
  username: string;
  password: string;
  contactUserName: string;
  contactPhone: string;
  companyName: string;
  licenseNumber: string;
  domain: string;
  address: string;
  intro: string;
  remark: string;
  packageId: string | number;
  expireTime: string;
  accountCount: number;
  status: string;
}
```

### 5.2 租户套餐类型（TenantPackage）

文件: `src/api/system/tenantPackage/types.ts`

**TenantPkgVO（视图对象）**:

```typescript
export interface TenantPkgVO extends BaseEntity {
  packageId: string | number;
  packageName: string;
  menuIds: string;
  remark: string;
  menuCheckStrictly: boolean;
  status: string;
}
```

**TenantPkgQuery（查询参数）**:

```typescript
export interface TenantPkgQuery extends PageQuery {
  packageName: string;
}
```

**TenantPkgForm（表单对象）**:

```typescript
export interface TenantPkgForm {
  packageId: string | number | undefined;
  packageName: string;
  menuIds: string;
  remark: string;
  menuCheckStrictly: boolean;
}
```

---

## 6. 后端 VO 类型

### 6.1 TenantListVo

文件: `src/main/java/org/fellow99/magic/ruoyi/domain/vo/TenantListVo.java`

```java
@Data
@AutoMapper(target = SysTenantVo.class)
public class TenantListVo {
    private String tenantId;      // 租户编号
    private String companyName;   // 企业名称
    private String domain;        // 域名
}
```

> 用于登录页面展示可选租户列表。

### 6.2 LoginTenantVo

文件: `src/main/java/org/fellow99/magic/ruoyi/domain/vo/LoginTenantVo.java`

```java
@Data
public class LoginTenantVo {
    private Boolean tenantEnabled;        // 租户开关
    private List<TenantListVo> voList;    // 租户列表
}
```

> 用于登录页面返回租户开关状态和可选租户列表。

---

## 7. 审计字段

两张表均使用标准审计字段，通过 MyBatis-Plus `MetaObjectHandler` 自动填充：

| 字段 | 类型 | 填充时机 | 数据来源 |
|------|------|----------|----------|
| create_dept | BIGINT(20) | 插入时 | 当前用户所属部门 |
| create_by | BIGINT(20) | 插入时 | 当前登录用户 ID |
| create_time | DATETIME | 插入时 | 当前时间 |
| update_by | BIGINT(20) | 更新时 | 当前登录用户 ID |
| update_time | DATETIME | 更新时 | 当前时间 |

---

## 8. 逻辑删除

| 表 | 字段 | 默认值 | 删除值 |
|----|------|--------|--------|
| sys_tenant | del_flag | '0' | '1' |
| sys_tenant_package | del_flag | '0' | '1' |

通过 MyBatis-Plus `@TableLogic` 注解实现，查询时自动附加 `del_flag = '0'` 条件。

---

## 9. 枚举值

### 9.1 状态枚举

| 字段 | 值 | 含义 |
|------|-----|------|
| status | '0' | 正常 |
| | '1' | 停用 |
| del_flag | '0' | 存在 |
| | '1' | 已删除 |

### 9.2 用户数量限制

| 值 | 含义 |
|----|------|
| -1 | 不限制用户数量 |
| >0 | 最大用户数量 |

### 9.3 菜单联动开关

| 值 | 含义 |
|----|------|
| 1 (true) | 父子联动，选择父节点自动选中子节点 |
| 0 (false) | 独立选择，父子节点互不影响 |

---

## 10. 多租户数据隔离

### 10.1 隔离策略

采用 **行级逻辑隔离** 模式：
- 共享数据库
- 共享 Schema
- 按 `tenant_id` 行级过滤

### 10.2 租户编号规则

| 属性 | 值 |
|------|-----|
| 字段名 | tenant_id |
| 类型 | VARCHAR(20) |
| 超级租户 | '000000' |

### 10.3 租户表隔离说明

`sys_tenant` 和 `sys_tenant_package` 两张表 **不参与** 租户过滤，原因：
- `sys_tenant` 是租户定义表，由平台管理员在超级租户上下文中管理
- `sys_tenant_package` 是全局共享的套餐定义表

通过 MyBatis-Plus `TenantLineInnerInterceptor` 配置忽略列表排除这两张表。

### 10.4 租户生命周期

```
创建租户 → 分配套餐 → 生成 tenant_id → 初始化租户管理员 → 激活
    │
    ├── 过期处理: expire_time 到期后 status 自动设为 '1'
    ├── 用户限额: account_count 控制最大用户数 (-1 不限制)
    └── 删除: del_flag = '1' 逻辑删除
```

---

## 11. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，租户管理模块数据模型定义 |
