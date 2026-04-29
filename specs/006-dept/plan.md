# 部门管理模块技术实现方案 (plan.md)

> magic-ruoyi 部门管理模块。负责部门的增删改查、树形组织机构管理和数据权限关联。
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

### 1.2 现有代码分析

**前端已实现** (`magic-ruoyi-web/src/views/system/dept/index.vue`):
- 树形表格展示，支持展开/折叠 (`default-expand-all` + `toggleRowExpansion`)
- 一键展开/折叠所有节点 (`handleToggleExpandAll`)
- 部门表单对话框，含上级部门树形选择器 (`el-tree-select`)
- 负责人选择 (从当前部门用户列表中选择)
- 修改部门时排除当前部门及其子部门 (`listDeptExcludeChild`)

**前端 API 已定义** (`magic-ruoyi-web/src/api/system/dept/`):
- `listDept`, `getDept`, `addDept`, `updateDept`, `delDept`
- `listDeptExcludeChild` (排除指定节点的部门列表)
- `optionSelect` (根据 deptIds 查询部门)

**树形处理**: 前端使用 `proxy?.handleTree()` 工具函数将扁平列表转换为树形结构

### 1.3 待实现后端

后端 Controller/Service/Mapper 层尚未创建，需要从零实现。

---

## 2. Constitution Compliance

### 2.1 架构约束

- 遵循 RuoYi-Vue-Plus 分层架构: Controller -> Service -> Mapper
- 使用 MyBatis-Plus 的 `BaseMapper<T>` 和 `IService<T>` / `ServiceImpl` 模式
- BO / VO 分离，通过 MapStruct 转换
- 逻辑删除使用 `del_flag` 字段 ('0'=正常, '1'=删除)
- 多租户: 部门数据按 `tenant_id` 隔离

### 2.2 安全约束

- 所有写操作需要 `@SaCheckPermission` 权限校验
- 删除操作需要事务保护 (`@Transactional`)
- 删除前校验: 存在子部门或有关联用户时拒绝删除

### 2.3 编码规范

- 权限标识格式: `system:dept:{operation}` (list/query/add/edit/remove)
- RESTful API 设计
- 树形数据通过 `parent_id` 自引用 + `ancestors` 祖级列表维护

---

## 3. Research Findings

### 3.1 数据库表结构 (sys_dept)

| 字段 | 类型 | 说明 |
|------|------|------|
| dept_id | bigint(20) | 主键 |
| tenant_id | varchar(20) | 租户编号，默认 '000000' |
| parent_id | bigint(20) | 父部门ID，默认0 |
| ancestors | varchar(500) | 祖级列表，如 "0,100,102" |
| dept_name | varchar(30) | 部门名称 |
| dept_category | varchar(100) | 部门类别编码 |
| order_num | int(4) | 显示顺序 |
| leader | bigint(20) | 负责人 (用户ID) |
| phone | varchar(11) | 联系电话 |
| email | varchar(50) | 邮箱 |
| status | char(1) | 状态 (0=正常, 1=停用) |
| del_flag | char(1) | 删除标志 (0=存在, 1=删除) |
| create_dept | bigint(20) | 创建部门 |
| create_by | bigint(20) | 创建者 |
| create_time | datetime | 创建时间 |
| update_by | bigint(20) | 更新者 |
| update_time | datetime | 更新时间 |

### 3.2 关联关系

- SysUser N-1 SysDept: 用户通过 `dept_id` 关联所属部门
- SysRole N-M SysDept: 通过 `sys_role_dept` 关联表 (角色数据权限范围)
- SysPost N-1 SysDept: 岗位通过 `dept_id` 关联所属部门

### 3.3 树形结构维护

- `parent_id` 自引用形成树形结构
- `ancestors` 字段存储从根到当前部门的完整路径 (逗号分隔)
- 修改部门的上级部门时，需递归更新该部门及所有子孙部门的 `ancestors` 字段
- 根部门的 `parent_id = 0`, `ancestors = "0"`

### 3.4 前端树形处理

- 前端使用 `handleTree(data, 'deptId')` 将扁平列表转为树形结构
- 树形表格使用 `tree-props="{ children: 'children', hasChildren: 'hasChildren' }"`
- 展开/折叠通过 `toggleRowExpansion()` 控制

---

