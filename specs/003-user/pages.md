# 003-User 用户管理模块 - 前端页面规格

> 本文档定义用户管理模块的全部前端页面结构、组件和交互。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 页面总览

| 页面 | 文件路径 | 路由 | 说明 |
|------|----------|------|------|
| 用户管理 | `views/system/user/index.vue` | `/system/user` | 用户列表、增删改查、导入导出 |
| 分配角色 | `views/system/user/authRole.vue` | `/system/user-auth/role/:userId` | 为用户分配角色 |
| 个人中心 | `views/system/user/profile/index.vue` | `/system/user/profile` | 个人信息概览 |
| 基本信息 | `views/system/user/profile/userInfo.vue` | 嵌入个人中心 | 修改基本资料 |
| 修改密码 | `views/system/user/profile/resetPwd.vue` | 嵌入个人中心 | 修改登录密码 |
| 头像修改 | `views/system/user/profile/userAvatar.vue` | 嵌入个人中心 | 裁剪上传头像 |
| 第三方应用 | `views/system/user/profile/thirdParty.vue` | 嵌入个人中心 | 第三方账号管理 |
| 在线设备 | `views/system/user/profile/onlineDevice.vue` | 嵌入个人中心 | 在线设备列表 |

---

## 2. 用户管理页面 (index.vue)

### 2.1 页面布局

```
+----------------------------------------------------------+
|  [部门树 - 左侧 4/24]  |  [搜索区 + 表格 - 右侧 20/24]    |
|                        |                                  |
|  +------------------+  |  +----------------------------+  |
|  | 搜索框           |  |  | 搜索表单（可折叠）          |  |
|  | [部门树组件]     |  |  | 用户名称 | 昵称 | 手机 | 状态 |  |
|  |                  |  |  | 创建时间范围               |  |
|  |                  |  |  | [搜索] [重置]              |  |
|  |                  |  |  +----------------------------+  |
|  |                  |  |                                  |
|  |                  |  |  +----------------------------+  |
|  |                  |  |  | 工具栏                      |  |
|  |                  |  |  | [新增] [修改] [删除] [更多] |  |
|  |                  |  |  +----------------------------+  |
|  |                  |  |                                  |
|  |                  |  |  +----------------------------+  |
|  |                  |  |  | 数据表格                    |  |
|  |                  |  |  | [选择] | 编号 | 名称 | ...  |  |
|  |                  |  |  +----------------------------+  |
|  |                  |  |                                  |
|  |                  |  |  [分页组件]                     |
|  +------------------+  +----------------------------------+
+----------------------------------------------------------+
```

### 2.2 部门树区域

- **组件**: `el-tree`
- **数据源**: `GET /system/user/deptTree`
- **功能**:
  - 顶部搜索框支持按部门名称过滤
  - 默认全部展开（`default-expand-all`）
  - 高亮当前选中节点（`highlight-current`）
  - 点击节点触发 `handleNodeClick`，将 `deptId` 加入查询条件
- **布局**: `el-col :lg="4" :xs="24"`，响应式小屏占满宽度

### 2.3 搜索表单

| 字段 | 组件 | 绑定 | 说明 |
|------|------|------|------|
| 用户名称 | `el-input` | `queryParams.userName` | 支持回车搜索 |
| 用户昵称 | `el-input` | `queryParams.nickName` | 支持回车搜索 |
| 手机号码 | `el-input` | `queryParams.phonenumber` | 支持回车搜索 |
| 状态 | `el-select` | `queryParams.status` | 字典 `sys_normal_disable` |
| 创建时间 | `el-date-picker` | `dateRange` | daterange 类型 |

- 可折叠显示/隐藏（`showSearch` 控制）
- 右侧工具栏提供搜索切换和列显隐配置

### 2.4 工具栏

| 按钮 | 权限 | 说明 |
|------|------|------|
| 新增 | `system:user:add` | 打开新增对话框 |
| 修改 | `system:user:edit` | 打开编辑对话框，未选中时禁用 |
| 删除 | `system:user:remove` | 批量删除，未选中时禁用 |
| 更多 > 下载模板 | 无 | 下载 Excel 导入模板 |
| 更多 > 导入数据 | `system:user:import` | 打开导入对话框 |
| 更多 > 导出数据 | `system:user:export` | 导出当前查询结果 |

### 2.5 数据表格

| 列 | 字段 | 默认显示 | 可隐藏 | 说明 |
|----|------|----------|--------|------|
| 选择框 | - | 是 | 否 | 多选 |
| 用户编号 | `userId` | 否 | 是 | - |
| 用户名称 | `userName` | 是 | 是 | 超长省略 |
| 用户昵称 | `nickName` | 是 | 是 | 超长省略 |
| 部门 | `deptName` | 是 | 是 | 超长省略 |
| 手机号码 | `phonenumber` | 是 | 是 | 固定宽度 120 |
| 状态 | `status` | 是 | 是 | 开关组件 |
| 创建时间 | `createTime` | 是 | 是 | 固定宽度 160 |
| 操作 | - | 是 | 否 | 固定右侧，宽度 180 |

