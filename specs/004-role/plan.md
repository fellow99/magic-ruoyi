# 004-Role 角色管理模块 - 技术实现方案（Plan）

> magic-ruoyi 角色管理模块技术实现方案。定义角色 CRUD、菜单权限、数据权限、用户分配的技术实现细节。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. Technical Context

### 1.1 运行环境

| 组件 | 版本/配置 | 说明 |
|------|-----------|------|
| Java | 21+ | 语言版本 |
| Spring Boot | 3.5.14 | 基础框架 |
| RuoYi-Vue-Plus | 5.6.0 | 父框架 |
| MyBatis-Plus | 3.5.16 | ORM 框架 + 数据权限插件 |
| MySQL | 最新稳定版 | 数据存储 |
| Redis | 最新稳定版 | 缓存 |
| Vue | 3.5.22 | 前端框架 |
| Element Plus | 2.11.7 | 前端 UI 库 |

### 1.2 模块依赖

| 依赖 | 来源 | 用途 |
|------|------|------|
| `ruoyi-common-core` | org.dromara | 基础工具类、常量、异常 |
| `ruoyi-common-web` | org.dromara | BaseController、全局异常处理 |
| `ruoyi-common-excel` | org.dromara | Excel 导出 |
| `ruoyi-common-satoken` | org.dromara | Sa-Token 权限校验 |
| `ruoyi-common-log` | org.dromara | @Log 操作日志注解 |
| `ruoyi-system` | org.dromara | SysRole、SysRoleMenu、SysRoleDept 实体/服务 |

### 1.3 包结构

> 注：角色管理的 Controller、Service、Mapper、Domain 类均继承自 RuoYi-Vue-Plus 框架（org.dromara.system 包）。

```
org.dromara.system (上游框架)
├── controller/
│   ├── SysRoleController.java           # 角色管理入口（CRUD、数据权限、用户分配）
│   └── SysRoleAuthController.java       # 角色用户分配入口
├── service/
│   ├── ISysRoleService.java             # 角色服务接口
│   └── impl/
│       └── SysRoleServiceImpl.java      # 角色服务实现
├── domain/
│   ├── SysRole.java                     # 角色实体
│   ├── SysRoleMenu.java                 # 角色菜单关联实体
│   └── SysRoleDept.java                 # 角色部门关联实体（数据权限）
├── domain/bo/
│   └── SysRoleBo.java                   # 角色业务对象
├── domain/vo/
│   └── SysRoleVo.java                   # 角色视图对象
└── mapper/
    ├── SysRoleMapper.java               # 角色数据访问
    ├── SysRoleMenuMapper.java           # 角色菜单关联数据访问
    └── SysRoleDeptMapper.java           # 角色部门关联数据访问
```

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 角色 CRUD 直接复用框架能力，菜单/数据权限通过关联表实现 |
| 约定优于配置 | 合规 | 遵循 RuoYi-Vue-Plus 的分层结构，权限标识使用 system:role:* 约定 |
| 实用优于完美 | 合规 | 菜单树和数据权限树使用 el-tree 组件，父子联动开关控制勾选行为 |
| 安全优于便利 | 合规 | 超级管理员角色保护，数据权限变更即时生效 |
| 零样板代码 | 合规 | MyBatis-Plus BaseMapperPlus 减少 CRUD 代码，MapStruct 对象转换 |
| 多租户优先 | 合规 | 角色数据按 tenant_id 隔离，不同租户可创建同名角色 |
| 前后端分离 | 合规 | 前端独立 role/index.vue 和 authUser.vue，通过 API 客户端调用 |
| 依赖整合，非复制 | 合规 | 角色管理核心代码位于 org.dromara.system 包，通过 Maven 依赖引入 |

---

## 3. Research Findings

### 3.1 数据权限实现决策

**决策**: 使用 MyBatis-Plus 数据权限拦截器，通过角色配置的 data_scope 自动在 SQL 中追加过滤条件。

**理由**:
- 6 种数据权限范围覆盖常见场景
- 拦截器自动注入 SQL，开发者无需手动编写权限过滤
- `DataPermissionHelper.ignore()` 可临时忽略数据权限
- 相比手动编写 WHERE 条件，拦截器方式更统一、更不易出错

### 3.2 菜单权限存储决策

