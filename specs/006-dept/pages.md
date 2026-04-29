# 部门管理前端页面文档 (pages.md)

> magic-ruoyi 部门管理模块的前端页面定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 页面清单

| 页面文件 | 路由路径 | 页面名称 | 功能说明 |
|----------|----------|----------|----------|
| `views/system/dept/index.vue` | `/system/dept` | 部门管理 | 部门树形列表、CRUD、上级部门选择 |

---

## 2. 部门管理页面 (index.vue)

### 2.1 页面布局

```
+----------------------------------------------------------+
| 搜索区域（可折叠）                                          |
| [部门名称] [类别编码] [状态] [搜索] [重置]                  |
+----------------------------------------------------------+
| 工具栏                                                    |
| [新增] [展开/折叠]                      [显示/隐藏搜索]    |
+----------------------------------------------------------+
| 树形数据表格                                               |
| 部门名称 | 类别编码 | 排序 | 状态 | 创建时间 | 操作         |
| ▶ 根机构 | - | 0 | 正常 | 2026-xx-xx | 修改 新增 删除     |
|   ▶ 研发部 | R&D | 1 | 正常 | 2026-xx-xx | 修改 新增 删除 |
|     前端组 | FE | 1 | 正常 | 2026-xx-xx | 修改 新增 删除  |
+----------------------------------------------------------+
```

### 2.2 搜索区域

| 字段 | 组件 | 说明 |
|------|------|------|
| 部门名称 | el-input | 模糊搜索，支持回车触发 |
| 类别编码 | el-input | 模糊搜索，支持回车触发 |
| 状态 | el-select | 下拉选择，选项来自字典 sys_normal_disable |

### 2.3 工具栏按钮

| 按钮 | 权限标识 | 禁用条件 | 操作 |
|------|----------|----------|------|
| 新增 | system:dept:add | 无 | 打开新增部门弹窗 |
| 展开/折叠 | 无 | 无 | 切换所有树节点的展开/折叠状态 |

### 2.4 数据表格

| 列名 | 字段 | 宽度 | 说明 |
|------|------|------|------|
| 部门名称 | deptName | 260px | 树形列，支持展开/折叠 |
| 类别编码 | deptCategory | 200px | 居中显示 |
| 排序 | orderNum | 200px | 居中显示 |
| 状态 | status | 100px | dict-tag 组件显示 |
| 创建时间 | createTime | 200px | 格式化显示 |
| 操作 | - | 180px | 固定右侧 |

**表格配置**:

```
row-key: "deptId"
border: true
tree-props: { children: 'children', hasChildren: 'hasChildren' }
default-expand-all: true
```

### 2.5 操作列按钮

| 按钮 | 权限标识 | 图标 | 操作 |
|------|----------|------|------|
| 修改 | system:dept:edit | Edit | 打开修改弹窗 |
| 新增 | system:dept:add | Plus | 打开新增弹窗，自动设置当前部门为上级部门 |
| 删除 | system:dept:remove | Delete | 删除确认并执行 |

### 2.6 新增/修改部门弹窗

**弹窗标题**: "添加部门" 或 "修改部门"
**弹窗宽度**: 600px

**表单字段**:

| 字段 | 组件 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| 上级部门 | el-tree-select | 条件必填 | 根部门（parentId=0）时不显示 | 树形选择，check-strictly，排除当前部门及子部门（修改时） |
| 部门名称 | el-input | 是 | 不能为空 | - |
| 类别编码 | el-input | 否 | - | 部门类型标识 |
| 显示排序 | el-input-number | 是 | 不能为空 | 最小值0，右侧控制按钮 |
| 负责人 | el-select | 否 | - | 下拉选择，选项为当前部门的所有用户 |
| 联系电话 | el-input | 否 | 手机号格式校验 | 最大长度11 |
| 邮箱 | el-input | 否 | 邮箱格式校验 | 最大长度50 |
| 部门状态 | el-radio-group | 是 | - | 选项来自字典 sys_normal_disable |

**上级部门树选择配置**:

```
node-key: "deptId"
props: { value: 'deptId', label: 'deptName', children: 'children' }
value-key: "deptId"
check-strictly: true
placeholder: "选择上级部门"
```

**修改时的特殊处理**:
- 调用 `listDeptExcludeChild(deptId)` 获取排除当前部门及子部门的部门列表
- 如果返回结果为空，手动添加当前部门的父部门作为唯一选项

### 2.7 表单初始化值

```typescript
const initForm: DeptForm = {
  deptId: undefined,
  parentId: undefined,
  deptName: undefined,
  deptCategory: undefined,
  orderNum: 0,
  leader: undefined,
  phone: undefined,
  email: undefined,
  status: '0'
};
```

### 2.8 查询参数

```typescript
queryParams: {
  pageNum: 1,
  pageSize: 10,
  deptName: undefined,
  deptCategory: undefined,
  status: undefined
}
```

### 2.9 表单校验规则

```typescript
rules: {
  parentId: [{ required: true, message: '上级部门不能为空', trigger: 'blur' }],
  deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }],
  orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3456789][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
}
```

### 2.10 交互行为

| 行为 | 说明 |
|------|------|
| 新增根部门 | 不显示上级部门选择字段（parentId === 0） |
| 新增子部门 | 点击行操作的新增按钮，自动设置当前部门为上级部门 |
| 修改部门 | 加载部门详情，加载当前部门的所有用户作为负责人选项 |
| 删除部门 | 弹出确认框，确认后执行删除，删除成功后刷新列表 |
| 展开/折叠 | 切换 isExpandAll 状态，递归调用 toggleRowExpansion |

### 2.11 负责人加载逻辑

- 修改部门时，调用 `listUserByDeptId(deptId)` 获取当前部门的所有用户
- 负责人下拉列表仅显示该部门的用户
- 负责人字段存储的是用户ID

---

## 3. 路由配置

| 路由路径 | 组件 | 说明 |
|----------|------|------|
| `/system/dept` | system/dept/index | 部门管理主页面 |

---

## 4. 字典引用

| 字典类型 | 用途 |
|----------|------|
| sys_normal_disable | 状态显示（正常/停用） |

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，部门管理前端页面定义 |
