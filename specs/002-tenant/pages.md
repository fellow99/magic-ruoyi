# 租户管理模块前端页面文档

> magic-ruoyi 租户管理模块的前端页面定义与交互说明。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 页面清单

| 页面 | 文件路径 | 路由 | 说明 |
|------|----------|------|------|
| 租户管理 | `src/views/system/tenant/index.vue` | `/system/tenant` | 租户增删改查、状态管理、数据同步 |
| 租户套餐管理 | `src/views/system/tenantPackage/index.vue` | `/system/tenantPackage` | 套餐增删改查、菜单权限配置 |

---

## 2. 租户管理页面

### 2.1 页面结构

```
租户管理页面
├── 搜索区域（可折叠）
│   ├── 租户编号（输入框）
│   ├── 联系人（输入框）
│   ├── 联系电话（输入框）
│   ├── 企业名称（输入框）
│   ├── 搜索按钮
│   └── 重置按钮
│
├── 工具栏
│   ├── 新增按钮（权限: system:tenant:add）
│   ├── 修改按钮（权限: system:tenant:edit）
│   ├── 删除按钮（权限: system:tenant:remove）
│   ├── 导出按钮（权限: system:tenant:export）
│   ├── 同步租户字典按钮（仅 userId=1）
│   ├── 同步租户参数配置按钮（仅 userId=1）
│   └── 显示/隐藏搜索区域切换
│
├── 数据表格
│   ├── 多选框列
│   ├── 租户编号
│   ├── 联系人
│   ├── 联系电话
│   ├── 企业名称
│   ├── 社会信用代码
│   ├── 过期时间（格式化显示）
│   ├── 租户状态（开关控件）
│   └── 操作列
│       ├── 修改按钮
│       ├── 同步套餐按钮
│       └── 删除按钮
│
└── 分页组件
```

### 2.2 搜索区域

| 字段 | 组件 | 绑定 | 说明 |
|------|------|------|------|
| 租户编号 | el-input | `queryParams.tenantId` | 精确匹配，支持回车搜索 |
| 联系人 | el-input | `queryParams.contactUserName` | 模糊匹配，支持回车搜索 |
| 联系电话 | el-input | `queryParams.contactPhone` | 模糊匹配，支持回车搜索 |
| 企业名称 | el-input | `queryParams.companyName` | 模糊匹配，支持回车搜索 |

**搜索行为**:
- 点击搜索或按 Enter 键触发查询，页码重置为 1
- 点击重置清空所有搜索条件并重新查询

### 2.3 数据表格

| 列 | 字段 | 宽度 | 对齐 | 说明 |
|----|------|------|------|------|
| 多选框 | selection | 55px | center | 支持多选 |
| 租户编号 | tenantId | auto | center | - |
| 联系人 | contactUserName | auto | center | - |
| 联系电话 | contactPhone | auto | center | - |
| 企业名称 | companyName | auto | center | - |
| 社会信用代码 | licenseNumber | auto | center | - |
| 过期时间 | expireTime | 180px | center | 格式化为 `yyyy-MM-dd` |
| 租户状态 | status | auto | center | el-switch 控件，0=正常，1=停用 |
| 操作 | - | 150px | center | 固定右侧 |

**状态开关交互**:
1. 用户点击开关切换状态
2. 弹出确认对话框："确认要'启用/停用'XXX租户吗？"
3. 确认后调用 `changeTenantStatus` API
4. 成功则提示"启用/停用成功"
5. 失败则开关自动回滚到原状态

**操作列按钮**:
- **修改**: 打开编辑对话框，加载租户详情
- **同步套餐**: 确认后调用 `syncTenantPackage` API，同步该租户的套餐权限
- **删除**: 确认后调用 `delTenant` API，逻辑删除

### 2.4 新增/编辑对话框

**对话框属性**:
- 宽度: 500px
- 标题: "添加租户" / "修改租户"
- 挂载到 body（append-to-body）

**表单字段**:

