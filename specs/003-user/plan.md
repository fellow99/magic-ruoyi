# 003-User 用户管理模块 - 技术实现方案（Plan）

> magic-ruoyi 用户管理模块技术实现方案。定义用户 CRUD、角色/岗位分配、导入导出、个人中心的技术实现细节。
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
| MyBatis-Plus | 3.5.16 | ORM 框架 |
| MySQL | 最新稳定版 | 数据存储 |
| Redis | 最新稳定版 | 缓存 |
| Vue | 3.5.22 | 前端框架 |
| Element Plus | 2.11.7 | 前端 UI 库 |

### 1.2 模块依赖

| 依赖 | 来源 | 用途 |
|------|------|------|
| `ruoyi-common-core` | org.dromara | 基础工具类、常量、异常 |
| `ruoyi-common-web` | org.dromara | BaseController、全局异常处理 |
| `ruoyi-common-excel` | org.dromara | Excel 导入导出 |
| `ruoyi-common-encrypt` | org.dromara | @ApiEncrypt 接口加密 |
| `ruoyi-common-satoken` | org.dromara | LoginHelper 登录辅助 |
| `ruoyi-common-oss` | org.dromara | 头像文件存储 |
| `ruoyi-common-log` | org.dromara | @Log 操作日志注解 |
| `ruoyi-system` | org.dromara | SysUser、SysUserRole、SysUserPost 实体/服务 |

### 1.3 包结构

> 注：用户管理的 Controller、Service、Mapper、Domain 类均继承自 RuoYi-Vue-Plus 框架（org.dromara.system 包）。

```
org.dromara.system (上游框架)
├── controller/
│   └── SysUserController.java           # 用户管理入口（CRUD、导入导出、角色分配、个人中心）
├── service/
│   ├── ISysUserService.java             # 用户服务接口
│   └── impl/
│       └── SysUserServiceImpl.java      # 用户服务实现
├── domain/
│   ├── SysUser.java                     # 用户实体
│   ├── SysUserRole.java                 # 用户角色关联实体
│   └── SysUserPost.java                 # 用户岗位关联实体
├── domain/bo/
│   └── SysUserBo.java                   # 用户业务对象
├── domain/vo/
│   └── SysUserVo.java                   # 用户视图对象
└── mapper/
    ├── SysUserMapper.java               # 用户数据访问
    ├── SysUserRoleMapper.java           # 用户角色关联数据访问
    └── SysUserPostMapper.java           # 用户岗位关联数据访问
```

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 用户 CRUD 直接复用框架能力，角色/岗位分配通过关联表实现 |
| 约定优于配置 | 合规 | 遵循 RuoYi-Vue-Plus 的分层结构，权限标识使用 system:user:* 约定 |
| 实用优于完美 | 合规 | 导入导出使用框架内置 Excel 功能，个人中心复用用户编辑逻辑 |
| 安全优于便利 | 合规 | 密码 RSA 加密传输，超级管理员保护，自我编辑限制 |
| 零样板代码 | 合规 | MyBatis-Plus BaseMapperPlus 减少 CRUD 代码，MapStruct 对象转换 |
| 多租户优先 | 合规 | 用户数据按 tenant_id 隔离，MyBatis-Plus 租户插件自动过滤 |
| 前后端分离 | 合规 | 前端独立 user/index.vue 和 profile 子组件，通过 API 客户端调用 |
| 依赖整合，非复制 | 合规 | 用户管理核心代码位于 org.dromara.system 包，通过 Maven 依赖引入 |

---

## 3. Research Findings

### 3.1 用户角色/岗位关联决策

**决策**: 使用独立的关联表（sys_user_role、sys_user_post）存储多对多关系。

**理由**:
- 符合关系型数据库范式
- 支持一个用户拥有多个角色和多个岗位
- 删除用户时级联清除关联记录
- 相比 JSON 字段存储，关联表支持高效的 JOIN 查询

### 3.2 自我编辑限制决策

**决策**: 用户编辑自己的账号时，前端自动清空 roleIds、deptId、postIds 字段。

**理由**:
- 防止用户自行提升权限（修改角色）
- 防止用户自行修改部门归属
- 后端不额外校验，依赖前端逻辑（可考虑后端增加校验）

### 3.3 超级管理员保护决策

**决策**: userId = 1 的超级管理员不可删除、不可重置密码、不可修改状态、不显示操作按钮。

