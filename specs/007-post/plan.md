# 岗位管理模块技术实现方案 (plan.md)

> magic-ruoyi 岗位管理模块。负责岗位信息的增删改查、部门关联和岗位选择功能。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. Technical Context

### 1.1 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot 3.x + MyBatis-Plus |
| 权限框架 | Sa-Token |
| 前端框架 | Vue 3.5 + TypeScript + Element Plus |
| 状态管理 | Pinia |
| 数据校验 | Element Plus Form Validation |
| 权限控制 | v-hasPermi 指令 |
| Excel 导出 | EasyExcel |

### 1.2 现有代码分析

**前端已实现** (`magic-ruoyi-web/src/views/system/post/index.vue`):
- 左侧部门树 + 右侧岗位列表的双栏布局
- 部门树支持搜索过滤 (`filter-node-method`)
- 点击部门树节点按该部门及其子部门筛选岗位
- 顶部部门下拉选择器 (`el-tree-select`)
- 岗位列表分页展示
- 岗位新增/修改对话框，含部门树形选择器
- 批量删除功能 (checkbox 多选)
- Excel 导出功能

**前端 API 已定义** (`magic-ruoyi-web/src/api/system/post/`):
- `listPost`, `getPost`, `addPost`, `updatePost`, `delPost`
- `optionselect` (岗位下拉选择，支持按 deptId 和 postIds 过滤)
- `deptTreeSelect` (查询部门下拉树结构)

**布局特点**: 使用 `el-row` + `el-col` 实现响应式双栏布局 (`lg="4/20"`, `xs="24"`)

### 1.3 待实现后端

后端 Controller/Service/Mapper 层尚未创建，需要从零实现。

---

## 2. Constitution Compliance

### 2.1 架构约束

- 遵循 RuoYi-Vue-Plus 分层架构: Controller -> Service -> Mapper
- 使用 MyBatis-Plus 的 `BaseMapper<T>` 和 `IService<T>` / `ServiceImpl` 模式
- BO / VO 分离，通过 MapStruct 转换
- 多租户: 岗位数据按 `tenant_id` 隔离
- 岗位归属于特定部门 (N-1 关系)

### 2.2 安全约束

- 所有写操作需要 `@SaCheckPermission` 权限校验
- 删除操作支持批量删除
- 导出功能需要权限校验 (`system:post:export`)

### 2.3 编码规范

- 权限标识格式: `system:post:{operation}` (list/query/add/edit/remove/export)
- RESTful API 设计
- 分页查询使用 `PageQuery` 参数

---

## 3. Research Findings

### 3.1 数据库表结构 (sys_post)

| 字段 | 类型 | 说明 |
|------|------|------|
| post_id | bigint(20) | 主键 |
| tenant_id | varchar(20) | 租户编号，默认 '000000' |
| dept_id | bigint(20) | 部门ID (关联 sys_dept) |
| post_code | varchar(64) | 岗位编码 |
| post_category | varchar(100) | 岗位类别编码 |
| post_name | varchar(50) | 岗位名称 |
| post_sort | int(4) | 显示顺序 |
| status | char(1) | 状态 (0=正常, 1=停用) |
| create_dept | bigint(20) | 创建部门 |
| create_by | bigint(20) | 创建者 |
| create_time | datetime | 创建时间 |
| update_by | bigint(20) | 更新者 |
| update_time | datetime | 更新时间 |
| remark | varchar(500) | 备注 |

### 3.2 关联关系

- SysPost N-1 SysDept: 每个岗位归属于一个部门
- SysUser N-M SysPost: 通过 `sys_user_post` 关联表

### 3.3 前端筛选机制

- 左侧部门树点击: 设置 `belongDeptId` 参数，按该部门及其子部门筛选
- 顶部部门下拉: 设置 `deptId` 参数，按精确部门筛选
- 两者互斥: 使用部门下拉时清空 `belongDeptId`

### 3.4 字典使用