**操作列按钮** (userId !== 1 时显示):

| 按钮 | 权限 | 说明 |
|------|------|------|
| 修改 | `system:user:edit` | 打开编辑对话框 |
| 删除 | `system:user:remove` | 确认删除 |
| 重置密码 | `system:user:resetPwd` | 弹窗输入新密码 |
| 分配角色 | `system:user:edit` | 跳转到角色分配页 |

### 2.6 用户表单对话框

- **宽度**: 600px
- **表单布局**: 两列（`el-col :span="12"`）

| 字段 | 组件 | 必填 | 校验规则 | 显示条件 |
|------|------|------|----------|----------|
| 用户昵称 | `el-input` | 是 | 非空 | 始终 |
| 归属部门 | `el-tree-select` | 否 | - | 非自己编辑时 |
| 手机号码 | `el-input` | 否 | 手机号格式 | 始终 |
| 邮箱 | `el-input` | 否 | 邮箱格式 | 始终 |
| 用户名称 | `el-input` | 是 | 2-20字符 | 仅新增时 |
| 用户密码 | `el-input` (password) | 是 | 5-20字符，不含非法字符 | 仅新增时 |
| 用户性别 | `el-select` | 否 | - | 始终 |
| 状态 | `el-radio-group` | 是 | - | 始终 |
| 岗位 | `el-select` (multiple) | 否 | - | 非自己编辑时 |
| 角色 | `el-select` (multiple, filterable) | 是 | 非空 | 非自己编辑时 |
| 备注 | `el-input` (textarea) | 否 | - | 始终 |

**部门联动**: 选择部门后自动加载该部门下的岗位列表。

**自我编辑限制**: 编辑自己时，`roleIds`、`deptId`、`postIds` 提交前设为 null。

### 2.7 导入对话框

- **宽度**: 400px
- **组件**: `el-upload` (drag 模式)
- **功能**:
  - 仅接受 `.xlsx`、`.xls` 文件
  - 拖拽上传
  - 复选框控制是否更新已存在数据（`updateSupport`）
  - 上传地址: `/system/user/importData?updateSupport={updateSupport}`
  - 上传成功后弹窗显示导入结果详情

### 2.8 状态切换

- 通过 `el-switch` 组件在表格中直接切换
- 切换前弹出确认对话框
- 接口失败时自动回滚开关状态

### 2.9 重置密码

- 通过 `ElMessageBox.prompt` 弹窗输入新密码
- 密码校验: 5-20 字符，不含 `< > " ' \|`
- 密码通过 RSA 加密传输
- 成功后显示新密码

---

## 3. 分配角色页面 (authRole.vue)

### 3.1 页面布局

```
+----------------------------------------------------------+
|  基本信息                                                 |
|  用户昵称: [____]  登录账号: [____]                       |
+----------------------------------------------------------+
|  角色信息                                                 |
|  +----------------------------------------------------+  |
|  | [选择] | 序号 | 角色编号 | 角色名称 | 权限字符 | 时间 |  |
|  |  [x]   |  1   |    1     | 超级管理员 | admin  | ... |  |
|  |  [ ]   |  2   |    4     | 普通角色   | common | ... |  |
|  +----------------------------------------------------+  |
|  [分页组件]                                              |
|                                                          |
|                    [提交] [返回]                          |
+----------------------------------------------------------+
```

### 3.2 基本信息区

- 只读展示用户昵称和登录账号
- 数据来源: `GET /system/user/authRole/{userId}`

### 3.3 角色表格

| 列 | 字段 | 说明 |
|----|------|------|
| 序号 | - | 分页计算 `(pageNum-1)*pageSize + index + 1` |
| 选择框 | - | 多选，保留跨页选中状态 |
| 角色编号 | `roleId` | - |
| 角色名称 | `roleName` | - |
| 权限字符 | `roleKey` | - |
| 创建时间 | `createTime` | 格式化显示 |

**交互**:
- 点击行可切换选中状态（仅正常状态的角色）
- 停用角色（`status='1'`）不可勾选
- 初始化时自动勾选用户已有角色（`flag=true`）

### 3.4 提交

- 将选中的角色 ID 用逗号拼接，通过 `PUT /system/user/authRole` 提交
- 成功后提示"授权成功"并返回用户管理页

---

## 4. 个人中心页面 (profile/index.vue)

### 4.1 页面布局

```
+-------------------+  +-------------------------------------+
|  个人信息卡片      |  |  基本资料                           |
|                   |  |                                     |
|  [头像组件]        |  |  [Tab: 基本资料] [修改密码]          |
|                   |  |  [第三方应用] [在线设备]              |
|  用户名称: admin   |  |                                     |
|  手机号码: ...     |  |  +-------------------------------+  |
|  用户邮箱: ...     |  |  | Tab 内容区                      |  |
|  所属部门: ...     |  |  |                                 |  |
|  所属角色: ...     |  |  |                                 |  |
|  创建日期: ...     |  |  +-------------------------------+  |
+-------------------+  +-------------------------------------+
```

### 4.2 左侧个人信息卡片