## 4. Data Model

### 4.1 后端实体设计

**SysDept (Entity)**
```
deptId: Long
tenantId: String
parentId: Long
ancestors: String
deptName: String
deptCategory: String
orderNum: Integer
leader: Long
phone: String
email: String
status: String
delFlag: String
createDept: Long
createBy: Long
createTime: Date
updateBy: Long
updateTime: Date
```

**SysDeptBo (Business Object)**
```
deptId: Long (修改时必填)
deptName: String (必填)
parentId: Long (必填)
deptCategory: String
orderNum: Integer (必填)
leader: Long
phone: String
email: String
status: String
```

**SysDeptVo (View Object)**
```
deptId: Long
parentName: String (关联查询)
parentId: Long
ancestors: String
deptName: String
deptCategory: String
orderNum: Integer
leader: Long
leaderName: String (关联查询)
phone: String
email: String
status: String
createTime: Date
children: List<SysDeptVo> (树形结构)
```

**DeptTreeVo (树形选择器专用)**
```
id: Long
label: String (部门名称)
parentId: Long
weight: Integer (orderNum)
children: List<DeptTreeVo>
disabled: Boolean (有子部门或有关联用户时禁用删除)
```

### 4.2 树形构建算法

使用递归方式构建树形结构:
1. 查询所有部门 (按 order_num 排序, del_flag='0')
2. 过滤出 parentId = 0 的根节点
3. 对每个根节点递归查找其子节点

---

## 5. Interface Contracts

### 5.1 RESTful API 端点

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/dept/list` | system:dept:list | 查询部门列表 (树形) |
| GET | `/system/dept/{deptId}` | system:dept:query | 查询部门详情 |
| GET | `/system/dept/list/exclude/{deptId}` | - | 查询部门列表 (排除指定节点) |
| GET | `/system/dept/optionselect` | - | 根据 deptIds 查询部门 |
| POST | `/system/dept` | system:dept:add | 新增部门 |
| PUT | `/system/dept` | system:dept:edit | 修改部门 |
| DELETE | `/system/dept/{deptId}` | system:dept:remove | 删除部门 |

### 5.2 请求/响应示例

**GET /system/dept/list**
- Query: `deptName?`, `deptCategory?`, `status?`
- Response: `R<List<SysDeptVo>>` (树形结构)

**GET /system/dept/list/exclude/{deptId}**
- Path: `deptId` (要排除的部门ID及其子部门)
- Response: `R<List<SysDeptVo>>` (排除指定节点后的树形列表)

**POST /system/dept**
- Body: `SysDeptBo`
- Response: `R<Void>`

### 5.3 前端 API 契约

前端已定义完整 API 接口 (`@/api/system/dept/index.ts`)，后端需匹配:
- URL 路径一致
- HTTP 方法一致
- 请求参数名称与 `DeptForm` / `DeptQuery` 类型匹配
- 响应数据结构与 `DeptVO` / `DeptTreeVO` 类型匹配

---

## 6. Implementation Strategy

### 6.1 后端实现步骤

**Phase 1: 基础 CRUD**
1. 创建 `SysDept` Entity (继承 BaseEntity)
2. 创建 `SysDeptMapper` 继承 `BaseMapper<SysDept>`
3. 创建 `ISysDeptService` 接口
4. 创建 `SysDeptServiceImpl` 实现业务逻辑
5. 创建 `SysDeptController` 暴露 REST API

**Phase 2: 树形结构**
1. 实现 `buildDeptTree()` 递归构建树形结构
2. 实现 `selectDeptList()` 查询部门列表 (支持条件过滤)
3. 实现 `selectDeptListExcludeChild()` 排除指定节点及其子部门

**Phase 3: ancestors 维护**
1. 新增部门时: `ancestors = parent.ancestors + "," + parent.deptId`
2. 修改部门上级时:
   - 计算新的 ancestors 路径
   - 递归更新该部门及所有子孙部门的 ancestors
   - 使用 `updateBatchById()` 批量更新

**Phase 4: 删除校验**
1. 检查是否存在子部门 (selectCount by parentId, del_flag='0')
2. 检查是否存在关联用户 (selectCount from sys_user where dept_id = ?)
3. 任一条件满足则拒绝删除
4. 执行逻辑删除 (update del_flag = '1')

**Phase 5: 负责人管理**
1. 负责人字段存储用户ID (leader = user_id)
2. 前端通过 `listUserByDeptId()` 获取当前部门所有用户
3. 负责人可为空

### 6.2 关键业务逻辑

**新增部门**:
```
1. 校验部门名称在同一父部门下不重复
2. 设置 parent_id, ancestors
3. ancestors = (parent.ancestors ?: "0") + "," + parent.deptId
4. 设置 del_flag = '0'
5. 插入记录
```

**修改部门**:
```
1. 校验新上级不是当前部门或其子部门 (防止循环引用)
2. 如果 parent_id 变更:
   a. 计算新的 ancestors
   b. 更新当前部门 ancestors
   c. 查询所有子孙部门 (ancestors like '%,oldDeptId,%')
   d. 批量更新子孙部门 ancestors (替换旧路径为新路径)
