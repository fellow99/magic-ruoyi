# 客户端管理模块前端页面文档（012-client/pages.md）

> magic-ruoyi 客户端管理模块前端页面定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 页面概览

| 页面 | 文件路径 | 路由 | 描述 |
|------|----------|------|------|
| 客户端管理 | `src/views/system/client/index.vue` | `/system/client` | OAuth 客户端 CRUD 管理 |

**公共依赖**:
- `@/api/system/client` - 客户端 API 封装
- `pagination` - 分页组件
- `right-toolbar` - 右侧工具栏

---

## 2. 客户端管理页面（index.vue）

### 2.1 页面布局

```
┌────────────────────────────────────────────────────┐
│  [搜索区域]                                         │
│  ┌──────────────────────────────────────────────┐  │
│  │ 客户端ID  客户端Key  授权类型  设备类型  状态  │  │
│  │                              [搜索][重置]     │  │
│  └──────────────────────────────────────────────┘  │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │ [新增][修改][删除]                             │  │
│  │ ┌──────────────────────────────────────────┐ │  │
│  │ │ 客户端列表表格                            │ │  │
│  │ │ ID | Key | 秘钥 | 授权类型 | 设备 | 状态  │ │  │
│  │ └──────────────────────────────────────────┘ │  │
│  │ [分页控件]                                    │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### 2.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 客户端 ID 输入 | `<el-input>` | 精确搜索 |
| 客户端 Key 输入 | `<el-input>` | 模糊搜索 |
| 授权类型输入 | `<el-input>` | 筛选 |
| 设备类型输入 | `<el-input>` | 筛选 |
| 状态下拉 | `<el-select>` | 正常/停用 |
| 新增按钮 | `<el-button>` | 权限: `system:client:add` |
| 修改按钮 | `<el-button>` | 权限: `system:client:edit` |
| 删除按钮 | `<el-button>` | 权限: `system:client:remove` |
| 数据表格 | `<el-table>` | 支持多选 |

### 2.3 表单数据模型

```typescript
interface ClientForm extends BaseEntity {
  id?: string | number;
  clientId?: string | number;
  clientKey?: string;
  clientSecret?: string;
  grantTypeList?: string[];
  deviceType?: string;
  activeTimeout?: number;
  timeout?: number;
  status?: string;
}

interface ClientQuery extends PageQuery {
  clientId?: string | number;
  clientKey?: string;
  clientSecret?: string;
  grantType?: string;
  deviceType?: string;
  activeTimeout?: number;
  timeout?: number;
  status?: string;
}
```

### 2.4 核心交互逻辑

#### 2.4.1 提交表单

```
submitForm()
  ├── 表单校验
  ├── 若有 id → updateClient(form)
  ├── 若无 id → addClient(form)
  ├── 提示"操作成功"
  ├── 关闭对话框
  └── 刷新列表
```

#### 2.4.2 状态切换

```
handleStatusChange(row)
  ├── 调用 changeStatus(row.clientId, row.status)
  └── 提示"操作成功"
```

#### 2.4.3 删除操作

```
handleDelete(row?)
  ├── 确认对话框
  ├── 调用 delClient(ids)
  ├── 刷新列表
  └── 提示"删除成功"
```

---

## 3. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
