# 菜单管理前端页面文档 (pages.md)

> magic-ruoyi 菜单管理模块的前端页面定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 页面清单

| 页面文件 | 路由路径 | 页面名称 | 功能说明 |
|----------|----------|----------|----------|
| `views/system/menu/index.vue` | `/system/menu` | 菜单管理 | 菜单树形列表、CRUD、级联删除 |

---

## 2. 菜单管理页面 (index.vue)

### 2.1 页面布局

```
+----------------------------------------------------------+
| 搜索区域（可折叠）                                          |
| [菜单名称] [状态] [搜索] [重置]            [显示/隐藏搜索]  |
+----------------------------------------------------------+
| 工具栏                                                    |
| [新增] [级联删除]                          [显示/隐藏搜索]  |
+----------------------------------------------------------+
| 树形数据表格（懒加载）                                       |
| 菜单名称 | 图标 | 排序 | 权限标识 | 组件路径 | 状态 | 创建时间 | 操作 |
| ▶ 系统管理 | system | 1 | | | 正常 | 2026-xx-xx | 修改 新增 删除 |
|   ▶ 用户管理 | user | 1 | system:user:list | system/user/index | 正常 | ... | 修改 新增 删除 |
|     用户查询 | # | 1 | system:user:query | | 正常 | ... | 修改 新增 删除 |
+----------------------------------------------------------+
```

### 2.2 搜索区域

| 字段 | 组件 | 说明 |
|------|------|------|
| 菜单名称 | el-input | 模糊搜索，支持回车触发 |
| 状态 | el-select | 下拉选择，选项来自字典 sys_normal_disable |

### 2.3 工具栏按钮

| 按钮 | 权限标识 | 操作 |
|------|----------|------|
| 新增 | system:menu:add | 打开新增菜单弹窗 |
| 级联删除 | system:menu:remove | 打开级联删除弹窗 |

### 2.4 数据表格

| 列名 | 字段 | 宽度 | 说明 |
|------|------|------|------|
| 菜单名称 | menuName | 160px | 超出显示省略号 |
| 图标 | icon | 100px | svg-icon 组件渲染 |
| 排序 | orderNum | 60px | 数字 |
| 权限标识 | perms | - | 超出显示省略号 |
| 组件路径 | component | - | 超出显示省略号 |
| 状态 | status | 80px | dict-tag 组件显示，引用 sys_normal_disable |
| 创建时间 | createTime | - | 直接显示 |
| 操作 | - | 180px | 固定右侧 |

**表格特性**:

| 特性 | 配置 | 说明 |
|------|------|------|
| 树形数据 | row-key="menuId" | 以 menuId 作为行唯一标识 |
| 子节点 | tree-props="{ children: 'children' }" | 子节点字段名 |
| 懒加载 | lazy + :load="getChildrenList" | 展开时按需加载子菜单 |
| 展开管理 | expand-change 事件 | 记录展开状态，收起时清除缓存 |

### 2.5 操作列按钮

| 按钮 | 权限标识 | 操作 |
|------|----------|------|
| 修改 | system:menu:edit | 打开修改弹窗，回显当前菜单数据 |
| 新增 | system:menu:add | 打开新增弹窗，自动将当前菜单设为上级 |
| 删除 | system:menu:remove | 删除确认并执行 |

### 2.6 新增/修改菜单弹窗

**弹窗标题**: "添加菜单" 或 "修改菜单"
**弹窗宽度**: 750px

**表单字段**:

| 字段 | 组件 | 必填 | 校验规则 | 显示条件 | 说明 |
|------|------|------|----------|----------|------|
| 上级菜单 | el-tree-select | 否 | - | 始终显示 | 树形选择器，数据来自菜单列表，可选"主类目"(parentId=0) |
| 菜单类型 | el-radio-group | 是 | - | 始终显示 | 选项：目录(M)、菜单(C)、按钮(F) |
| 菜单图标 | icon-select | 否 | - | 类型 != F | 图标选择器组件 |
| 菜单名称 | el-input | 是 | 不能为空 | 始终显示 | - |
| 显示排序 | el-input-number | 是 | 不能为空 | 始终显示 | 最小值0，右侧控制按钮 |
| 是否外链 | el-radio-group | 否 | - | 类型 != F | 选项：是(0)、否(1)，带tooltip |
| 路由地址 | el-input | 是 | 不能为空 | 类型 != F | 带tooltip提示 |
| 组件路径 | el-input | 是 | - | 类型 = C | 带tooltip，默认在views目录下 |
| 权限标识 | el-input | 否 | - | 类型 != M | 最大长度100，带tooltip |
| 路由参数 | el-input | 否 | - | 类型 = C | 最大长度255，JSON格式 |
| 是否缓存 | el-radio-group | 否 | - | 类型 = C | 选项：缓存(0)、不缓存(1) |
| 显示状态 | el-radio-group | 否 | - | 类型 != F | 选项来自字典 sys_show_hide |
| 菜单状态 | el-radio-group | 否 | - | 始终显示 | 选项来自字典 sys_normal_disable |
| 激活路由 | el-input | 否 | - | 显示状态 != 隐藏 | 隐藏菜单的默认激活路由，带tooltip |

