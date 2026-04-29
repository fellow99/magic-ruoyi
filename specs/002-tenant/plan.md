# 002-Tenant 租户管理模块 - 技术实现方案（Plan）

> magic-ruoyi 租户管理模块技术实现方案。定义多租户架构、租户 CRUD、套餐管理、数据同步的技术实现细节。
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
| MyBatis-Plus | 3.5.16 | ORM 框架 + 多租户插件 |
| MySQL | 最新稳定版 | 数据存储 |
| Redis | 最新稳定版 | 缓存、分布式锁 |
| Vue | 3.5.22 | 前端框架 |
| Element Plus | 2.11.7 | 前端 UI 库 |

### 1.2 模块依赖

| 依赖 | 来源 | 用途 |
|------|------|------|
| `ruoyi-common-tenant` | org.dromara | TenantHelper、多租户插件配置 |
| `ruoyi-common-core` | org.dromara | 基础工具类、常量、异常 |
| `ruoyi-common-web` | org.dromara | BaseController、全局异常处理 |
| `ruoyi-common-excel` | org.dromara | Excel 导入导出 |
| `ruoyi-common-encrypt` | org.dromara | @ApiEncrypt 接口加密 |
| `ruoyi-common-satoken` | org.dromara | LoginHelper 登录辅助 |
| `ruoyi-system` | org.dromara | SysTenant、SysTenantPackage 实体/服务 |
| `ruoyi-common-log` | org.dromara | @Log 操作日志注解 |

### 1.3 包结构

> 注：租户管理的 Controller、Service、Mapper、Domain 类均继承自 RuoYi-Vue-Plus 框架（org.dromara.system 包），自定义代码仅扩展 VO 对象。

```
org.dromara.system (上游框架)
├── controller/
│   ├── SysTenantController.java         # 租户管理入口
│   └── SysTenantPackageController.java  # 租户套餐管理入口
├── service/
│   ├── ISysTenantService.java           # 租户服务接口
│   ├── ISysTenantPackageService.java    # 租户套餐服务接口
│   └── impl/
│       ├── SysTenantServiceImpl.java    # 租户服务实现
│       └── SysTenantPackageServiceImpl.java  # 租户套餐服务实现
├── domain/
│   ├── SysTenant.java                   # 租户实体
│   └── SysTenantPackage.java            # 租户套餐实体
├── domain/bo/
│   ├── SysTenantBo.java                 # 租户业务对象
│   └── SysTenantPackageBo.java          # 租户套餐业务对象
└── domain/vo/
    ├── SysTenantVo.java                 # 租户视图对象
    └── SysTenantPackageVo.java          # 租户套餐视图对象

org.fellow99.magic.ruoyi (自定义扩展)
└── domain/vo/
    ├── TenantListVo.java                # 登录页租户列表项
    └── LoginTenantVo.java               # 登录页租户响应
```

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 多租户通过 MyBatis-Plus 插件自动注入 tenant_id 过滤条件，无需手动编写 SQL |
| 约定优于配置 | 合规 | 遵循 RuoYi-Vue-Plus 的 Controller/Service/Mapper 分层结构，权限标识使用 system:tenant:* 约定 |
| 实用优于完美 | 合规 | 租户 CRUD 直接复用框架能力，仅在需要时扩展（如登录页租户下拉） |
| 安全优于便利 | 合规 | 新增租户使用 @ApiEncrypt 加密传输用户名/密码，@RepeatSubmit 防重复提交 |
| 零样板代码 | 合规 | 使用 MapStruct 对象转换，MyBatis-Plus BaseMapperPlus 减少 CRUD 代码 |
| 多租户优先 | 合规 | 所有业务表默认启用租户隔离，系统级表（sys_menu、sys_tenant 等）显式排除 |
| 前后端分离 | 合规 | 前端独立 tenant/index.vue 和 tenantPackage/index.vue，通过 API 客户端调用后端 |
| 依赖整合，非复制 | 合规 | 租户管理核心代码位于 org.dromara.system 包，通过 Maven 依赖引入 |