**决策**: 角色菜单权限存储在 sys_role_menu 关联表中，同时记录选中节点和半选中节点。

**理由**:
- 关联表支持高效的 JOIN 查询
- 半选中节点（父节点部分子节点被选中）需要记录，以保证树形回显正确
- `menuCheckStrictly` 控制父子联动行为

### 3.3 数据权限部门存储决策

**决策**: 自定义数据权限（data_scope=2）的部门存储在 sys_role_dept 关联表中。

**理由**:
- 一个角色可关联多个部门
- 关联表支持高效的权限查询
- `deptCheckStrictly` 控制部门树父子联动行为

### 3.4 超级管理员保护决策

**决策**: roleId = 1 的超级管理员角色不可删除、不可修改数据权限。

**理由**:
- 超级管理员拥有全部权限，不受菜单分配限制
- 防止误操作导致系统失去管理员角色
- 前端通过 `v-if="scope.row.roleId !== 1"` 隐藏操作按钮

### 3.5 用户分配决策

**决策**: 角色用户分配通过独立页面（authUser.vue）实现，支持已分配/未分配用户列表切换。

**理由**:
- 已分配用户列表支持取消授权（单个/批量）
- 未分配用户列表支持批量添加
- 相比在角色编辑页分配用户，独立页面更适合大量用户场景

---

## 4. Data Model

### 4.1 SysRole（角色实体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role_id | BIGINT | 是 | 主键 |
| tenant_id | VARCHAR(20) | 是 | 租户编号 |
| role_name | VARCHAR(30) | 是 | 角色名称（租户内唯一） |
| role_key | VARCHAR(100) | 是 | 权限字符（租户内唯一，如 admin） |
| role_sort | INT | 是 | 显示顺序 |
| data_scope | CHAR(1) | 是 | 数据权限范围（1-6） |
| menu_check_strictly | CHAR(1) | 是 | 菜单树父子联动（0=否, 1=是） |
| dept_check_strictly | CHAR(1) | 是 | 部门树父子联动（0=否, 1=是） |
| status | CHAR(1) | 是 | 状态（0=正常, 1=停用） |
| del_flag | CHAR(1) | 是 | 删除标志（0=正常, 1=删除） |
| remark | VARCHAR(500) | 否 | 备注 |
| create_dept | BIGINT | 否 | 创建部门 |
| create_by | BIGINT | 否 | 创建者 |
| create_time | DATETIME | 否 | 创建时间 |
| update_by | BIGINT | 否 | 更新者 |
| update_time | DATETIME | 否 | 更新时间 |

### 4.2 关联表

#### sys_role_menu（角色菜单关联）

| 字段 | 类型 | 说明 |
|------|------|------|
| role_id | BIGINT | 角色 ID |
| menu_id | BIGINT | 菜单 ID |

#### sys_role_dept（角色部门关联，数据权限）

| 字段 | 类型 | 说明 |
|------|------|------|
| role_id | BIGINT | 角色 ID |
| dept_id | BIGINT | 部门 ID |

### 4.3 前端类型定义

#### RoleVO

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | string/number | 角色 ID |
| roleName | string | 角色名称 |
| roleKey | string | 权限字符 |
| roleSort | number | 显示顺序 |
| dataScope | string | 数据权限范围 |
| menuCheckStrictly | boolean | 菜单树父子联动 |
| deptCheckStrictly | boolean | 部门树父子联动 |
| status | string | 状态 |
| delFlag | string | 删除标志 |
| remark | any | 备注 |
| flag | boolean | 是否已分配给用户 |
| menuIds | Array | 菜单 ID 数组 |
| deptIds | Array | 部门 ID 数组 |
| admin | boolean | 是否超级管理员 |

#### RoleForm

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | string | 角色 ID |
| roleName | string | 角色名称 |
| roleKey | string | 权限字符 |
| roleSort | number | 显示顺序 |
| status | string | 状态 |
| menuCheckStrictly | boolean | 菜单树父子联动 |
| deptCheckStrictly | boolean | 部门树父子联动 |
| remark | string | 备注 |
| dataScope | string | 数据权限范围 |
| menuIds | Array | 菜单 ID 数组 |
| deptIds | Array | 部门 ID 数组 |

### 4.4 数据权限范围枚举

