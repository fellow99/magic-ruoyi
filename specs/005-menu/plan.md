# 菜单管理模块技术实现方案 (plan.md)

> magic-ruoyi 菜单管理模块。负责菜单的增删改查、树形结构管理、权限标识配置和动态路由生成。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. Technical Context

### 1.1 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot 3.x + MyBatis-Plus |
| 权限框架 | Sa-Token |
| 缓存 | Redis (字典缓存、路由缓存) |
| 前端框架 | Vue 3.5 + TypeScript + Element Plus |
| 状态管理 | Pinia (permissionStore, dictStore) |
| 路由 | Vue Router 4 (动态路由) |
| 构建工具 | Vite |

### 1.2 现有代码分析

**前端已实现** (`magic-ruoyi-web/src/views/system/menu/index.vue`):
- 树形表格展示，支持懒加载子菜单 (`lazy` + `load` 属性)
- 级联删除对话框 (el-tree 多选)
- 菜单表单对话框，支持目录/菜单/按钮三种类型切换
- 图标选择器 (`icon-select` 组件)
- 上级菜单树形选择器 (`el-tree-select`)
- 展开/折叠状态管理 (`menuExpandMap`, `menuChildrenListMap`)

**前端 API 已定义** (`magic-ruoyi-web/src/api/system/menu/`):
- `listMenu`, `getMenu`, `addMenu`, `updateMenu`, `delMenu`, `cascadeDelMenu`
- `treeselect` (菜单下拉树)
- `roleMenuTreeselect`, `tenantPackageMenuTreeselect` (角色/租户套餐关联)

**动态路由机制** (`magic-ruoyi-web/src/store/modules/permission.ts`):
- `getRouters()` 调用 `/system/menu/getRouters` 接口
- `filterAsyncRouter()` 将后端返回的路由字符串转换为 Vue Router 组件对象
- 特殊组件映射: `Layout` -> `@/layout/index.vue`, `ParentView` -> `@/components/ParentView`, `InnerLink` -> 内链组件
- `loadView()` 通过 `import.meta.glob` 动态加载 views 目录下的 .vue 文件

### 1.3 待实现后端

后端 Controller/Service/Mapper 层尚未创建，需要从零实现。参考项目已有的 `AuthController` 风格:
- 使用 `@RequiredArgsConstructor` 构造器注入
- 统一返回 `R<T>` 响应体
- 使用 `@SaCheckPermission` 进行权限校验

---

## 2. Constitution Compliance

### 2.1 架构约束

- 遵循 RuoYi-Vue-Plus 分层架构: Controller -> Service -> Mapper
- 使用 MyBatis-Plus 的 `BaseMapper<T>` 和 `IService<T>` / `ServiceImpl` 模式
- BO (Business Object) / VO (View Object) 分离，通过 MapStruct 转换
- 逻辑删除使用 `del_flag` 字段 (菜单表暂未定义 del_flag，物理删除)
- 多租户: 菜单数据为全局共享，不按租户隔离

### 2.2 安全约束

- 所有写操作需要 `@SaCheckPermission` 权限校验
- 级联删除需要事务保护 (`@Transactional`)
- 删除前校验: 有子菜单时拒绝单条删除

### 2.3 编码规范

- 权限标识格式: `system:menu:{operation}` (list/query/add/edit/remove)
- RESTful API 设计: GET 查询, POST 新增, PUT 修改, DELETE 删除
- 树形数据通过 `parentId` 自引用关联

---

## 3. Research Findings

### 3.1 数据库表结构 (sys_menu)

