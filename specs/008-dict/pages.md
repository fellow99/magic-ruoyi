# 字典管理模块前端页面文档（008-dict/pages.md）

> magic-ruoyi 字典管理模块前端页面定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 页面概览

| 页面 | 文件路径 | 路由 | 描述 |
|------|----------|------|------|
| 字典类型管理 | `src/views/system/dict/index.vue` | `/system/dict` | 字典类型 CRUD |
| 字典数据管理 | `src/views/system/dict/data.vue` | `/system/dict-data/index/:dictId` | 字典数据 CRUD |

**公共依赖**:
- `@/api/system/dict/type` - 字典类型 API
- `@/api/system/dict/data` - 字典数据 API
- `@/store/modules/dict` - 字典状态管理
- `dict-tag` - 字典标签组件
- `pagination` - 分页组件
- `right-toolbar` - 右侧工具栏

---

## 2. 字典类型管理页面（index.vue）

### 2.1 页面布局

```
┌────────────────────────────────────────────────────┐
│  [搜索区域]                                         │
│  ┌──────────────────────────────────────────────┐  │
│  │ 字典名称  字典类型  创建时间范围  [搜索][重置] │  │
│  └──────────────────────────────────────────────┘  │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │ [新增][修改][删除][导出][刷新缓存]             │  │
│  │ ┌──────────────────────────────────────────┐ │  │
│  │ │ 字典类型列表表格                          │ │  │
│  │ │ 名称 | 类型(可点击) | 备注 | 创建时间     │ │  │
│  │ └──────────────────────────────────────────┘ │  │
│  │ [分页控件]                                    │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### 2.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 字典名称输入 | `<el-input>` | 模糊搜索 |
| 字典类型输入 | `<el-input>` | 模糊搜索 |
| 创建时间 | `<el-date-picker>` | 日期范围选择 |
| 新增按钮 | `<el-button>` | 权限: `system:dict:add` |
| 修改按钮 | `<el-button>` | 权限: `system:dict:edit` |
| 删除按钮 | `<el-button>` | 权限: `system:dict:remove` |
| 导出按钮 | `<el-button>` | 权限: `system:dict:export` |
| 刷新缓存 | `<el-button>` | 权限: `system:dict:remove` |
| 数据表格 | `<el-table>` | 支持多选 |
| 字典类型链接 | `<router-link>` | 点击跳转到字典数据页 |

### 2.3 表单数据模型

```typescript
interface DictTypeForm {
  dictId: number | string | undefined;
  dictName: string;
  dictType: string;
  remark: string;
}

interface DictTypeQuery extends PageQuery {
  dictName: string;
  dictType: string;
}
```

### 2.4 表单校验规则

| 字段 | 规则 | 触发时机 | 错误提示 |
|------|------|----------|----------|
| dictName | 必填 | blur | 字典名称不能为空 |
| dictType | 必填 | blur | 字典类型不能为空 |

### 2.5 核心交互逻辑

#### 2.5.1 刷新缓存

```
handleRefreshCache()
  ├── 调用 refreshCache() API
  ├── 提示"刷新成功"
  └── useDictStore().cleanDict() 清除前端缓存
```

#### 2.5.2 删除操作

```
handleDelete(row?)
  ├── 确认对话框
  ├── 调用 delType(dictIds)
  ├── 刷新列表
  └── 提示"删除成功"
```

#### 2.5.3 导出操作

```
handleExport()
  └── proxy?.download('system/dict/type/export', queryParams, `dict_${timestamp}.xlsx`)