---

## 3. Research Findings

### 3.1 多租户实现决策

**决策**: 使用 MyBatis-Plus 租户插件实现行级逻辑隔离。

**理由**:
- 所有业务表包含 `tenant_id` 字段
- MyBatis-Plus 拦截器自动在 SQL 中追加 `WHERE tenant_id = ?`
- 系统级表通过 `tenant.excludes` 配置排除过滤
- 相比 Schema 隔离或 Database 隔离，行级隔离成本最低，适合 SaaS 场景

**排除表清单**（不受租户过滤）:

```
sys_menu, sys_tenant, sys_tenant_package, sys_role_dept,
sys_role_menu, sys_user_post, sys_user_role, sys_client,
sys_oss_config, flow_spel
```

### 3.2 租户编号生成决策

**决策**: `tenant_id` 为 VARCHAR(20) 类型，由系统自动生成。

**理由**:
- 超级租户固定为 `000000`
- 新租户编号由框架自动生成（雪花算法或自定义规则）
- VARCHAR 类型支持未来扩展为有意义的编号格式

### 3.3 套餐菜单存储决策

**决策**: 套餐的 `menu_ids` 存储为逗号分隔的字符串（如 `"1,2,3,100,101"`）。

**理由**:
- 简化存储结构，无需额外的关联表
- 前端通过 el-tree 勾选后直接转为逗号分隔字符串
- 查询时通过 FIND_IN_SET 或字符串匹配即可

### 3.4 数据同步决策

**决策**: 平台级数据（字典、参数）通过遍历所有租户并复制数据实现同步。

**理由**:
- 字典和参数数据量小，同步操作可在 10 秒内完成
- 使用 `TenantHelper.dynamic(tenantId, () -> {...})` 切换租户上下文
- 同步操作仅限超级管理员（userId = 1）执行

---

## 4. Data Model

### 4.1 SysTenant（租户实体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键 |
| tenant_id | VARCHAR(20) | 是 | 租户编号（唯一标识） |
| username | VARCHAR(60) | 是 | 用户名（创建租户管理员） |
| password | VARCHAR(100) | 是 | 密码（BCrypt 加密） |
| contact_user_name | VARCHAR(20) | 是 | 联系人 |
| contact_phone | VARCHAR(20) | 是 | 联系电话 |
| company_name | VARCHAR(30) | 是 | 企业名称 |
| license_number | VARCHAR(30) | 否 | 统一社会信用代码 |
| domain | VARCHAR(200) | 否 | 绑定域名 |
| address | VARCHAR(256) | 否 | 企业地址 |
| intro | VARCHAR(200) | 否 | 企业简介 |
| remark | VARCHAR(200) | 否 | 备注 |
| package_id | BIGINT | 是 | 租户套餐 ID |
| expire_time | DATETIME | 否 | 过期时间（空表示永不过期） |
| account_count | INT | 是 | 用户数量上限（-1 表示不限制） |
| status | CHAR(1) | 是 | 状态（0=正常, 1=停用） |
| del_flag | CHAR(1) | 是 | 删除标志（0=正常, 1=删除） |
| create_dept | BIGINT | 否 | 创建部门 |
| create_by | BIGINT | 否 | 创建者 |
| create_time | DATETIME | 否 | 创建时间 |
| update_by | BIGINT | 否 | 更新者 |
| update_time | DATETIME | 否 | 更新时间 |