| data_scope 值 | 含义 | 数据过滤逻辑 |
|---------------|------|-------------|
| 1 | 全部数据权限 | 无额外过滤，可查看全部数据 |
| 2 | 自定义数据权限 | 仅可查看 sys_role_dept 中指定部门的数据 |
| 3 | 本部门数据权限 | 仅可查看用户所属部门的数据 |
| 4 | 本部门及以下数据权限 | 可查看用户所属部门及其所有子部门的数据 |
| 5 | 仅本人数据权限 | 仅可查看用户自己创建的数据 |
| 6 | 部门及以下或本人数据权限 | 可查看用户所属部门及子部门的数据，或用户自己创建的数据 |

### 4.5 状态转换

```
[正常(0)] ──changeStatus──▶ [停用(1)]
    │                            │
    │  停用后该角色不再生效        │  重新启用
    ▼                            ▼
  失去权限                     恢复权限

[未删除] ──delete──▶ [已删除(del_flag=1)]
```

### 4.6 验证规则

| 规则 | 适用场景 | 验证方式 |
|------|---------|---------|
| 角色名称 | 新增/修改 | 必填，最长 30 字符，租户内唯一 |
| 权限字符 | 新增/修改 | 必填，最长 100 字符，租户内唯一 |
| 角色顺序 | 新增/修改 | 必填，数字 |
| 数据权限 | 数据权限配置 | 必填，1-6 之一 |
| 自定义部门 | data_scope=2 | 至少选择一个部门 |

---

## 5. Interface Contracts

### 5.1 提供接口（后端 API）

#### 角色管理接口（/system/role）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /system/role/list | system:role:list | 分页查询角色列表 |
| GET | /system/role/export | system:role:export | 导出角色数据（Excel） |
| GET | /system/role/optionselect | - | 按角色 ID 查询角色列表（下拉选） |
| GET | /system/role/{roleId} | system:role:query | 查询角色详情 |
| POST | /system/role | system:role:add | 新增角色 |
| PUT | /system/role | system:role:edit | 修改角色 |
| DELETE | /system/role/{roleIds} | system:role:remove | 删除角色 |
| PUT | /system/role/changeStatus | system:role:edit | 切换角色状态 |
| PUT | /system/role/dataScope | system:role:edit | 配置角色数据权限 |
| GET | /system/role/deptTree/{roleId} | - | 查询角色部门树（数据权限回显） |

#### 角色用户分配接口（/system/role/authUser）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /system/role/authUser/allocatedList | system:role:edit | 查询已分配用户列表 |
| GET | /system/role/authUser/unallocatedList | system:role:edit | 查询未分配用户列表 |
| PUT | /system/role/authUser/cancel | system:role:edit | 取消单个用户授权 |
| PUT | /system/role/authUser/cancelAll | system:role:edit | 批量取消用户授权 |
| PUT | /system/role/authUser/selectAll | system:role:edit | 批量添加用户授权 |

### 5.2 消费接口（依赖上游）

| 接口 | 来源 | 用途 |
|------|------|------|
| ISysMenuService | ruoyi-system | 菜单树数据 |
| ISysDeptService | ruoyi-system | 部门树数据 |
| ISysUserService | ruoyi-system | 用户列表（分配用户） |

### 5.3 前端 API 客户端

| 函数 | 文件 | 方法 | 路径 |
|------|------|------|------|
| listRole() | api/system/role/index.ts | GET | /system/role/list |
| getRole() | api/system/role/index.ts | GET | /system/role/:id |
| addRole() | api/system/role/index.ts | POST | /system/role |
| updateRole() | api/system/role/index.ts | PUT | /system/role |
| delRole() | api/system/role/index.ts | DELETE | /system/role/:ids |
| changeRoleStatus() | api/system/role/index.ts | PUT | /system/role/changeStatus |
| dataScope() | api/system/role/index.ts | PUT | /system/role/dataScope |
| deptTreeSelect() | api/system/role/index.ts | GET | /system/role/deptTree/:id |
| allocatedUserList() | api/system/role/index.ts | GET | /system/role/authUser/allocatedList |
| unallocatedUserList() | api/system/role/index.ts | GET | /system/role/authUser/unallocatedList |
| authUserCancel() | api/system/role/index.ts | PUT | /system/role/authUser/cancel |
| authUserCancelAll() | api/system/role/index.ts | PUT | /system/role/authUser/cancelAll |
| authUserSelectAll() | api/system/role/index.ts | PUT | /system/role/authUser/selectAll |