**理由**:
- 防止误操作导致系统失去管理员
- 前端通过 `v-if="scope.row.userId !== 1"` 隐藏操作按钮
- 后端同样有保护逻辑

### 3.4 部门树联动决策

**决策**: 用户管理页面左侧展示部门树，点击部门节点后列表仅显示该部门及子部门下的用户。

**理由**:
- 直观的组织架构导航
- 部门树默认全部展开，方便快速定位
- 支持部门名称搜索过滤

### 3.5 导入导出决策

**决策**: 使用框架内置 Excel 导入导出功能，支持模板下载和更新已存在数据。

**理由**:
- 框架已封装完整的 Excel 处理逻辑
- 支持 @Excel 注解标注字段
- 导入结果弹窗展示成功/失败详情

---

## 4. Data Model

### 4.1 SysUser（用户实体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| user_id | BIGINT | 是 | 主键 |
| tenant_id | VARCHAR(20) | 是 | 租户编号 |
| dept_id | BIGINT | 否 | 部门 ID |
| user_name | VARCHAR(30) | 是 | 用户名称（租户内唯一） |
| nick_name | VARCHAR(30) | 是 | 用户昵称 |
| user_type | VARCHAR(10) | 是 | 用户类型（sys_user=系统用户） |
| email | VARCHAR(50) | 否 | 邮箱（租户内唯一） |
| phonenumber | VARCHAR(11) | 否 | 手机号（租户内唯一） |
| sex | CHAR(1) | 否 | 性别（0=男, 1=女, 2=未知） |
| avatar | VARCHAR(100) | 否 | 头像地址 |
| password | VARCHAR(100) | 是 | 密码（BCrypt 加密） |
| status | CHAR(1) | 是 | 状态（0=正常, 1=停用） |
| del_flag | CHAR(1) | 是 | 删除标志（0=正常, 1=删除） |
| login_ip | VARCHAR(128) | 否 | 最后登录 IP |
| login_date | DATETIME | 否 | 最后登录时间 |
| remark | VARCHAR(500) | 否 | 备注 |
| create_dept | BIGINT | 否 | 创建部门 |
| create_by | BIGINT | 否 | 创建者 |
| create_time | DATETIME | 否 | 创建时间 |
| update_by | BIGINT | 否 | 更新者 |
| update_time | DATETIME | 否 | 更新时间 |

### 4.2 关联表

#### sys_user_role（用户角色关联）

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 用户 ID |
| role_id | BIGINT | 角色 ID |

#### sys_user_post（用户岗位关联）

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 用户 ID |
| post_id | BIGINT | 岗位 ID |

### 4.3 前端类型定义

#### UserVO

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | string/number | 用户 ID |
| tenantId | string | 租户编号 |
| deptId | number | 部门 ID |
| userName | string | 用户名称 |
| nickName | string | 用户昵称 |
| userType | string | 用户类型 |
| email | string | 邮箱 |
| phonenumber | string | 手机号 |
| sex | string | 性别 |
| avatar | string | 头像地址 |
| status | string | 状态 |
| delFlag | string | 删除标志 |
| loginIp | string | 最后登录 IP |
| loginDate | string | 最后登录时间 |
| remark | string | 备注 |
| deptName | string | 部门名称 |
| roles | RoleVO[] | 角色列表 |
| roleIds | any | 角色 ID 数组 |
| postIds | any | 岗位 ID 数组 |
| admin | boolean | 是否超级管理员 |

#### UserForm

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | string | 用户 ID |
| deptId | number | 部门 ID |
| userName | string | 用户名称 |
| nickName | string | 用户昵称 |
| password | string | 密码 |
| phonenumber | string | 手机号 |
| email | string | 邮箱 |
| sex | string | 性别 |
| status | string | 状态 |
| remark | string | 备注 |
| postIds | string[] | 岗位 ID 数组 |
| roleIds | string[] | 角色 ID 数组 |

#### UserInfoVO（用户详情响应）

| 字段 | 类型 | 说明 |
|------|------|------|
| user | UserVO | 用户信息 |
| roles | RoleVO[] | 可选角色列表 |
| roleIds | string[] | 已选角色 ID |
| posts | PostVO[] | 可选岗位列表 |
| postIds | string[] | 已选岗位 ID |
| roleGroup | string | 角色组名称 |
| postGroup | string | 岗位组名称 |