- 岗位状态使用字典 `sys_normal_disable` (0=正常, 1=停用)
- 前端通过 `useDict('sys_normal_disable')` 加载字典数据
- 列表中使用 `<dict-tag>` 组件展示状态

---

## 4. Data Model

### 4.1 后端实体设计

**SysPost (Entity)**
```
postId: Long
tenantId: String
deptId: Long
postCode: String
postCategory: String
postName: String
postSort: Integer
status: String
createDept: Long
createBy: Long
createTime: Date
updateBy: Long
updateTime: Date
remark: String
```

**SysPostBo (Business Object)**
```
postId: Long (修改时必填)
deptId: Long (必填)
postCode: String (必填)
postCategory: String
postName: String (必填)
postSort: Integer (必填)
status: String
remark: String
```

**SysPostVo (View Object)**
```
postId: Long
deptId: Long
postCode: String
postCategory: String
postName: String
deptName: String (关联查询)
postSort: Integer
status: String
remark: String
createTime: Date
```

### 4.2 查询参数

**SysPostBo (查询用)**
```
deptId: Long (精确部门筛选)
belongDeptId: Long (部门及其子部门筛选)
postCode: String (模糊匹配)
postName: String (模糊匹配)
postCategory: String (模糊匹配)
status: String (精确匹配)
```

---

## 5. Interface Contracts