### 4.2 SysTenantPackage（租户套餐实体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| package_id | BIGINT | 是 | 主键 |
| package_name | VARCHAR(20) | 是 | 套餐名称 |
| menu_ids | TEXT | 否 | 关联菜单 ID（逗号分隔） |
| remark | VARCHAR(200) | 否 | 备注 |
| menu_check_strictly | CHAR(1) | 是 | 菜单树父子联动（0=否, 1=是） |
| status | CHAR(1) | 是 | 状态（0=正常, 1=停用） |
| del_flag | CHAR(1) | 是 | 删除标志 |
| create_dept | BIGINT | 否 | 创建部门 |
| create_by | BIGINT | 否 | 创建者 |
| create_time | DATETIME | 否 | 创建时间 |
| update_by | BIGINT | 否 | 更新者 |
| update_time | DATETIME | 否 | 更新时间 |

### 4.3 前端类型定义

#### TenantVO / TenantForm

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number/string | 主键 |
| tenantId | number/string | 租户编号 |
| username | string | 用户名（仅新增时） |
| password | string | 密码（仅新增时） |
| contactUserName | string | 联系人 |
| contactPhone | string | 联系电话 |
| companyName | string | 企业名称 |
| licenseNumber | string | 统一社会信用代码 |
| domain | string | 绑定域名 |
| address | string | 企业地址 |
| intro | string | 企业简介 |
| remark | string | 备注 |
| packageId | string/number | 租户套餐 ID |
| expireTime | string | 过期时间 |
| accountCount | number | 用户数量上限 |
| status | string | 状态 |

#### TenantPkgVO / TenantPkgForm

| 字段 | 类型 | 说明 |
|------|------|------|
| packageId | string/number | 套餐 ID |
| packageName | string | 套餐名称 |
| menuIds | string | 关联菜单 ID（逗号分隔） |
| remark | string | 备注 |
| menuCheckStrictly | boolean | 菜单树父子联动 |
| status | string | 状态 |

### 4.4 状态转换

#### 租户状态

```
[正常(0)] ──changeStatus──▶ [停用(1)]
    │                            │
    │  停用后该租户用户无法登录    │  重新启用
    ▼                            ▼
  禁止登录                     允许登录

[未过期] ──expire_time 到期──▶ [已过期] ──▶ 自动停用(status=1)
```

#### 套餐状态

```
[正常(0)] ──changeStatus──▶ [停用(1)]
    │                            │
    │  停用后新租户不可选此套餐    │  重新启用
    ▼                            ▼
  不可分配                     可分配
```

### 4.5 验证规则

| 规则 | 适用场景 | 验证方式 |
|------|---------|---------|
| 企业名称 | 新增/修改 | 必填，最长 30 字符 |
| 联系人 | 新增/修改 | 必填，最长 20 字符 |
| 联系电话 | 新增/修改 | 必填，最长 20 字符 |
| 用户名 | 新增 | 必填，2-20 字符 |
| 密码 | 新增 | 必填，5-20 字符 |
| 套餐名称 | 新增/修改套餐 | 必填，最长 20 字符 |
| 过期时间 | 新增/修改 | 可选，DATETIME 格式 |
| 用户数量 | 新增/修改 | 必填，-1 表示不限制 |

---

## 5. Interface Contracts

### 5.1 提供接口（后端 API）

#### 租户管理接口（/system/tenant）

| 方法 | 路径 | 权限 | 加密 | 说明 |
|------|------|------|------|------|
| GET | /system/tenant/list | system:tenant:list | 否 | 分页查询租户列表 |
| GET | /system/tenant/export | system:tenant:export | 否 | 导出租户数据（Excel） |
| GET | /system/tenant/{id} | system:tenant:query | 否 | 查询租户详情 |
| POST | /system/tenant | system:tenant:add | 是 | 新增租户（含初始化租户管理员） |
| PUT | /system/tenant | system:tenant:edit | 否 | 修改租户 |
| DELETE | /system/tenant/{ids} | system:tenant:remove | 否 | 删除租户（逻辑删除） |
| PUT | /system/tenant/changeStatus | system:tenant:edit | 否 | 切换租户状态 |
| GET | /system/tenant/dynamic/{tenantId} | - | 否 | 动态切换到指定租户上下文 |
| GET | /system/tenant/dynamic/clear | - | 否 | 清除动态租户切换 |
| GET | /system/tenant/syncTenantPackage | system:tenant:edit | 否 | 同步租户套餐 |
| GET | /system/tenant/syncTenantDict | 超级管理员 | 否 | 同步平台字典到所有租户 |
| GET | /system/tenant/syncTenantConfig | 超级管理员 | 否 | 同步平台参数到所有租户 |

