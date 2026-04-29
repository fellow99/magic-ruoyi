# 代码生成模块前端页面文档（014-codegen/pages.md）

> magic-ruoyi 代码生成模块前端页面定义。描述代码生成列表页、导入表弹窗、编辑页的组件结构、交互逻辑与数据流。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 页面概览

| 页面 | 文件路径 | 路由 | 描述 |
|------|----------|------|------|
| 代码生成列表 | `src/views/tool/gen/index.vue` | `/tool/gen` | 已导入表列表管理 |
| 导入表弹窗 | `src/views/tool/gen/importTable.vue` | 弹窗组件 | 从数据库选择表导入 |
| 编辑表配置 | `src/views/tool/gen/editTable.vue` | `/tool/gen-edit/index/:tableId` | 编辑表基本信息、字段信息、生成信息 |
| 基本信息表单 | `src/views/tool/gen/basicInfoForm.vue` | 子组件 | 表基本信息编辑表单 |
| 生成信息表单 | `src/views/tool/gen/genInfoForm.vue` | 子组件 | 生成配置信息编辑表单 |

**公共依赖**:
- `@/api/tool/gen` - 代码生成 API 封装
- `@/api/tool/gen/types` - TypeScript 类型定义
- `@/api/system/dict/type` - 字典类型 API（编辑页下拉列表）
- `Pagination` - 分页组件
- `RightToolbar` - 右侧工具栏组件

---

## 2. 代码生成列表页（index.vue）

### 2.1 页面布局

```
┌─────────────────────────────────────────────────────┐
│  [搜索区域]（可折叠）                                  │
│  数据源: [下拉]  表名称: [输入]  表描述: [输入]        │
│  创建时间: [日期范围选择器]  [搜索] [重置]             │
├─────────────────────────────────────────────────────┤
│  [工具栏]                                            │
│  [生成] [导入] [修改] [删除]              [显示搜索▼] │
├─────────────────────────────────────────────────────┤
│  [表格]                                              │
│  ☑ 序号 | 数据源 | 表名称 | 表描述 | 实体 | 创建时间  │
│     | 更新时间 | 操作                                │
│     操作: [预览] [编辑] [删除] [同步] [生成代码]       │
├─────────────────────────────────────────────────────┤
│  [分页]                                              │
├─────────────────────────────────────────────────────┤
│  [代码预览对话框]（80% 宽度）                          │
│  Tab 页签: domain.java | mapper.java | ...           │
│  [复制] 按钮 + 语法高亮代码                            │
└─────────────────────────────────────────────────────┘
```

### 2.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 搜索表单 | `<el-form>` | 数据源、表名称、表描述、创建时间范围 |
| 数据源下拉 | `<el-select>` | 可搜索，从 `getDataNames()` 加载 |
| 工具栏按钮 | `<el-button>` | 生成、导入、修改、删除，带权限控制 |
| 数据表格 | `<el-table>` | 支持多选、行内操作 |
| 操作按钮 | `<el-button link>` | 预览、编辑、删除、同步、生成代码 |
| 分页 | `<pagination>` | 页码和每页条数 |
| 预览对话框 | `<el-dialog>` | 80% 宽度，Tab 页签展示代码 |
| 导入表组件 | `<import-table>` | 子组件，导入成功后触发 `ok` 事件 |

### 2.3 表单数据模型

```typescript
interface TableQuery extends PageQuery {
  tableName: string;      // 表名称
  tableComment: string;   // 表描述
  dataName: string;       // 数据源名称
}
```

### 2.4 核心交互逻辑

#### 2.4.1 列表加载

```
onMounted()
  ├── getList()          → 查询已导入表列表
  └── getDataNameList()  → 获取数据源名称列表
```

#### 2.4.2 生成代码

```
handleGenTable(row?)
  ├── 若 row 存在 → 取 row.tableId
  ├── 若 row 不存在 → 取选中的 ids
  ├── 若 genType === '1' → 调用 genCode() 生成到自定义路径
  └── 否则 → 调用 proxy.$download.zip() 下载 ZIP
```

#### 2.4.3 预览代码

```
handlePreview(row)
  ├── 调用 previewTable(row.tableId)
  ├── 设置 preview.data = res.data（键值对：文件名 → 代码内容）
  ├── 设置 dialog.visible = true
  └── 默认选中 'domain.java' 页签
```

#### 2.4.4 同步数据库

```
handleSynchDb(row)
  ├── 弹出确认对话框："确认要强制同步表结构吗？"
  ├── 调用 synchDb(row.tableId)
  └── 提示"同步成功"
```

#### 2.4.5 导入表

```
openImportTable()
  └── 调用 importRef.value.show(queryParams.dataName)
      → 打开导入表弹窗，传入当前数据源
      → 导入成功后触发 @ok → handleQuery() 刷新列表
```

#### 2.4.6 编辑表

```
handleEditTable(row?)
  ├── 取 tableId = row?.tableId || ids.value[0]
  └── 路由跳转 → /tool/gen-edit/index/{tableId}?pageNum={当前页码}
```

#### 2.4.7 删除表

```
handleDelete(row?)
  ├── 弹出确认对话框
  ├── 调用 delTable(tableIds)
  └── 刷新列表
```

---

## 3. 导入表弹窗（importTable.vue）

### 3.1 页面布局

```
┌──────────────────────────────────────────┐
│  导入表                            [X]    │
├──────────────────────────────────────────┤
│  数据源: [下拉]  表名称: [输入]  表描述: [输入] │
│  [搜索] [重置]                            │
├──────────────────────────────────────────┤
│  [表格]（固定高度 260px）                   │
│  ☑ 表名称 | 表描述 | 创建时间 | 更新时间    │
├──────────────────────────────────────────┤
│  [分页]                                   │
├──────────────────────────────────────────┤
│                    [确定] [取消]           │
└──────────────────────────────────────────┘
```