### 4.4 状态转换

```
[正常(0)] ──changeStatus──▶ [停用(1)]
    │                            │
    │  停用后用户无法登录          │  重新启用
    ▼                            ▼
  禁止登录                     允许登录

[未删除] ──delete──▶ [已删除(del_flag=1)]
```

### 4.5 验证规则

| 规则 | 适用场景 | 验证方式 |
|------|---------|---------|
| 用户名称 | 新增/修改 | 必填，2-20 字符，租户内唯一 |
| 用户昵称 | 新增/修改 | 必填，最长 30 字符 |
| 密码 | 新增 | 必填，5-20 字符，不可包含 `< > " ' \|` |
| 邮箱 | 新增/修改 | 可选，标准邮箱格式，租户内唯一 |
| 手机号 | 新增/修改 | 可选，`1[3456789][0-9]\d{8}`，租户内唯一 |
| 角色 | 新增/修改 | 必填，至少选择一个角色 |
| 部门 | 新增/修改 | 可选，树形选择 |
| 岗位 | 新增/修改 | 可选，多选，仅显示正常状态的岗位 |

---

## 5. Interface Contracts

### 5.1 提供接口（后端 API）

#### 用户管理接口（/system/user）

| 方法 | 路径 | 权限 | 加密 | 说明 |
|------|------|------|------|------|
| GET | /system/user/list | system:user:list | 否 | 分页查询用户列表 |
| GET | /system/user/export | system:user:export | 否 | 导出用户数据（Excel） |
| GET | /system/user/importTemplate | system:user:import | 否 | 下载导入模板 |
| POST | /system/user/importData | system:user:import | 否 | 导入用户数据 |
| GET | /system/user/optionselect | - | 否 | 按用户 ID 查询用户列表（下拉选） |
| GET | /system/user/{userId} | system:user:query | 否 | 查询用户详情（含角色/岗位） |
| POST | /system/user | system:user:add | 否 | 新增用户 |
| PUT | /system/user | system:user:edit | 否 | 修改用户 |
| DELETE | /system/user/{userIds} | system:user:remove | 否 | 删除用户 |
| PUT | /system/user/changeStatus | system:user:edit | 否 | 切换用户状态 |
| PUT | /system/user/resetPwd | system:user:resetPwd | 是 | 重置用户密码 |
| GET | /system/user/deptTree | - | 否 | 查询部门树 |
| GET | /system/user/list/dept/{deptId} | - | 否 | 查询部门下所有用户 |

#### 个人中心接口（/system/user/profile）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /system/user/profile | 查询当前用户个人信息 |
| PUT | /system/user/profile | 修改当前用户个人信息 |
| PUT | /system/user/profile/updatePwd | 修改当前用户密码（加密） |
| POST | /system/user/profile/avatar | 上传用户头像 |

#### 角色分配接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /system/user/authRole/{userId} | system:user:edit | 查询用户角色分配情况 |
| PUT | /system/user/authRole | system:user:edit | 保存用户角色分配 |

### 5.2 消费接口（依赖上游）

| 接口 | 来源 | 用途 |
|------|------|------|
| ISysDeptService | ruoyi-system | 部门树数据 |
| ISysRoleService | ruoyi-system | 角色列表 |
| ISysPostService | ruoyi-system | 岗位列表 |
| ISysConfigService | ruoyi-system | 获取默认初始密码 |
| ISysOssService | ruoyi-system | 头像文件存储 |

### 5.3 前端 API 客户端

| 函数 | 文件 | 方法 | 路径 | 特殊头 |
|------|------|------|------|--------|
| listUser() | api/system/user/index.ts | GET | /system/user/list | - |
| getUser() | api/system/user/index.ts | GET | /system/user/:id | - |
| addUser() | api/system/user/index.ts | POST | /system/user | - |
| updateUser() | api/system/user/index.ts | PUT | /system/user | - |
| delUser() | api/system/user/index.ts | DELETE | /system/user/:ids | - |
| resetUserPwd() | api/system/user/index.ts | PUT | /system/user/resetPwd | isEncrypt: true |
| changeUserStatus() | api/system/user/index.ts | PUT | /system/user/changeStatus | - |
| getUserProfile() | api/system/user/index.ts | GET | /system/user/profile | - |
| updateUserProfile() | api/system/user/index.ts | PUT | /system/user/profile | - |
| updateUserPwd() | api/system/user/index.ts | PUT | /system/user/profile/updatePwd | isEncrypt: true |
| uploadAvatar() | api/system/user/index.ts | POST | /system/user/profile/avatar | - |
| getAuthRole() | api/system/user/index.ts | GET | /system/user/authRole/:id | - |
| updateAuthRole() | api/system/user/index.ts | PUT | /system/user/authRole | - |
| deptTreeSelect() | api/system/user/index.ts | GET | /system/user/deptTree | - |
| listUserByDeptId() | api/system/user/index.ts | GET | /system/user/list/dept/:id | - |