- **头像**: 使用 `userAvatar.vue` 组件，点击可裁剪上传
- **信息列表**:
  - 用户名称: `state.user.userName`
  - 手机号码: `state.user.phonenumber`
  - 用户邮箱: `state.user.email`
  - 所属部门: `state.user.deptName` / `state.postGroup`
  - 所属角色: `state.roleGroup`
  - 创建日期: `state.user.createTime`

### 4.3 右侧 Tab 页签

| Tab | 组件 | 说明 |
|-----|------|------|
| 基本资料 | `userInfo.vue` | 修改昵称、手机号、邮箱、性别 |
| 修改密码 | `resetPwd.vue` | 修改登录密码 |
| 第三方应用 | `thirdParty.vue` | 查看已绑定的社交账号 |
| 在线设备 | `onlineDevice.vue` | 查看在线设备列表 |

---

## 5. 基本信息组件 (profile/userInfo.vue)

### 5.1 表单字段

| 字段 | 组件 | 必填 | 校验规则 |
|------|------|------|----------|
| 用户昵称 | `el-input` | 是 | 非空 |
| 手机号码 | `el-input` | 是 | 非空 + 手机号格式 |
| 邮箱 | `el-input` | 是 | 非空 + 邮箱格式 |
| 性别 | `el-radio-group` | 否 | - |

### 5.2 提交

- 调用 `PUT /system/user/profile`
- 成功后提示"修改成功"

---

## 6. 修改密码组件 (profile/resetPwd.vue)

### 6.1 表单字段

| 字段 | 组件 | 必填 | 校验规则 |
|------|------|------|----------|
| 旧密码 | `el-input` (password) | 是 | 非空 |
| 新密码 | `el-input` (password) | 是 | 非空 + 6-20字符 + 不含非法字符 |
| 确认密码 | `el-input` (password) | 是 | 非空 + 与新密码一致 |

### 6.2 密码一致性校验

```typescript
const equalToPassword = (rule, value, callback) => {
  if (user.value.newPassword !== value) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};
```

### 6.3 提交

- 调用 `PUT /system/user/profile/updatePwd`
- 请求头: `isEncrypt: true`, `repeatSubmit: false`
- 成功后提示"修改成功"

---

## 7. 头像修改组件 (profile/userAvatar.vue)

### 7.1 触发方式

- 点击头像区域打开裁剪对话框
- hover 时显示 "+" 遮罩提示

### 7.2 裁剪对话框

- **宽度**: 800px
- **布局**: 左右两栏

| 区域 | 内容 |
|------|------|
| 左侧 | `vue-cropper` 裁剪组件，350px 高度 |
| 右侧 | 实时预览区域，350px 高度 |

### 7.3 操作按钮

| 按钮 | 功能 |
|------|------|
| 选择 | 选择图片文件上传 |
| + | 放大裁剪区域 |
| - | 缩小裁剪区域 |
| 左转 | 逆时针旋转 90 度 |
| 右转 | 顺时针旋转 90 度 |
| 提交 | 裁剪并上传 |

### 7.4 上传流程

1. 选择图片文件，校验格式（必须为 image/*）
2. 读取文件为 DataURL，显示在裁剪区
3. 调整裁剪区域后点击提交
4. 获取裁剪后的 Blob，构造 FormData
5. 调用 `POST /system/user/profile/avatar`
6. 更新头像并同步到 UserStore

### 7.5 裁剪配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| autoCrop | true | 默认生成裁剪框 |
| autoCropWidth | 200 | 裁剪框宽度 |
| autoCropHeight | 200 | 裁剪框高度 |
| fixedBox | true | 固定裁剪框大小 |
| outputType | png | 输出格式 |

---

## 8. 第三方应用组件 (profile/thirdParty.vue)

- 展示已绑定的第三方社交账号列表
- 数据来源: `GET /system/social/auth/list`
- 显示平台名称、绑定状态

---

## 9. 在线设备组件 (profile/onlineDevice.vue)

- 展示当前账号的在线设备列表
- 数据来源: `GET /monitor/online/list`
- 显示设备信息（IP、浏览器、操作系统、登录时间等）

---

## 10. 公共组件依赖

| 组件 | 来源 | 用途 |
|------|------|------|
| `Pagination` | `@/components/Pagination` | 分页 |
| `RightToolbar` | `@/components/RightToolbar` | 右侧工具栏（搜索切换、列显隐） |
| `SvgIcon` | `@/components/SvgIcon` | 图标 |
| `vue-cropper` | 第三方库 | 图片裁剪 |

---

## 11. 字典依赖

| 字典 | 用途 | 页面 |
|------|------|------|
| `sys_normal_disable` | 用户状态选项 | index.vue |
| `sys_user_sex` | 性别选项 | index.vue, userInfo.vue |

---

## 12. 路由配置

| 路由路径 | 组件 | 菜单ID | 说明 |
|----------|------|--------|------|
| `/system/user` | `system/user/index` | 100 | 用户管理 |
| `/system/user-auth/role/:userId` | `system/user/authRole` | 131 | 分配角色（隐藏菜单） |
| `/system/user/profile` | `system/user/profile/index` | - | 个人中心 |