| 字段 | 类型 | 说明 |
|------|------|------|
| menu_id | bigint(20) | 主键 |
| menu_name | varchar(50) | 菜单名称 |
| parent_id | bigint(20) | 父菜单ID，默认0 |
| order_num | int(4) | 显示顺序 |
| path | varchar(200) | 路由地址 |
| component | varchar(255) | 组件路径 |
| query_param | varchar(255) | 路由参数 |
| is_frame | int(1) | 是否外链 (0=是, 1=否) |
| is_cache | int(1) | 是否缓存 (0=缓存, 1=不缓存) |
| menu_type | char(1) | 菜单类型 (M=目录, C=菜单, F=按钮) |
| visible | char(1) | 显示状态 (0=显示, 1=隐藏) |
| status | char(1) | 菜单状态 (0=正常, 1=停用) |
| perms | varchar(100) | 权限标识 |
| icon | varchar(100) | 菜单图标 |
| create_dept | bigint(20) | 创建部门 |
| create_by | bigint(20) | 创建者 |
| create_time | datetime | 创建时间 |
| update_by | bigint(20) | 更新者 |
| update_time | datetime | 更新时间 |
| remark | varchar(500) | 备注 |

### 3.2 关联表 (sys_role_menu)

| 字段 | 类型 | 说明 |
|------|------|------|
| role_id | bigint(20) | 角色ID |
| menu_id | bigint(20) | 菜单ID |

### 3.3 前端路由生成规则

从 `permission.ts` 的 `filterAsyncRouter` 分析:
- 后端返回的 `component` 字段为字符串路径 (如 `system/user/index`)
- `component === 'Layout'` 时映射为 Layout 组件
- `component === 'ParentView'` 时映射为 ParentView 组件
- `component === 'InnerLink'` 时映射为 InnerLink 组件
- 其他情况通过 `loadView()` 在 `views/` 目录下查找对应 .vue 文件
- 外链菜单 (`is_frame = '0'`) 使用完整 URL 作为 path
- 隐藏菜单 (`visible = '1'`) 不在侧边栏显示，但路由有效
- 停用菜单 (`status = '1'`) 不参与路由生成

### 3.4 前端懒加载机制

菜单表格使用 `lazy` + `load` 属性实现懒加载:
- `getChildrenList(row, treeNode, resolve)` 按需加载子菜单
- `menuExpandMap` 记录已展开的节点及其 resolve 函数
- `refreshLoadTree()` 在增删改后刷新已展开的父节点
- `menuChildrenListMap` 缓存父子关系数据

---

## 4. Data Model

### 4.1 后端实体设计

**SysMenu (Entity)**
```
menuId: Long
menuName: String
parentId: Long
orderNum: Integer
path: String
component: String
queryParam: String
isFrame: String
isCache: String
menuType: String
visible: String
status: String
perms: String
icon: String
createDept: Long
createBy: Long
createTime: Date
updateBy: Long
updateTime: Date
remark: String
```

**SysMenuBo (Business Object)**
```
menuId: Long (修改时必填)
menuName: String (必填)
parentId: Long (默认0)
orderNum: Integer (必填)
path: String
component: String
queryParam: String
isFrame: String
isCache: String
menuType: String (必填: M/C/F)
visible: String
status: String
perms: String
icon: String
remark: String
```

**SysMenuVo (View Object)**
```
menuId: Long
menuName: String
parentId: Long
parentName: String (关联查询)
orderNum: Integer
path: String
component: String
queryParam: String
isFrame: String
isCache: String
menuType: String
visible: String
status: String
perms: String
icon: String
createTime: Date
children: List<SysMenuVo> (树形结构)
```

**RouterVo (路由返回对象)**
```
name: String
path: String
hidden: Boolean
redirect: String
component: String
meta: MetaVo {
  title: String
  icon: String
  noCache: Boolean
  link: String
}
children: List<RouterVo>
```

### 4.2 树形构建算法

使用递归方式构建树形结构:
1. 查询所有菜单 (按 order_num 排序)
2. 过滤出 parentId = 0 的根节点
3. 对每个根节点递归查找其子节点
4. 子节点查找条件: parentId = 当前节点 menuId

---

## 5. Interface Contracts

