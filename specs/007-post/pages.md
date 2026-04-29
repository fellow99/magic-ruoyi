# 岗位管理模块前端页面文档（007-post/pages.md）

> magic-ruoyi 岗位管理模块前端页面定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 页面概览

| 页面 | 文件路径 | 路由 | 描述 |
|------|----------|------|------|
| 岗位管理 | `src/views/system/post/index.vue` | `/system/post` | 岗位 CRUD 管理，含左侧部门树 |

**公共依赖**:
- `@/api/system/post` - 岗位 API 封装
- `@/api/system/dept/types` - 部门类型定义
- `dict-tag` - 字典标签组件
- `pagination` - 分页组件
- `right-toolbar` - 右侧工具栏

---

## 2. 岗位管理页面（index.vue）

### 2.1 页面布局

```
┌─────────────────────────────────────────────────────────┐
│  [左侧部门树 4/24]        │  [搜索区域 20/24]            │
│  ┌──────────────────┐    │  ┌──────────────────────┐   │
│  │ [搜索部门输入框]  │    │  │ 岗位编码  类别编码    │   │
│  │                  │    │  │ 岗位名称  部门        │   │
│  │  ┌─ 总公司        │    │  │ 状态      [搜索][重置]│   │
│  │  ├─ 深圳分公司    │    │  └──────────────────────┘   │
│  │  └─ 长沙分公司    │    │                              │
│  │                  │    │  ┌──────────────────────┐   │
│  │                  │    │  │ [新增][修改][删除][导出]│   │
│  │                  │    │  │ ┌──────────────────┐ │   │
│  │                  │    │  │ │ 岗位列表表格      │ │   │
│  │                  │    │  │ │ 编码|名称|部门|状态│ │   │
│  │                  │    │  │ └──────────────────┘ │   │
│  │                  │    │  │ [分页控件]            │   │
│  └──────────────────┘    │  └──────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 2.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 部门搜索 | `<el-input>` | 部门树过滤输入框 |
| 部门树 | `<el-tree>` | 左侧部门树，支持搜索过滤 |
| 查询表单 | `<el-form>` | 岗位查询条件表单 |
| 岗位编码输入 | `<el-input>` | 模糊搜索岗位编码 |
| 类别编码输入 | `<el-input>` | 模糊搜索类别编码 |
| 岗位名称输入 | `<el-input>` | 模糊搜索岗位名称 |
| 部门下拉 | `<el-tree-select>` | 树形部门选择器 |
| 状态下拉 | `<el-select>` | 正常/停用筛选 |
| 新增按钮 | `<el-button>` | 权限: `system:post:add` |
| 修改按钮 | `<el-button>` | 权限: `system:post:edit`，未选中时禁用 |
| 删除按钮 | `<el-button>` | 权限: `system:post:remove`，未选中时禁用 |
| 导出按钮 | `<el-button>` | 权限: `system:post:export` |
| 数据表格 | `<el-table>` | 岗位列表，支持多选 |
| 分页 | `<pagination>` | 分页控件 |
| 对话框 | `<el-dialog>` | 新增/修改岗位表单 |

### 2.3 表单数据模型

```typescript
interface PostForm {
  postId: number | string | undefined;
  deptId: number | string | undefined;
  postCode: string;
  postName: string;
  postCategory: string;
  postSort: number;
  status: string;
  remark: string;
}

interface PostQuery extends PageQuery {
  deptId: number | string;
  belongDeptId: number | string;
  postCode: string;
  postName: string;
  postCategory: string;
  status: string;
}
```

### 2.4 表单校验规则

| 字段 | 规则 | 触发时机 | 错误提示 |
|------|------|----------|----------|
| postName | 必填 | blur | 岗位名称不能为空 |
| postCode | 必填 | blur | 岗位编码不能为空 |
| deptId | 必填 | blur | 部门不能为空 |
| postSort | 必填 | blur | 岗位顺序不能为空 |

### 2.5 页面生命周期

```
onMounted()
  ├── getTreeSelect()  → 获取部门树结构
  └── getList()        → 获取岗位列表
```

### 2.6 核心交互逻辑

#### 2.6.1 部门树节点点击

```
handleNodeClick(data: DeptVO)
  ├── 设置 queryParams.belongDeptId = data.id
  ├── 清空 queryParams.deptId
  └── 触发 handleQuery() 刷新列表
```

#### 2.6.2 查询操作

```
handleQuery()
  ├── 重置页码为 1
  ├── 若 deptId 有值，清空 belongDeptId
  └── 调用 getList()
```

#### 2.6.3 重置操作

```
resetQuery()
  ├── 重置查询表单
  ├── 重置页码为 1
  ├── 清空 deptId
  ├── 清空部门树选中状态
  ├── 清空 belongDeptId
  └── 调用 handleQuery()
```

#### 2.6.4 新增/修改提交

```
submitForm()
  ├── 表单校验
  ├── 若有 postId → updatePost(form)
  ├── 若无 postId → addPost(form)
  ├── 提示"操作成功"
  ├── 关闭对话框
  └── 刷新列表
```

#### 2.6.5 删除操作

```
handleDelete(row?)
  ├── 确认对话框: "是否确认删除岗位编号为'{postIds}'的数据项？"
  ├── 调用 delPost(postIds)
  ├── 刷新列表
  └── 提示"删除成功"
```

### 2.7 响应式布局

| 断点 | 部门树宽度 | 主内容宽度 |
|------|-----------|-----------|
| lg (>=1024px) | 4/24 | 20/24 |
| xs (<1024px) | 24/24 | 24/24 |

### 2.8 使用的字典

| 字典 | 用途 |
|------|------|
| sys_normal_disable | 岗位状态展示（正常/停用） |

---

## 3. 数据流图

```
┌──────────────┐     ┌──────────────┐
│  index.vue   │     │  post API    │
│              │     │              │
│  postList ───┼────>│  listPost()  │
│  deptOptions─┼────>│  deptTree()  │
│  form ───────┼────>│  add/update  │
│              │     │  delPost()   │
└──────┬───────┘     └──────┬───────┘
       │                    │
       ▼                    ▼
┌──────────────┐     ┌──────────────┐
│  Element Plus│     │  Axios       │
│  Components  │     │  Request     │
└──────────────┘     └──────────────┘
```

---

## 4. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