### 3.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 搜索表单 | `<el-form>` | 数据源、表名称、表描述 |
| 数据表格 | `<el-table>` | 支持多选，固定高度 260px |
| 分页 | `<pagination>` | 页码和每页条数 |
| 确定/取消 | `<el-button>` | 确定导入或取消关闭 |

### 3.3 核心交互逻辑

```
show(dataName)
  ├── 调用 getDataNames() 获取数据源列表
  ├── 设置 queryParams.dataName = dataName 或第一个数据源
  ├── 调用 getList() 加载数据库表列表
  └── 设置 visible = true 显示弹窗

handleImportTable()
  ├── 校验已选表不为空
  ├── 调用 importTable({ tables: 逗号分隔表名, dataName })
  ├── 成功后关闭弹窗
  └── 触发 emit('ok') 通知父组件刷新
```

---

## 4. 编辑表配置页（editTable.vue）

### 4.1 页面布局

```
┌─────────────────────────────────────────────────────┐
│  [Tab 页签]                                          │
│  ┌─────────┬─────────┬─────────┐                    │
│  │ 基本信息 │ 字段信息 │ 生成信息 │                    │
│  └─────────┴─────────┴─────────┘                    │
│                                                     │
│  [基本信息 Tab]                                      │
│    表名称、表描述、实体类名、作者、备注等               │
│                                                     │
│  [字段信息 Tab]                                      │
│    [表格] 可编辑每一列:                                │
│    序号 | 字段列名 | 字段描述 | 物理类型 | Java类型   │
│    | Java属性 | 插入 | 编辑 | 列表 | 查询 | 查询方式  │
│    | 必填 | 显示类型 | 字典类型                       │
│                                                     │
│  [生成信息 Tab]                                      │
│    生成模板、包名、模块名、业务名、功能名、作者          │
│    生成方式、生成路径、上级菜单等                       │
│                                                     │
│  [提交] [返回]                                       │
└─────────────────────────────────────────────────────┘
```

### 4.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| Tab 容器 | `<el-tabs>` | 基本信息、字段信息、生成信息三个页签 |
| 基本信息表单 | `<basic-info-form>` | 子组件，编辑表基本信息 |
| 字段表格 | `<el-table>` | 可编辑表格，每行对应一个字段 |
| Java 类型下拉 | `<el-select>` | Long/String/Integer/Double/BigDecimal/Date/Boolean |
| 查询方式下拉 | `<el-select>` | EQ/NE/GT/GE/LT/LE/LIKE/BETWEEN |
| 显示类型下拉 | `<el-select>` | input/textarea/select/radio/checkbox/datetime 等 |
| 字典类型下拉 | `<el-select>` | 从字典类型 API 加载，可搜索 |
| 复选框 | `<el-checkbox>` | 插入/编辑/列表/查询/必填标志 |
| 生成信息表单 | `<gen-info-form>` | 子组件，编辑生成配置 |

### 4.3 页面生命周期

```
onMounted (async IIFE)
  ├── 从路由参数获取 tableId
  ├── 调用 getGenTable(tableId) 获取表详情
  │   ├── columns.value = res.data.rows（字段列表）
  │   ├── info.value = res.data.info（表信息）
  │   └── tables.value = res.data.tables（关联表）
  └── 调用 getDictOptionselect() 获取字典类型下拉列表
```

### 4.4 核心交互逻辑

#### 4.4.1 提交表单

```
submitForm()
  ├── 获取 basicInfoForm 和 genInfoForm 的表单引用
  ├── 并行执行两个表单的 validate()
  ├── 若全部通过校验:
  │   ├── 合并 info.value + columns.value + params
  │   ├── params 包含 treeCode、treeName、treeParentCode、parentMenuId
  │   ├── 调用 updateGenTable(genTable)
  │   └── 成功后提示并返回
  └── 若校验失败 → 提示"表单校验未通过"
```

#### 4.4.2 返回列表

```
close()
  └── 调用 proxy.$tab.closeOpenPage()
      → 跳转到 /tool/gen?t={timestamp}&pageNum={原页码}
```

---

## 5. 数据流图

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  index.vue   │     │ importTable  │     │  editTable   │
│              │     │              │     │              │
│  queryParams │     │  dbTableList │     │  columns     │
│  tableList   │◄────│  tables      │     │  info        │
│  preview     │     │  visible     │     │  dictOptions │
│              │     │              │     │              │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       ▼                    ▼                    ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  @/api/tool  │     │  @/api/tool  │     │  @/api/tool  │
│  /gen        │     │  /gen        │     │  /gen        │
│              │     │              │     │              │
│  listTable() │     │  listDbTable()│    │  getGenTable()│
│  previewTable│     │  importTable()│    │  updateGenTab│
│  delTable()  │     │  getDataNames│     │              │
│  genCode()   │     │              │     │              │
│  synchDb()   │     └──────────────┘     └──────────────┘
│  getDataNames│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Axios       │
│  Request     │
└──────────────┘
```

---

## 6. 权限控制

页面按钮通过 `v-hasPermi` 指令控制显示:

| 按钮 | 权限标识 |
|------|----------|
| 生成 | `tool:gen:code` |
| 导入 | `tool:gen:import` |
| 修改 | `tool:gen:edit` |
| 删除 | `tool:gen:remove` |
| 预览 | `tool:gen:preview` |

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