**表单初始化值**:

```typescript
const initFormData = {
  path: '',
  menuId: undefined,
  parentId: 0,
  menuName: '',
  icon: '',
  menuType: MenuTypeEnum.M,  // 默认目录
  orderNum: 1,
  isFrame: '1',              // 默认非外链
  isCache: '0',              // 默认缓存
  visible: '0',              // 默认显示
  status: '0'                // 默认正常
};
```

**表单校验规则**:

```typescript
rules: {
  menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  orderNum: [{ required: true, message: '菜单顺序不能为空', trigger: 'blur' }],
  path: [{ required: true, message: '路由地址不能为空', trigger: 'blur' }]
}
```

### 2.7 级联删除弹窗

**弹窗标题**: "级联删除菜单"
**弹窗宽度**: 750px

**内容**:

| 组件 | 配置 | 说明 |
|------|------|------|
| el-tree | node-key="menuId" | 菜单树，支持多选 |
| | show-checkbox | 显示复选框 |
| | check-strictly=false | 父子联动 |
| | default-expanded-keys=[0] | 默认展开主类目 |
| | props: { value: 'menuId', label: 'menuName', children: 'children' } | 节点属性映射 |

**交互流程**:

1. 点击"级联删除"按钮
2. 加载完整菜单树数据
3. 用户在树中勾选要删除的菜单节点
4. 点击"确定"，调用 cascadeDelMenu 接口
5. 删除成功后刷新列表，关闭弹窗

### 2.8 懒加载机制

**子菜单加载**:

```typescript
const getChildrenList = async (row, treeNode, resolve) => {
  // 缓存子菜单数据
  menuExpandMap.value[row.menuId] = { row, treeNode, resolve };
  const children = menuChildrenListMap.value[row.menuId] || [];
  resolve(children);
};
```

**展开状态管理**:

| 变量 | 用途 |
|------|------|
| menuExpandMap | 记录已展开的菜单ID及其加载回调 |
| menuChildrenListMap | 缓存每个父菜单的子节点列表 |

**刷新机制**:

- 新增/修改/删除菜单后，自动刷新已展开的父菜单数据
- 同时刷新祖父级菜单数据，确保层级数据一致性

### 2.9 查询参数

```typescript
queryParams: {
  menuName: undefined,
  status: undefined
}
```

### 2.10 状态管理

| 变量 | 类型 | 说明 |
|------|------|------|
| menuList | ref<MenuVO[]> | 顶层菜单列表 |
| menuChildrenListMap | ref | 父菜单到子菜单的映射 |
| menuExpandMap | ref | 已展开菜单的加载回调映射 |
| loading | ref<boolean> | 表格加载状态 |
| showSearch | ref<boolean> | 搜索区域显示状态 |
| menuOptions | ref | 菜单下拉树数据 |
| dialog | reactive | 新增/修改弹窗状态 |
| deleteDialog | reactive | 级联删除弹窗状态 |
| deleteLoading | ref<boolean> | 级联删除加载状态 |

---

## 3. 路由配置

| 路由路径 | 组件 | 说明 |
|----------|------|------|
| `/system/menu` | system/menu/index | 菜单管理主页面 |

---

## 4. 字典引用

| 字典类型 | 用途 |
|----------|------|
| sys_normal_disable | 状态显示（正常/停用） |
| sys_show_hide | 显示状态（显示/隐藏） |

---

## 5. 组件依赖

| 组件 | 用途 |
|------|------|
| icon-select | 图标选择器，用于选择菜单图标 |
| svg-icon | SVG图标渲染，显示菜单图标 |
| dict-tag | 字典标签，显示状态等信息 |
| right-toolbar | 右侧工具栏，控制搜索区域显示 |

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，菜单管理前端页面定义 |