#### 租户套餐接口（/system/tenant/package）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /system/tenant/package/list | system:tenantPackage:list | 分页查询套餐列表 |
| GET | /system/tenant/package/selectList | - | 查询套餐下拉选列表 |
| GET | /system/tenant/package/{packageId} | system:tenantPackage:query | 查询套餐详情 |
| POST | /system/tenant/package | system:tenantPackage:add | 新增套餐 |
| PUT | /system/tenant/package | system:tenantPackage:edit | 修改套餐 |
| DELETE | /system/tenant/package/{ids} | system:tenantPackage:remove | 删除套餐 |
| PUT | /system/tenant/package/changeStatus | system:tenantPackage:edit | 切换套餐状态 |

### 5.2 消费接口（依赖上游）

| 接口 | 来源 | 用途 |
|------|------|------|
| ISysMenuService.selectMenuTreeByPackageId() | ruoyi-system | 查询套餐关联的菜单树 |
| ISysDictDataService | ruoyi-system | 同步字典数据 |
| ISysConfigService | ruoyi-system | 同步参数配置 |
| ISysUserService | ruoyi-system | 创建租户管理员账号 |
| TenantHelper.dynamic() | ruoyi-common-tenant | 动态切换租户上下文 |

### 5.3 前端 API 客户端

| 函数 | 文件 | 方法 | 路径 | 特殊头 |
|------|------|------|------|--------|
| listTenant() | api/system/tenant/index.ts | GET | /system/tenant/list | - |
| getTenant() | api/system/tenant/index.ts | GET | /system/tenant/:id | - |
| addTenant() | api/system/tenant/index.ts | POST | /system/tenant | isEncrypt: true |
| updateTenant() | api/system/tenant/index.ts | PUT | /system/tenant | - |
| changeTenantStatus() | api/system/tenant/index.ts | PUT | /system/tenant/changeStatus | - |
| delTenant() | api/system/tenant/index.ts | DELETE | /system/tenant/:ids | - |
| dynamicTenant() | api/system/tenant/index.ts | GET | /system/tenant/dynamic/:tenantId | - |
| dynamicClear() | api/system/tenant/index.ts | GET | /system/tenant/dynamic/clear | - |
| syncTenantPackage() | api/system/tenant/index.ts | GET | /system/tenant/syncTenantPackage | - |
| syncTenantDict() | api/system/tenant/index.ts | GET | /system/tenant/syncTenantDict | - |
| syncTenantConfig() | api/system/tenant/index.ts | GET | /system/tenant/syncTenantConfig | - |
| listTenantPackage() | api/system/tenantPackage/index.ts | GET | /system/tenant/package/list | - |
| selectTenantPackage() | api/system/tenantPackage/index.ts | GET | /system/tenant/package/selectList | - |
| getTenantPackage() | api/system/tenantPackage/index.ts | GET | /system/tenant/package/:id | - |
| addTenantPackage() | api/system/tenantPackage/index.ts | POST | /system/tenant/package | - |
| updateTenantPackage() | api/system/tenantPackage/index.ts | PUT | /system/tenant/package | - |
| changePackageStatus() | api/system/tenantPackage/index.ts | PUT | /system/tenant/package/changeStatus | - |
| delTenantPackage() | api/system/tenantPackage/index.ts | DELETE | /system/tenant/package/:ids | - |

---

## 6. Implementation Strategy

### 6.1 架构模式