### 5.1 RESTful API 端点

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/menu/list` | system:menu:list | 查询菜单列表 (树形) |
| GET | `/system/menu/{menuId}` | system:menu:query | 查询菜单详情 |
| GET | `/system/menu/treeselect` | - | 查询菜单下拉树结构 |
| GET | `/system/menu/roleMenuTreeselect/{roleId}` | - | 查询角色菜单树 (含已选) |
| GET | `/system/menu/tenantPackageMenuTreeselect/{packageId}` | - | 查询租户套餐菜单树 |
| GET | `/system/menu/getRouters` | - | 获取当前用户路由 (登录后) |
| POST | `/system/menu` | system:menu:add | 新增菜单 |
| PUT | `/system/menu` | system:menu:edit | 修改菜单 |
| DELETE | `/system/menu/{menuId}` | system:menu:remove | 删除单个菜单 |
| DELETE | `/system/menu/cascade/{menuIds}` | system:menu:remove | 级联删除菜单 |

### 5.2 请求/响应示例

**GET /system/menu/list**
- Query: `menuName?`, `status?`
- Response: `R<List<SysMenuVo>>` (树形结构)

**GET /system/menu/getRouters**
- Response: `R<List<RouterVo>>` (当前用户可见路由)

**DELETE /system/menu/cascade/{menuIds}**
- Path: `menuIds` (逗号分隔的菜单ID列表)
- Response: `R<Void>`

### 5.3 前端 API 契约

前端已定义完整 API 接口 (`@/api/system/menu/index.ts`)，后端需匹配:
- URL 路径一致
- HTTP 方法一致
- 请求参数名称与 `MenuForm` / `MenuQuery` 类型匹配
- 响应数据结构与 `MenuVO` 类型匹配

---

## 6. Implementation Strategy

### 6.1 后端实现步骤

**Phase 1: 基础 CRUD**
1. 创建 `SysMenu` Entity (继承 BaseEntity)
2. 创建 `SysMenuMapper` 继承 `BaseMapper<SysMenu>`
3. 创建 `ISysMenuService` 接口，定义 CRUD 方法
4. 创建 `SysMenuServiceImpl` 实现业务逻辑
5. 创建 `SysMenuController` 暴露 REST API

**Phase 2: 树形结构**
1. 实现 `buildMenuTree()` 递归构建树形结构
2. 实现 `selectMenuTreeByUserId()` 根据用户角色查询可见菜单
3. 实现 `treeselect()` 返回下拉树结构

**Phase 3: 动态路由**
1. 实现 `getRouters()` 方法:
   - 获取当前用户角色
   - 查询角色关联的菜单
   - 过滤 status='0' 的菜单
   - 过滤 menuType in ('M', 'C') 的菜单 (排除按钮)
   - 转换为 `RouterVo` 列表
   - 递归构建子路由
2. 路由转换规则:
   - 外链 (`is_frame='0'`): path 使用完整 URL, component='InnerLink'
   - 目录 (`menu_type='M'`): component='Layout' 或 'ParentView'
   - 菜单 (`menu_type='C'`): component 使用组件路径
   - hidden: `visible='1'` 时为 true

**Phase 4: 级联删除**
1. 实现 `cascadeDeleteMenu()` 方法:
   - 递归查找所有子菜单ID
   - 批量删除 sys_menu 记录
   - 批量删除 sys_role_menu 关联记录
   - 使用 `@Transactional` 保证事务一致性

**Phase 5: 角色/租户套餐关联**
1. 实现 `roleMenuTreeselect()`: 返回菜单树 + 角色已选菜单ID列表
2. 实现 `tenantPackageMenuTreeselect()`: 返回租户套餐可用菜单树

### 6.2 关键业务逻辑

**删除校验**:
```
单条删除:
  1. 检查是否存在子菜单 (selectCount by parentId)
  2. 存在子菜单 -> 拒绝删除
  3. 不存在 -> 执行删除
  4. 清理 sys_role_menu 关联

级联删除:
  1. 收集所有选中菜单ID
  2. 递归查找所有子菜单ID
  3. 合并去重
  4. 批量删除 sys_menu
  5. 批量删除 sys_role_menu