```

---

## 3. 字典数据管理页面（data.vue）

### 3.1 页面布局

```
┌────────────────────────────────────────────────────┐
│  [搜索区域]                                         │
│  ┌──────────────────────────────────────────────┐  │
│  │ 字典类型(下拉)  字典标签  [搜索][重置]         │  │
│  └──────────────────────────────────────────────┘  │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │ [新增][修改][删除][导出][关闭]                 │  │
│  │ ┌──────────────────────────────────────────┐ │  │
│  │ │ 字典数据列表表格                          │ │  │
│  │ │ 标签(Tag) | 键值 | 排序 | 备注 | 创建时间  │ │  │
│  │ └──────────────────────────────────────────┘ │  │
│  │ [分页控件]                                    │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### 3.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 字典类型下拉 | `<el-select>` | 切换字典类型 |
| 字典标签输入 | `<el-input>` | 模糊搜索 |
| 关闭按钮 | `<el-button>` | 返回字典类型列表页 |
| 数据标签展示 | `<el-tag>` | 根据 listClass 显示不同颜色 |
| 新增/修改对话框 | `<el-dialog>` | 字典数据表单 |

### 3.3 表单数据模型

```typescript
interface DictDataForm {
  dictType?: string;
  dictCode: string | undefined;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  listClass: ElTagType;
  dictSort: number;
  remark: string;
}
```

### 3.4 表单校验规则

| 字段 | 规则 | 触发时机 | 错误提示 |
|------|------|----------|----------|
| dictLabel | 必填 | blur | 数据标签不能为空 |
| dictValue | 必填 | blur | 数据键值不能为空 |
| dictSort | 必填 | blur | 数据顺序不能为空 |

### 3.5 回显样式选项

```typescript
const listClassOptions = [
  { value: 'default', label: '默认' },
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'info', label: '信息' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' }
];
```

### 3.6 页面生命周期

```
onMounted()
  ├── getTypes(route.params.dictId)  → 根据路由参数加载字典类型
  └── getTypeList()                   → 加载字典类型下拉选项
```

### 3.7 核心交互逻辑

#### 3.7.1 加载字典类型

```
getTypes(dictId)
  ├── 调用 getType(dictId) 获取字典类型详情
  ├── 设置 queryParams.dictType = data.dictType
  ├── 设置 defaultDictType = data.dictType
  └── 调用 getList()
```

#### 3.7.2 提交表单

```
submitForm()
  ├── 表单校验
  ├── 调用 addData/updateData
  ├── useDictStore().removeDict(dictType) 清除对应缓存
  ├── 提示"操作成功"
  ├── 关闭对话框
  └── 刷新列表
```

#### 3.7.3 删除操作

```
handleDelete(row?)
  ├── 确认对话框
  ├── 调用 delData(dictCodes)
  ├── 刷新列表
  ├── useDictStore().removeDict(dictType) 清除缓存
  └── 提示"删除成功"
```

#### 3.7.4 关闭返回

```
handleClose()
  └── proxy?.$tab.closeOpenPage({ path: '/system/dict' })
```

---

## 4. 数据流图

```
┌──────────────────┐     ┌──────────────────┐
│  dict/index.vue  │     │  dict/data.vue   │
│                  │     │                  │
│  typeList ───────┼──┐  │  dataList ───────┼──┐
│  form ───────────┼──┤  │  form ───────────┼──┤
│  typeOptions ────┼──┤  │  typeOptions ────┼──┤
└────────┬─────────┘  │  └────────┬─────────┘  │
         │            │           │            │
         ▼            │           ▼            │
┌──────────────────┐  │  ┌──────────────────┐  │
│  dict/type API   │  │  │  dict/data API   │  │
│                  │  │  │                  │  │
│  listType()      │  │  │  listData()      │  │
│  getType()       │  │  │  getData()       │  │
│  addType()       │  │  │  addData()       │  │
│  updateType()    │  │  │  updateData()    │  │
│  delType()       │  │  │  delData()       │  │
│  refreshCache()  │  │  │  getDicts()      │  │
│  optionselect() ─┼──┘  └──────────────────┘  │
└──────────────────┘                           │
         │                                     │
         ▼                                     ▼
┌──────────────────────────────────────────────────┐
│  @/store/modules/dict (dictStore)                │
│                                                  │
│  cleanDict()    → 清除全部字典缓存               │
│  removeDict()   → 清除指定类型缓存               │
└──────────────────────────────────────────────────┘
```

---

## 5. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