**多租户插件模式**: MyBatis-Plus 拦截器自动注入 tenant_id 过滤条件。

```
请求进入
    │
    ▼
TenantHelper.dynamic(tenantId, () -> {...})
    │  设置当前线程租户上下文
    ▼
MyBatis-Plus 租户拦截器
    │  自动在 SQL 中追加 WHERE tenant_id = ?
    ▼
执行 SQL（自动过滤非当前租户数据）
    │
    ▼
返回结果
```

**套餐菜单树模式**: 前端通过 el-tree 勾选菜单，保存为逗号分隔字符串。

```
菜单树数据 ──▶ el-tree（勾选/半勾选） ──▶ getCheckedKeys() + getHalfCheckedKeys()
                                                    │
                                                    ▼
                                            合并为逗号分隔字符串
                                                    │
                                                    ▼
                                            存入 menu_ids 字段
```

### 6.2 关键算法

#### 6.2.1 新增租户流程

```
1. 接收 TenantForm（含 username、password、packageId 等）
2. 参数校验（@Validated）
3. 生成 tenant_id（框架自动生成）
4. BCrypt 加密密码
5. 插入 sys_tenant 记录
6. 初始化租户管理员账号:
   a. 切换到新租户上下文（TenantHelper.dynamic）
   b. 创建 SysUser 记录（用户名、密码、角色=租户管理员）
   c. 分配默认角色和权限
7. 返回成功
```

#### 6.2.2 同步租户套餐流程

```
1. 接收 tenantId 和 packageId
2. 查询套餐的 menu_ids
3. 切换到目标租户上下文
4. 更新租户的菜单权限为套餐配置的菜单列表
5. 清除相关缓存
6. 返回成功
```

#### 6.2.3 同步平台字典流程

```
1. 查询平台级（主户 tenant_id=000000）全部字典数据
2. 查询全部租户列表
3. 遍历每个租户:
   a. TenantHelper.dynamic(tenantId, () -> {...})
   b. 复制字典数据到当前租户
   c. 清除字典缓存
4. 返回同步结果
```

### 6.3 错误处理

| 异常场景 | 处理方式 |
|---------|---------|
| 套餐已停用 | 不可分配给新租户 |
| 租户已过期 | 自动停用，用户无法登录 |
| 租户已停用 | 用户无法登录 |
| 删除有用户的租户 | 需先处理用户数据 |
| 同步字典/参数非超级管理员 | 拒绝操作 |

### 6.4 性能优化

| 优化点 | 策略 |
|--------|------|
| 列表查询 | 分页查询，默认 10 条/页 |
| 租户搜索 | 支持 tenantId、联系人、电话、企业名称模糊搜索 |
| 套餐搜索 | 支持 packageName 模糊搜索 |
| 导出操作 | 使用下载流方式，不阻塞主线程 |
| 同步操作 | 遍历租户时批量处理，目标 10 秒内完成 |

### 6.5 安全控制

| 安全措施 | 实现方式 |
|---------|---------|
| 新增租户加密 | @ApiEncrypt 加密用户名/密码 |
| 防重复提交 | 新增租户接口禁用重复提交保护（repeatSubmit: false） |
| 租户数据隔离 | MyBatis-Plus 租户插件自动过滤 |
| 权限控制 | @SaCheckPermission 注解校验操作权限 |
| 同步操作限制 | 仅限超级管理员（userId = 1） |
| 逻辑删除 | del_flag 标记，数据不物理删除 |

---

## 7. Testing Considerations

### 7.1 可测试性设计

- Service 层通过接口定义，便于 Mock
- 租户插件可通过配置排除表进行测试
- 同步操作可 Mock 租户列表和字典数据

### 7.2 测试类别