### 5.1 RESTful API 端点

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/post/list` | system:post:list | 查询岗位列表 (分页) |
| GET | `/system/post/{postId}` | system:post:query | 查询岗位详情 |
| GET | `/system/post/optionselect` | - | 岗位下拉选择 |
| GET | `/system/post/deptTree` | - | 查询部门下拉树结构 |
| POST | `/system/post` | system:post:add | 新增岗位 |
| PUT | `/system/post` | system:post:edit | 修改岗位 |
| DELETE | `/system/post/{postIds}` | system:post:remove | 删除岗位 (支持批量) |
| POST | `/system/post/export` | system:post:export | 导出岗位 Excel |

### 5.2 请求/响应示例

**GET /system/post/list**
- Query: `pageNum`, `pageSize`, `deptId?`, `belongDeptId?`, `postCode?`, `postName?`, `postCategory?`, `status?`
- Response: `R<TableDataInfo<SysPostVo>>` (分页数据)

**GET /system/post/optionselect**
- Query: `deptId?`, `postIds?`
- Response: `R<List<SysPostVo>>`

**GET /system/post/deptTree**
- Response: `R<List<DeptTreeVo>>`

**DELETE /system/post/{postIds}**
- Path: `postIds` (逗号分隔的岗位ID列表)
- Response: `R<Void>`

### 5.3 前端 API 契约

前端已定义完整 API 接口 (`@/api/system/post/index.ts`)，后端需匹配:
- URL 路径一致
- HTTP 方法一致
- 请求参数名称与 `PostForm` / `PostQuery` 类型匹配
- 响应数据结构与 `PostVO` 类型匹配

---

## 6. Implementation Strategy

### 6.1 后端实现步骤

**Phase 1: 基础 CRUD**
1. 创建 `SysPost` Entity (继承 BaseEntity)
2. 创建 `SysPostMapper` 继承 `BaseMapper<SysPost>`
3. 创建 `ISysPostService` 接口
4. 创建 `SysPostServiceImpl` 实现业务逻辑
5. 创建 `SysPostController` 暴露 REST API

**Phase 2: 查询功能**
1. 实现 `selectPostList()` 分页查询:
   - 支持按 deptId 精确筛选
   - 支持按 belongDeptId + 子部门筛选 (需查询部门树)
   - 支持按 postCode/postName/postCategory 模糊匹配
   - 支持按 status 精确匹配
   - 关联查询 deptName
2. 实现 `selectPostById()` 查询详情 (含 deptName)
3. 实现 `selectPostOptionselect()` 下拉选择

**Phase 3: 部门树接口**
1. 实现 `selectDeptTree()`:
   - 查询所有部门 (del_flag='0')
   - 构建树形结构
   - 转换为 `DeptTreeVo` 格式

**Phase 4: 导出功能**
1. 使用 EasyExcel 实现 Excel 导出
2. 按当前查询条件导出数据
3. 文件名格式: `post_{timestamp}.xlsx`

### 6.2 关键业务逻辑

**belongDeptId 筛选逻辑**:
```
1. 根据 belongDeptId 查询该部门
2. 获取该部门的 ancestors 路径
3. 查询所有 ancestors 包含该部门ID的部门 (即子部门)
4. 收集所有部门ID列表
5. 使用 IN 条件查询岗位
```

**岗位编码唯一性**:
- 同一租户下岗位编码应唯一 (可选校验)

### 6.3 前端已有实现 (无需修改)

- 双栏布局 (部门树 + 岗位列表) 已实现
- 部门树搜索过滤已实现
- 岗位列表分页已实现
- 岗位表单已实现 (含部门树形选择器)
- 批量删除已实现
- Excel 导出已实现
- DictTag 状态展示已实现

---

## 7. Testing Considerations

### 7.1 单元测试

- 分页查询测试: 验证各种筛选条件正确生效
- belongDeptId 筛选测试: 验证按部门及其子部门筛选正确
- 岗位编码唯一性测试 (如实现)
- 批量删除测试: 验证多个岗位ID正确删除

### 7.2 集成测试

- 岗位 CRUD 完整流程测试
- 部门树筛选测试: 点击部门树节点正确筛选岗位
- 岗位下拉选择测试: 按 deptId 和 postIds 过滤正确
- Excel 导出测试: 导出数据与查询条件一致

### 7.3 前端测试

- 左侧部门树点击筛选功能
- 部门树搜索过滤功能
- 顶部部门下拉选择功能
- 岗位列表分页功能
- 批量删除功能
- 岗位表单部门选择功能
- Excel 导出功能

### 7.4 性能测试

- 岗位列表分页查询响应时间 (目标: < 500ms P95)
- 部门树加载响应时间 (目标: < 300ms P95)
- Excel 导出性能 (目标: 1000条 < 3s)

---

## 8. File Inventory

### 8.1 后端文件 (待创建)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-admin/.../domain/SysPost.java` | 岗位实体 |
| `magic-ruoyi-admin/.../domain/bo/SysPostBo.java` | 岗位业务对象 |
| `magic-ruoyi-admin/.../domain/vo/SysPostVo.java` | 岗位视图对象 |
| `magic-ruoyi-admin/.../mapper/SysPostMapper.java` | 岗位 Mapper |
| `magic-ruoyi-admin/.../service/ISysPostService.java` | 岗位 Service 接口 |
| `magic-ruoyi-admin/.../service/impl/SysPostServiceImpl.java` | 岗位 Service 实现 |
| `magic-ruoyi-admin/.../controller/system/SysPostController.java` | 岗位 Controller |
| `magic-ruoyi-admin/.../resources/mapper/system/SysPostMapper.xml` | MyBatis XML (如需自定义 SQL) |

### 8.2 前端文件 (已存在)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-web/src/views/system/post/index.vue` | 岗位管理页面 |
| `magic-ruoyi-web/src/api/system/post/index.ts` | 岗位 API 接口 |
| `magic-ruoyi-web/src/api/system/post/types.ts` | 岗位类型定义 |
| `magic-ruoyi-web/src/api/system/dept/types.ts` | 部门类型定义 (DeptTreeVO) |

### 8.3 数据库文件

| 文件路径 | 说明 |
|----------|------|
| `sql/magic-ruoyi.sql` | 包含 sys_post 表定义和初始数据 |

### 8.4 关联表 (已存在)

| 表名 | 说明 |
|------|------|
| sys_dept | 部门表 (通过 dept_id 关联) |
| sys_user_post | 用户岗位关联表 |