---

## 6. Implementation Strategy

### 6.1 架构模式

**角色-菜单-数据权限三位一体**:

```
                    ┌──────────────┐
                    │    角色       │
                    │  SysRole     │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
     ┌──────────────┐ ┌──────────┐ ┌──────────────┐
     │ sys_role_menu│ │data_scope│ │ sys_role_dept│
     │ (功能权限)    │ │ (1-6)    │ │ (自定义部门)  │
     └──────┬───────┘ └──────────┘ └──────┬───────┘
            │                              │
            ▼                              ▼
     ┌──────────────┐              ┌──────────────┐
     │   sys_menu   │              │   sys_dept   │
     │ (菜单/按钮)   │              │   (部门)      │
     └──────────────┘              └──────────────┘
```

**数据权限拦截器工作流**:

```
SQL 查询进入
    │
    ▼
数据权限拦截器检查当前用户角色
    │
    ▼
根据 data_scope 追加 WHERE 条件:
  1 = 全部数据: 不追加
  2 = 自定义: WHERE dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = ?)
  3 = 本部门: WHERE dept_id = 用户所属部门
  4 = 本部门及以下: WHERE dept_id IN (用户部门及子部门)
  5 = 仅本人: WHERE create_by = 当前用户
  6 = 部门及以下或本人: WHERE dept_id IN (...) OR create_by = 当前用户
    │
    ▼
执行 SQL
```

### 6.2 关键算法

#### 6.2.1 新增角色流程

```
1. 前端获取菜单树数据
2. 用户填写角色信息（名称、权限字符、顺序、状态）
3. 勾选菜单权限（el-tree）
4. 获取选中节点 + 半选中节点 ID
5. 提交到后端
6. 后端校验（角色名称唯一性、权限字符唯一性）
7. 插入 sys_role 记录
8. 批量插入 sys_role_menu 关联记录
9. 返回成功
```

#### 6.2.2 数据权限配置流程

```
1. 打开数据权限对话框
2. 查询角色详情（含当前 data_scope）
3. 查询部门树（含已勾选的部门回显）
4. 用户选择数据权限范围（1-6）
5. 若选择"自定义数据权限"（2）:
   a. 显示部门树
   b. 用户勾选具体部门
   c. 获取选中节点 + 半选中节点 ID
6. 提交到后端
7. 后端:
   a. 更新 sys_role.data_scope
   b. 若 data_scope=2:
      - 删除该角色的所有 sys_role_dept 记录
      - 批量插入新的 sys_role_dept 记录
   c. 若 data_scope!=2:
      - 清空 sys_role_dept 记录
8. 清除数据权限缓存
9. 返回成功
```

#### 6.2.3 菜单树勾选算法

```
getMenuAllCheckedKeys():
  1. 获取 getCheckedKeys()（完全选中的节点）
  2. 获取 getHalfCheckedKeys()（半选中的节点）
  3. 合并两个数组（半选中节点插入到前面）
  4. 返回合并后的数组

说明:
- 半选中节点表示父节点的部分子节点被选中
- 保存半选中节点确保树形回显时父节点正确显示为半选中状态
```

### 6.3 错误处理

| 异常场景 | 处理方式 |
|---------|---------|
| 角色名称重复 | 返回错误提示 |
| 权限字符重复 | 返回错误提示 |
| 删除超级管理员角色 | 前端隐藏按钮，后端保护 |
| 删除有用户关联的角色 | 级联清除 sys_user_role 记录 |
| 数据权限切换为非自定义 | 清空已选部门 |
| 自定义数据权限未选部门 | 返回错误提示 |

### 6.4 性能优化

| 优化点 | 策略 |
|--------|------|
| 列表查询 | 分页查询，默认 10 条/页 |
| 角色搜索 | 支持角色名称、权限字符、状态、时间范围筛选 |
| 菜单树 | 一次性加载全部，前端树形展示 |
| 导出操作 | 使用下载流方式，不阻塞主线程 |
| 数据权限变更 | 清除缓存，下次查询即时生效 |

### 6.5 安全控制

