# 参数配置模块前端页面文档（009-config/pages.md）

> magic-ruoyi 参数配置模块前端页面定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 页面概览

| 页面 | 文件路径 | 路由 | 描述 |
|------|----------|------|------|
| 参数配置 | `src/views/system/config/index.vue` | `/system/config` | 系统参数 CRUD 管理 |

**公共依赖**:
- `@/api/system/config` - 参数 API 封装
- `dict-tag` - 字典标签组件
- `pagination` - 分页组件
- `right-toolbar` - 右侧工具栏

---

## 2. 参数配置页面（index.vue）

### 2.1 页面布局

```
┌────────────────────────────────────────────────────┐
│  [搜索区域]                                         │
│  ┌──────────────────────────────────────────────┐  │
│  │ 参数名称  参数键名  系统内置  创建时间范围    │  │
│  │                        [搜索][重置]           │  │
│  └──────────────────────────────────────────────┘  │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │ [新增][修改][删除][导出][刷新缓存]             │  │
│  │ ┌──────────────────────────────────────────┐ │  │
│  │ │ 参数列表表格                              │ │  │
│  │ │ 名称 | 键名 | 键值 | 内置 | 备注 | 时间   │ │  │
│  │ └──────────────────────────────────────────┘ │  │
│  │ [分页控件]                                    │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### 2.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 参数名称输入 | `<el-input>` | 模糊搜索 |
| 参数键名输入 | `<el-input>` | 模糊搜索 |
| 系统内置下拉 | `<el-select>` | Y/N 筛选，使用 sys_yes_no 字典 |
| 创建时间 | `<el-date-picker>` | 日期范围选择 |
| 新增按钮 | `<el-button>` | 权限: `system:config:add` |
| 修改按钮 | `<el-button>` | 权限: `system:config:edit` |
| 删除按钮 | `<el-button>` | 权限: `system:config:remove` |
| 导出按钮 | `<el-button>` | 权限: `system:config:export` |
| 刷新缓存 | `<el-button>` | 权限: `system:config:remove` |
| 数据表格 | `<el-table>` | 支持多选 |
| 系统内置展示 | `<dict-tag>` | 使用 sys_yes_no 字典 |

### 2.3 表单数据模型

```typescript
interface ConfigForm {
  configId: number | string | undefined;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  remark: string;
}

interface ConfigQuery extends PageQuery {
  configName: string;
  configKey: string;
  configType: string;
}
```

### 2.4 表单校验规则

| 字段 | 规则 | 触发时机 | 错误提示 |
|------|------|----------|----------|
| configName | 必填 | blur | 参数名称不能为空 |
| configKey | 必填 | blur | 参数键名不能为空 |
| configValue | 必填 | blur | 参数键值不能为空 |

### 2.5 页面生命周期

```
onMounted()
  └── getList()  → 获取参数列表
```

### 2.6 核心交互逻辑

#### 2.6.1 查询操作

```
handleQuery()
  ├── 重置页码为 1
  └── 调用 getList()（含日期范围）
```

#### 2.6.2 刷新缓存

```
handleRefreshCache()
  ├── 调用 refreshCache() API
  └── 提示"刷新缓存成功"
```

#### 2.6.3 提交表单

```
submitForm()
  ├── 表单校验
  ├── 若有 configId → updateConfig(form)
  ├── 若无 configId → addConfig(form)
  ├── 提示"操作成功"
  ├── 关闭对话框
  └── 刷新列表
```

### 2.7 使用的字典

| 字典 | 用途 |
|------|------|
| sys_yes_no | 系统内置标识展示（是/否） |

---

## 3. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