| 测试类型 | 测试对象 | 工具 |
|---------|---------|------|
| 单元测试 | SysTenantServiceImpl | JUnit 5 + Mockito |
| 单元测试 | SysTenantPackageServiceImpl | JUnit 5 + Mockito |
| 集成测试 | SysTenantController | Spring Boot Test + MockMvc |
| 集成测试 | 租户插件自动过滤 | Spring Boot Test |
| 前端测试 | tenant/index.vue 表单校验 | Vitest + Vue Test Utils |

### 7.3 边缘情况

| 场景 | 预期行为 |
|------|---------|
| 新增租户时套餐已停用 | 应允许分配（套餐停用仅影响新租户选择） |
| 租户过期时间已到 | 自动停用，用户登录被拒绝 |
| 租户用户数达到上限 | 创建新用户时被拒绝 |
| 删除超级租户（000000） | 应被保护，不可删除 |
| 同步字典时某租户同步失败 | 记录错误，继续同步其他租户 |
| 动态切换租户后未清除 | 后续请求仍在该租户上下文中 |
| 套餐菜单包含已删除菜单 | 同步时忽略不存在的菜单 ID |

---

## 8. File Inventory

### 8.1 后端文件（上游框架 org.dromara.system）

| 文件路径 | 类型 | 职责 |
|---------|------|------|
| `controller/SysTenantController.java` | Controller | 租户 CRUD、状态切换、同步、动态切换 |
| `controller/SysTenantPackageController.java` | Controller | 租户套餐 CRUD、状态切换 |
| `service/ISysTenantService.java` | Interface | 租户服务接口 |
| `service/ISysTenantPackageService.java` | Interface | 租户套餐服务接口 |
| `service/impl/SysTenantServiceImpl.java` | Service Impl | 租户服务实现 |
| `service/impl/SysTenantPackageServiceImpl.java` | Service Impl | 租户套餐服务实现 |
| `domain/SysTenant.java` | Entity | 租户实体 |
| `domain/SysTenantPackage.java` | Entity | 租户套餐实体 |
| `domain/bo/SysTenantBo.java` | BO | 租户业务对象 |
| `domain/bo/SysTenantPackageBo.java` | BO | 租户套餐业务对象 |
| `domain/vo/SysTenantVo.java` | VO | 租户视图对象 |
| `domain/vo/SysTenantPackageVo.java` | VO | 租户套餐视图对象 |
| `mapper/SysTenantMapper.java` | Mapper | 租户数据访问 |
| `mapper/SysTenantPackageMapper.java` | Mapper | 租户套餐数据访问 |

### 8.2 后端文件（自定义 org.fellow99）

| 文件路径 | 类型 | 职责 |
|---------|------|------|
| `domain/vo/TenantListVo.java` | VO | 登录页租户列表项 |
| `domain/vo/LoginTenantVo.java` | VO | 登录页租户响应 |

### 8.3 前端文件

| 文件路径 | 类型 | 行数 | 职责 |
|---------|------|------|------|
| `src/views/system/tenant/index.vue` | 页面组件 | 382 | 租户管理页面（列表、表单、状态切换、同步） |
| `src/views/system/tenantPackage/index.vue` | 页面组件 | 329 | 租户套餐管理页面（列表、表单、菜单树） |
| `src/api/system/tenant/index.ts` | API 模块 | 109 | 租户 API 客户端 |
| `src/api/system/tenant/types.ts` | 类型定义 | 46 | 租户 TypeScript 类型 |
| `src/api/system/tenantPackage/index.ts` | API 模块 | 67 | 租户套餐 API 客户端 |
| `src/api/system/tenantPackage/types.ts` | 类型定义 | 20 | 租户套餐 TypeScript 类型 |

### 8.4 数据库表

| 表名 | 实体类 | 主键 | 租户隔离 | 说明 |
|------|--------|------|---------|------|
| sys_tenant | SysTenant | id (BIGINT) | 否（系统级表） | 租户表 |
| sys_tenant_package | SysTenantPackage | package_id (BIGINT) | 否（系统级表） | 租户套餐表 |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