| 字段 | 组件 | 必填 | 验证规则 | 显示条件 |
|------|------|------|----------|----------|
| 企业名称 | el-input | 是 | 不能为空 | 始终显示 |
| 联系人 | el-input | 是 | 不能为空 | 始终显示 |
| 联系电话 | el-input | 是 | 不能为空 | 始终显示 |
| 用户名 | el-input | 是 | 不能为空，长度 2-20 | 仅新增时显示 |
| 用户密码 | el-input (password) | 是 | 不能为空，长度 5-20 | 仅新增时显示 |
| 租户套餐 | el-select | 否 | - | 始终显示，已有租户时禁用 |
| 过期时间 | el-date-picker | 否 | - | 始终显示 |
| 用户数量 | el-input | 否 | - | 始终显示 |
| 绑定域名 | el-input | 否 | - | 始终显示 |
| 企业地址 | el-input | 否 | - | 始终显示 |
| 企业代码 | el-input | 否 | - | 始终显示 |
| 企业简介 | el-input (textarea) | 否 | - | 始终显示 |
| 备注 | el-input | 否 | - | 始终显示 |

**套餐选择器**:
- 数据来源: `selectTenantPackage` API
- 已有租户的套餐字段禁用（`:disabled="!!form.tenantId"`）
- 支持清空选项

**提交行为**:
1. 触发表单验证
2. 验证通过后显示按钮 loading 状态
3. 根据 `form.id` 判断调用新增或修改 API
4. 成功后关闭对话框，刷新列表

### 2.5 工具栏按钮

| 按钮 | 权限标识 | 禁用条件 | 行为 |
|------|----------|----------|------|
| 新增 | system:tenant:add | 无 | 打开新增对话框，加载套餐列表 |
| 修改 | system:tenant:edit | 未选中或选中多条 | 打开编辑对话框，加载租户详情 |
| 删除 | system:tenant:remove | 未选中 | 确认后批量删除 |
| 导出 | system:tenant:export | 无 | 导出当前搜索条件的数据 |
| 同步租户字典 | 无（仅 userId=1） | 无 | 确认后同步所有租户字典 |
| 同步租户参数配置 | 无（仅 userId=1） | 无 | 确认后同步所有租户参数 |

---

## 3. 租户套餐管理页面

### 3.1 页面结构

```
租户套餐管理页面
├── 搜索区域（可折叠）
│   ├── 套餐名称（输入框）
│   ├── 搜索按钮
│   └── 重置按钮
│
├── 工具栏
│   ├── 新增按钮（权限: system:tenantPackage:add）
│   ├── 修改按钮（权限: system:tenantPackage:edit）
│   ├── 删除按钮（权限: system:tenantPackage:remove）
│   ├── 导出按钮（权限: system:tenantPackage:export）
│   └── 显示/隐藏搜索区域切换
│
├── 数据表格
│   ├── 多选框列
│   ├── 套餐名称
│   ├── 备注
│   ├── 状态（开关控件）
│   └── 操作列
│       ├── 修改按钮
│       └── 删除按钮
│
└── 分页组件
```

### 3.2 搜索区域

| 字段 | 组件 | 绑定 | 说明 |
|------|------|------|------|
| 套餐名称 | el-input | `queryParams.packageName` | 模糊匹配，支持回车搜索 |

### 3.3 数据表格

| 列 | 字段 | 宽度 | 对齐 | 说明 |
|----|------|------|------|------|
| 多选框 | selection | 55px | center | 支持多选 |
| 套餐名称 | packageName | auto | center | - |
| 备注 | remark | auto | center | - |
| 状态 | status | auto | center | el-switch 控件，0=正常，1=停用 |
| 操作 | - | auto | center | - |

**状态开关交互**: 同租户管理页面。

**操作列按钮**:
- **修改**: 打开编辑对话框，加载套餐详情和菜单树
- **删除**: 确认后调用 `delTenantPackage` API

### 3.4 新增/编辑对话框

**对话框属性**:
- 宽度: 500px
- 标题: "添加租户套餐" / "修改租户套餐"
- 挂载到 body

**表单字段**:

| 字段 | 组件 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| 套餐名称 | el-input | 是 | 不能为空 | - |
| 关联菜单 | el-tree | 否 | - | 树形控件，支持多选 |
| 备注 | el-input | 否 | - | - |

**菜单树控件**:

| 功能 | 组件 | 说明 |
|------|------|------|
| 展开/折叠 | el-checkbox | 控制所有节点的展开状态 |
| 全选/全不选 | el-checkbox | 一键选中或取消所有节点 |
| 父子联动 | el-checkbox | 绑定 `menuCheckStrictly`，控制是否关联选择父子节点 |
| 树形数据 | el-tree | `show-checkbox` 模式，node-key 为 `id` |