3. 更新其他字段
```

**删除部门**:
```
1. 检查是否存在子部门 (del_flag='0')
2. 检查是否存在关联用户
3. 执行逻辑删除 (del_flag = '1')
```

### 6.3 前端已有实现 (无需修改)

- 树形表格展示已实现
- 展开/折叠功能已实现
- 部门表单已实现 (含上级部门选择、负责人选择)
- 修改时排除当前部门及子部门已实现
- handleTree 工具函数已实现

---

## 7. Testing Considerations

### 7.1 单元测试

- 树形构建算法测试: 验证多层级部门正确构建树形结构
- ancestors 更新测试: 验证修改上级部门后 ancestors 正确更新
- 删除校验测试: 验证有子部门或关联用户时拒绝删除
- 循环引用检测测试: 验证不能将部门设为自身或子部门的子部门

### 7.2 集成测试

- 部门 CRUD 完整流程测试
- 树形结构维护测试: 新增/修改/删除后树形结构正确
- ancestors 更新测试: 修改上级后子孙部门 ancestors 同步更新
- 多租户隔离测试: 不同租户部门数据隔离

### 7.3 前端测试

- 树形表格展开/折叠功能
- 一键展开/折叠所有节点
- 部门表单上级部门选择 (排除当前部门及子部门)
- 负责人选择功能
- 删除前校验提示

### 7.4 性能测试

- 部门树加载响应时间 (目标: < 500ms P95)
- ancestors 批量更新性能 (目标: 500节点 < 1s)
- 树形表格渲染性能 (目标: 500节点流畅)

---

## 8. File Inventory

### 8.1 后端文件 (待创建)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-admin/.../domain/SysDept.java` | 部门实体 |
| `magic-ruoyi-admin/.../domain/bo/SysDeptBo.java` | 部门业务对象 |
| `magic-ruoyi-admin/.../domain/vo/SysDeptVo.java` | 部门视图对象 |
| `magic-ruoyi-admin/.../domain/vo/DeptTreeVo.java` | 部门树形选择器对象 |
| `magic-ruoyi-admin/.../mapper/SysDeptMapper.java` | 部门 Mapper |
| `magic-ruoyi-admin/.../service/ISysDeptService.java` | 部门 Service 接口 |
| `magic-ruoyi-admin/.../service/impl/SysDeptServiceImpl.java` | 部门 Service 实现 |
| `magic-ruoyi-admin/.../controller/system/SysDeptController.java` | 部门 Controller |
| `magic-ruoyi-admin/.../resources/mapper/system/SysDeptMapper.xml` | MyBatis XML (如需自定义 SQL) |

### 8.2 前端文件 (已存在)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-web/src/views/system/dept/index.vue` | 部门管理页面 |
| `magic-ruoyi-web/src/api/system/dept/index.ts` | 部门 API 接口 |
| `magic-ruoyi-web/src/api/system/dept/types.ts` | 部门类型定义 |
| `magic-ruoyi-web/src/api/system/user/index.ts` | 用户 API (listUserByDeptId) |

### 8.3 数据库文件

| 文件路径 | 说明 |
|----------|------|
| `sql/magic-ruoyi.sql` | 包含 sys_dept 表定义和初始数据 |

### 8.4 关联表 (已存在)

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 (通过 dept_id 关联) |
| sys_role_dept | 角色部门关联表 (数据权限) |
| sys_post | 岗位表 (通过 dept_id 关联) |