```

**路由生成**:
```
getRouters():
  1. 获取当前用户 userId
  2. 查询用户角色列表
  3. 查询角色关联的菜单ID列表 (sys_role_menu)
  4. 查询菜单详情 (menuId in 列表, status='0')
  5. 过滤 menuType in ('M', 'C')
  6. 按 orderNum 排序
  7. 构建树形结构
  8. 转换为 RouterVo
```

### 6.3 前端已有实现 (无需修改)

- 树形表格懒加载机制已完整实现
- 级联删除对话框已实现
- 菜单表单已实现 (含类型切换、字段显隐)
- 动态路由加载机制已完整实现 (permissionStore)
- DictTag 组件已实现 (用于状态显示)

---

## 7. Testing Considerations

### 7.1 单元测试

- 树形构建算法测试: 验证多层级菜单正确构建树形结构
- 路由生成测试: 验证不同菜单类型正确转换为 RouterVo
- 删除校验测试: 验证有子菜单时拒绝删除
- 级联删除测试: 验证子菜单和角色关联记录正确清理

### 7.2 集成测试

- 菜单 CRUD 完整流程测试
- 动态路由加载测试: 登录后调用 getRouters 验证路由数据
- 角色菜单关联测试: 分配菜单后验证用户可见菜单
- 级联删除测试: 删除父菜单后验证子菜单和关联记录清理

### 7.3 前端测试

- 树形表格展开/折叠功能
- 懒加载子菜单功能
- 级联删除选择功能
- 菜单表单类型切换 (目录/菜单/按钮)
- 动态路由加载后侧边栏渲染

### 7.4 性能测试

- 100+ 菜单节点树形加载性能 (目标: < 1s)
- 懒加载子菜单响应时间 (目标: < 300ms P95)
- getRouters 接口响应时间 (目标: < 500ms P95)

---

## 8. File Inventory

### 8.1 后端文件 (待创建)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-admin/.../domain/SysMenu.java` | 菜单实体 |
| `magic-ruoyi-admin/.../domain/bo/SysMenuBo.java` | 菜单业务对象 |
| `magic-ruoyi-admin/.../domain/vo/SysMenuVo.java` | 菜单视图对象 |
| `magic-ruoyi-admin/.../domain/vo/RouterVo.java` | 路由视图对象 |
| `magic-ruoyi-admin/.../domain/vo/MetaVo.java` | 路由元信息对象 |
| `magic-ruoyi-admin/.../mapper/SysMenuMapper.java` | 菜单 Mapper |
| `magic-ruoyi-admin/.../service/ISysMenuService.java` | 菜单 Service 接口 |
| `magic-ruoyi-admin/.../service/impl/SysMenuServiceImpl.java` | 菜单 Service 实现 |
| `magic-ruoyi-admin/.../controller/system/SysMenuController.java` | 菜单 Controller |
| `magic-ruoyi-admin/.../resources/mapper/system/SysMenuMapper.xml` | MyBatis XML (如需自定义 SQL) |

### 8.2 前端文件 (已存在)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-web/src/views/system/menu/index.vue` | 菜单管理页面 |
| `magic-ruoyi-web/src/api/system/menu/index.ts` | 菜单 API 接口 |
| `magic-ruoyi-web/src/api/system/menu/types.ts` | 菜单类型定义 |
| `magic-ruoyi-web/src/store/modules/permission.ts` | 权限/路由 Store |
| `magic-ruoyi-web/src/api/menu.ts` | 路由 API (getRouters) |
| `magic-ruoyi-web/src/components/IconSelect/index.vue` | 图标选择器 |
| `magic-ruoyi-web/src/components/DictTag/index.vue` | 字典标签组件 |

### 8.3 数据库文件

| 文件路径 | 说明 |
|----------|------|
| `sql/magic-ruoyi.sql` | 包含 sys_menu 表定义和初始数据 |

### 8.4 关联表 (已存在)

| 表名 | 说明 |
|------|------|
| sys_role_menu | 角色菜单关联表 |