**菜单树数据来源**:
- API: `tenantPackageMenuTreeselect(packageId)`
- 新增时传 `packageId=0`，返回完整菜单树
- 编辑时传实际 `packageId`，返回菜单树和已选中的 `checkedKeys`

**菜单选择逻辑**:
1. 获取已勾选的节点 keys（`getCheckedKeys`）
2. 获取半勾选的节点 keys（`getHalfCheckedKeys`）
3. 合并两者作为最终的 `menuIds`
4. 提交时转换为逗号分隔的字符串

**提交行为**: 同租户管理页面。

---

## 4. 组件依赖

### 4.1 公共组件

| 组件 | 用途 |
|------|------|
| `Pagination` | 分页组件 |
| `RightToolbar` | 右侧工具栏（搜索区域切换） |

### 4.2 Element Plus 组件

| 组件 | 用途 |
|------|------|
| `el-card` | 卡片容器 |
| `el-form` / `el-form-item` | 表单布局 |
| `el-input` | 文本输入 |
| `el-select` / `el-option` | 下拉选择 |
| `el-date-picker` | 日期时间选择 |
| `el-button` | 按钮 |
| `el-table` / `el-table-column` | 数据表格 |
| `el-dialog` | 对话框 |
| `el-switch` | 状态开关 |
| `el-tree` | 树形控件 |
| `el-checkbox` | 复选框 |
| `el-tooltip` | 工具提示 |
| `el-row` / `el-col` | 栅格布局 |

### 4.3 状态管理

| Store | 用途 |
|-------|------|
| `useUserStore` | 获取当前用户 ID，判断是否为超级管理员 |

---

## 5. 路由配置

```typescript
{
  path: '/system/tenant',
  component: Layout,
  hidden: false,
  children: [
    {
      path: 'tenant',
      component: () => import('@/views/system/tenant/index.vue'),
      name: 'Tenant',
      meta: { title: '租户管理', icon: 'home', perms: ['system:tenant:list'] }
    }
  ]
},
{
  path: '/system/tenantPackage',
  component: Layout,
  hidden: false,
  children: [
    {
      path: 'tenantPackage',
      component: () => import('@/views/system/tenantPackage/index.vue'),
      name: 'TenantPackage',
      meta: { title: '租户套餐管理', icon: 'list', perms: ['system:tenantPackage:list'] }
    }
  ]
}
```

---

## 6. 交互流程图

### 6.1 新增租户流程

```
用户点击"新增"按钮
    │
    ├── 1. 重置表单
    ├── 2. 加载套餐下拉列表（selectTenantPackage）
    ├── 3. 打开对话框，标题设为"添加租户"
    │
    └── 用户填写表单
            │
            ├── 填写必填字段（企业名称、联系人、联系电话、用户名、密码）
            ├── 选择租户套餐
            ├── 设置过期时间、用户数量等可选字段
            │
            └── 点击"确定"
                    │
                    ├── 表单验证
                    │   ├── 失败 → 显示错误提示
                    │   └── 成功 → 继续
                    │
                    ├── 调用 addTenant API（RSA 加密传输）
                    │   ├── 失败 → 显示错误提示
                    │   └── 成功 → 继续
                    │
                    └── 提示"操作成功"，关闭对话框，刷新列表
```

### 6.2 套餐菜单配置流程

```
用户点击"新增"或"修改"套餐
    │
    ├── 1. 重置表单和菜单树状态
    ├── 2. 加载套餐详情（修改时）
    ├── 3. 加载菜单树（tenantPackageMenuTreeselect）
    │   ├── 新增时: packageId=0，返回完整菜单树
    │   └── 修改时: packageId=实际值，返回菜单树+已选keys
    ├── 4. 设置已选中的菜单节点
    └── 5. 打开对话框
            │
            └── 用户操作菜单树
                    │
                    ├── 勾选/取消勾选菜单节点
                    ├── 点击"展开/折叠"切换所有节点
                    ├── 点击"全选/全不选"一键选择
                    └── 切换"父子联动"开关
                            │
                            └── 点击"确定"
                                    │
                                    ├── 获取选中keys + 半选中keys
                                    ├── 合并为 menuIds
                                    ├── 调用 add/update API
                                    └── 刷新列表
```

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，租户管理模块前端页面定义 |