---

## 6. Implementation Strategy

### 6.1 架构模式

**部门树 + 列表联动模式**:

```
部门树（左侧）                    用户列表（右侧）
    │                                │
    │  点击部门节点                   │
    ├───────────────────────────────▶│
    │                                │  按 deptId 查询
    │                                │  包含子部门用户
    │                                │
    │  部门名称搜索                   │
    ├───────────────────────────────▶│
    │                                │  过滤部门树节点
```

**用户表单自我编辑限制**:

```
用户编辑自己?
    │
    ├── 是 ──▶ 提交前清空 roleIds、deptId、postIds
    │
    └── 否 ──▶ 正常提交所有字段
```

### 6.2 关键算法

#### 6.2.1 新增用户流程

```
1. 前端获取部门树、角色列表、岗位列表
2. 设置默认密码（从 sys.user.initPassword 配置获取）
3. 用户填写表单（用户名、昵称、密码、部门、岗位、角色等）
4. 前端校验（用户名长度、密码规则、手机号格式、邮箱格式）
5. 提交到后端
6. 后端校验（用户名唯一性、手机号唯一性、邮箱唯一性）
7. BCrypt 加密密码
8. 插入 sys_user 记录
9. 插入 sys_user_role 关联记录
10. 插入 sys_user_post 关联记录
11. 返回成功
```

#### 6.2.2 用户导入流程

```
1. 用户下载 Excel 模板
2. 填写用户数据
3. 上传 Excel 文件
4. 后端解析 Excel
5. 逐行校验数据:
   a. 用户名是否重复
   b. 手机号/邮箱格式
   c. 部门是否存在
   d. 角色是否存在
6. 若 updateSupport=true 且用户已存在，则更新
7. 若用户不存在，则新增
8. 返回导入结果（成功数、失败数、失败原因）
```

#### 6.2.3 角色分配流程

```
1. 跳转到 /system/user-auth/role/:userId 页面
2. 查询用户信息和全部角色列表
3. 勾选用户已拥有的角色（回显）
4. 提交选中的角色 ID 列表
5. 后端:
   a. 删除该用户的所有角色关联记录
   b. 批量插入新的角色关联记录
6. 返回成功
```

### 6.3 错误处理

| 异常场景 | 处理方式 |
|---------|---------|
| 用户名重复 | 返回错误提示 |
| 手机号重复 | 返回错误提示 |
| 邮箱重复 | 返回错误提示 |
| 删除超级管理员 | 前端隐藏按钮，后端保护 |
| 用户编辑自己时修改角色/部门/岗位 | 前端自动清空，后端可考虑增加校验 |
| 导入 Excel 格式错误 | 返回详细错误信息 |
| 导入数据校验失败 | 记录失败原因，继续处理其他行 |

### 6.4 性能优化

| 优化点 | 策略 |
|--------|------|
| 列表查询 | 分页查询，默认 10 条/页 |
| 部门树 | 一次性加载全部，前端过滤 |
| 用户搜索 | 支持用户名、昵称、手机号、状态、部门、时间范围筛选 |
| 导出操作 | 使用下载流方式，不阻塞主线程 |
| 导入操作 | 批量处理，返回详细结果 |

### 6.5 安全控制

| 安全措施 | 实现方式 |
|---------|---------|
| 密码加密存储 | BCrypt |
| 密码传输加密 | @ApiEncrypt + RSA |
| 超级管理员保护 | userId=1 不可删除/重置密码/修改状态 |
| 自我编辑限制 | 前端清空 roleIds/deptId/postIds |
| 租户数据隔离 | MyBatis-Plus 租户插件自动过滤 |
| 权限控制 | @SaCheckPermission 注解 |
| 操作日志 | @Log 注解记录写操作 |
| XSS 防护 | 用户输入转义展示 |