| 安全措施 | 实现方式 |
|---------|---------|
| 超级管理员保护 | roleId=1 不可删除/修改数据权限 |
| 租户数据隔离 | MyBatis-Plus 租户插件自动过滤 |
| 权限控制 | @SaCheckPermission 注解 |
| 操作日志 | @Log 注解记录写操作 |
| 数据权限拦截 | MyBatis-Plus 数据权限拦截器 |
| 状态切换回滚 | 前端失败时恢复开关状态 |

---

## 7. Testing Considerations

### 7.1 可测试性设计

- Service 层通过接口定义，便于 Mock
- 数据权限拦截器可配置排除表进行测试
- 菜单树勾选算法可独立单元测试

### 7.2 测试类别

| 测试类型 | 测试对象 | 工具 |
|---------|---------|------|
| 单元测试 | SysRoleServiceImpl | JUnit 5 + Mockito |
| 单元测试 | 数据权限拦截器 | JUnit 5 + Spring Boot Test |
| 集成测试 | SysRoleController | Spring Boot Test + MockMvc |
| 集成测试 | 角色-菜单关联 | Spring Boot Test |
| 前端测试 | role/index.vue 表单校验 | Vitest + Vue Test Utils |

### 7.3 边缘情况

| 场景 | 预期行为 |
|------|---------|
| 新增角色时角色名称已存在 | 返回错误提示 |
| 新增角色时权限字符已存在 | 返回错误提示 |
| 删除有用户关联的角色 | 级联清除 sys_user_role 记录 |
| 数据权限从自定义切换为全部数据 | 清空 sys_role_dept 记录 |
| 数据权限从全部数据切换为自定义 | 需选择至少一个部门 |
| 菜单树父子联动关闭时 | 可独立勾选任意节点 |
| 菜单树父子联动开启时 | 勾选父节点自动勾选所有子节点 |
| 角色停用时 | 拥有该角色的用户失去对应权限 |
| 不同租户创建同名角色 | 允许（租户隔离） |
| 超级管理员角色修改数据权限 | 应被拒绝 |

---

## 8. File Inventory

### 8.1 后端文件（上游框架 org.dromara.system）

| 文件路径 | 类型 | 职责 |
|---------|------|------|
| `controller/SysRoleController.java` | Controller | 角色 CRUD、数据权限配置 |
| `controller/SysRoleAuthController.java` | Controller | 角色用户分配 |
| `service/ISysRoleService.java` | Interface | 角色服务接口 |
| `service/impl/SysRoleServiceImpl.java` | Service Impl | 角色服务实现 |
| `domain/SysRole.java` | Entity | 角色实体 |
| `domain/SysRoleMenu.java` | Entity | 角色菜单关联实体 |
| `domain/SysRoleDept.java` | Entity | 角色部门关联实体 |
| `domain/bo/SysRoleBo.java` | BO | 角色业务对象 |
| `domain/vo/SysRoleVo.java` | VO | 角色视图对象 |
| `mapper/SysRoleMapper.java` | Mapper | 角色数据访问 |
| `mapper/SysRoleMenuMapper.java` | Mapper | 角色菜单关联数据访问 |
| `mapper/SysRoleDeptMapper.java` | Mapper | 角色部门关联数据访问 |

### 8.2 前端文件

| 文件路径 | 类型 | 行数 | 职责 |
|---------|------|------|------|
| `src/views/system/role/index.vue` | 页面组件 | 503 | 角色管理主页面（列表、表单、菜单树、数据权限） |
| `src/views/system/role/authUser.vue` | 页面组件 | - | 角色用户分配页面（已分配/未分配用户列表） |
| `src/views/system/role/selectUser.vue` | 子组件 | - | 用户选择弹窗 |
| `src/api/system/role/index.ts` | API 模块 | 160 | 角色 API 客户端 |
| `src/api/system/role/types.ts` | 类型定义 | 52 | 角色 TypeScript 类型 |

### 8.3 数据库表

| 表名 | 实体类 | 主键 | 租户隔离 | 说明 |
|------|--------|------|---------|------|
| sys_role | SysRole | role_id (BIGINT) | 是 | 角色表 |
| sys_role_menu | SysRoleMenu | (role_id, menu_id) | 否（系统级表） | 角色菜单关联表 |
| sys_role_dept | SysRoleDept | (role_id, dept_id) | 否（系统级表） | 角色部门关联表（数据权限） |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