---

## 7. Testing Considerations

### 7.1 可测试性设计

- Service 层通过接口定义，便于 Mock
- 用户唯一性校验可独立测试
- 导入导出逻辑可 Mock Excel 数据

### 7.2 测试类别

| 测试类型 | 测试对象 | 工具 |
|---------|---------|------|
| 单元测试 | SysUserServiceImpl | JUnit 5 + Mockito |
| 单元测试 | 用户唯一性校验 | JUnit 5 + Mockito |
| 集成测试 | SysUserController | Spring Boot Test + MockMvc |
| 集成测试 | Excel 导入导出 | Spring Boot Test |
| 前端测试 | user/index.vue 表单校验 | Vitest + Vue Test Utils |

### 7.3 边缘情况

| 场景 | 预期行为 |
|------|---------|
| 新增用户时用户名已存在 | 返回错误提示 |
| 新增用户时手机号已存在 | 返回错误提示 |
| 新增用户时邮箱已存在 | 返回错误提示 |
| 删除有角色关联的用户 | 级联删除 sys_user_role 记录 |
| 删除有岗位关联的用户 | 级联删除 sys_user_post 记录 |
| 导入 Excel 中部分行数据错误 | 记录错误，继续处理其他行 |
| 用户编辑自己时尝试修改角色 | 前端自动清空 roleIds |
| 部门已停用时选择该部门 | 前端过滤掉停用的部门 |
| 岗位已停用时选择该岗位 | 前端禁用该岗位选项 |
| 角色已停用时选择该角色 | 前端禁用该角色选项 |

---

## 8. File Inventory

### 8.1 后端文件（上游框架 org.dromara.system）

| 文件路径 | 类型 | 职责 |
|---------|------|------|
| `controller/SysUserController.java` | Controller | 用户 CRUD、导入导出、角色分配、个人中心 |
| `service/ISysUserService.java` | Interface | 用户服务接口 |
| `service/impl/SysUserServiceImpl.java` | Service Impl | 用户服务实现 |
| `domain/SysUser.java` | Entity | 用户实体 |
| `domain/SysUserRole.java` | Entity | 用户角色关联实体 |
| `domain/SysUserPost.java` | Entity | 用户岗位关联实体 |
| `domain/bo/SysUserBo.java` | BO | 用户业务对象 |
| `domain/vo/SysUserVo.java` | VO | 用户视图对象 |
| `mapper/SysUserMapper.java` | Mapper | 用户数据访问 |
| `mapper/SysUserRoleMapper.java` | Mapper | 用户角色关联数据访问 |
| `mapper/SysUserPostMapper.java` | Mapper | 用户岗位关联数据访问 |

### 8.2 前端文件

| 文件路径 | 类型 | 行数 | 职责 |
|---------|------|------|------|
| `src/views/system/user/index.vue` | 页面组件 | 679 | 用户管理主页面（列表、表单、部门树、导入导出） |
| `src/views/system/user/authRole.vue` | 页面组件 | - | 用户角色分配页面 |
| `src/views/system/user/profile/index.vue` | 页面组件 | - | 个人中心主页 |
| `src/views/system/user/profile/userInfo.vue` | 子组件 | - | 个人信息编辑 |
| `src/views/system/user/profile/userAvatar.vue` | 子组件 | - | 头像上传 |
| `src/views/system/user/profile/resetPwd.vue` | 子组件 | - | 修改密码 |
| `src/views/system/user/profile/thirdParty.vue` | 子组件 | - | 第三方账号管理 |
| `src/views/system/user/profile/onlineDevice.vue` | 子组件 | - | 在线设备管理 |
| `src/api/system/user/index.ts` | API 模块 | 229 | 用户 API 客户端 |
| `src/api/system/user/types.ts` | 类型定义 | 86 | 用户 TypeScript 类型 |

### 8.3 数据库表

| 表名 | 实体类 | 主键 | 租户隔离 | 说明 |
|------|--------|------|---------|------|
| sys_user | SysUser | user_id (BIGINT) | 是 | 用户表 |
| sys_user_role | SysUserRole | (user_id, role_id) | 否（系统级表） | 用户角色关联表 |
| sys_user_post | SysUserPost | (user_id, post_id) | 否（系统级表） | 用户岗位关联表 |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
