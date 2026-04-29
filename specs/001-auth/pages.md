# 认证模块前端页面文档（001-auth/pages.md）

> magic-ruoyi 认证模块前端页面定义。描述登录页、注册页的组件结构、交互逻辑与数据流。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 页面概览

| 页面 | 文件路径 | 路由 | 描述 |
|------|----------|------|------|
| 登录页 | `src/views/login.vue` | `/login` | 用户登录入口，支持多认证方式 |
| 注册页 | `src/views/register.vue` | `/register` | 新用户注册入口 |

**公共依赖**:
- `@/api/login` - 认证 API 封装
- `@/api/system/social/auth` - 社交登录 API
- `@/store/modules/user` - 用户状态管理
- `@/api/types` - TypeScript 类型定义
- `lang-select` - 语言切换组件
- `svg-icon` - SVG 图标组件

---

## 2. 登录页面（login.vue）

### 2.1 页面布局

```
┌─────────────────────────────────────┐
│           背景图片（全屏）             │
│  ┌───────────────────────────────┐  │
│  │  [标题]              [语言切换] │  │
│  │                               │  │
│  │  [租户下拉选择] (可选)          │  │
│  │  [用户名输入框]                │  │
│  │  [密码输入框]                  │  │
│  │  [验证码输入框] [验证码图片]    │  │
│  │  [ ] 记住密码                   │  │
│  │  [社交按钮行]  [登录按钮]       │  │
│  │                    [注册链接]   │  │
│  └───────────────────────────────┘  │
│                                     │
│  Copyright © 2018-2026 ...          │
└─────────────────────────────────────┘
```

### 2.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 标题 | `<h3>` | 显示 `VITE_APP_TITLE` 环境变量值 |
| 语言切换 | `<lang-select>` | 右上角国际化切换按钮 |
| 租户选择 | `<el-select>` | 可搜索下拉框，`tenantEnabled` 控制显隐 |
| 用户名 | `<el-input>` | 带用户图标前缀 |
| 密码 | `<el-input type="password">` | 带密码图标前缀，回车触发登录 |
| 验证码 | `<el-input>` + `<img>` | 输入框占 63% 宽度，图片占 33%，点击刷新 |
| 记住密码 | `<el-checkbox>` | 勾选后将凭证存入 localStorage |
| 社交按钮 | `<el-button circle>` × 5 | WeChat、MaxKey、TopIAM、Gitee、GitHub |
| 登录按钮 | `<el-button type="primary">` | 全宽，loading 状态 |
| 注册链接 | `<router-link>` | `register` 开关控制显隐 |

### 2.3 表单数据模型

```typescript
interface LoginData {
  tenantId: string;      // 租户 ID，默认 '000000'
  username: string;      // 用户名，默认 'admin'
  password: string;      // 密码，默认 'admin123'
  rememberMe: boolean;   // 记住密码，默认 false
  code: string;          // 验证码答案
  uuid: string;          // 验证码 UUID
  clientId?: string;     // 客户端 ID（由 login() 自动注入）
  grantType?: string;    // 授权类型（由 login() 自动注入为 'password'）
}
```

### 2.4 表单校验规则

| 字段 | 规则 | 触发时机 | 错误提示 |
|------|------|----------|----------|
| tenantId | 必填 | blur | login.rule.tenantId.required |
| username | 必填 | blur | login.rule.username.required |
| password | 必填 | blur | login.rule.password.required |
| code | 必填 | change | login.rule.code.required |

### 2.5 页面生命周期

```
onMounted()
  ├── getCode()          → 获取图形验证码
  ├── initTenantList()   → 获取租户列表
  └── getLoginData()     → 从 localStorage 恢复记住的凭证
```

### 2.6 核心交互逻辑

#### 2.6.1 登录流程

```
handleLogin()
  ├── 表单校验
  ├── 若 rememberMe=true → 存入 localStorage
  ├── 若 rememberMe=false → 清除 localStorage
  ├── 调用 userStore.login(loginForm)
  │   └── 内部调用 login() API → POST /auth/login
  ├── 成功 → 跳转到 redirect 页面或首页
  └── 失败 → 重新获取验证码（若启用）
```

#### 2.6.2 验证码刷新

```
getCode()
  ├── 调用 getCodeImg() API → GET /auth/code
  ├── 更新 captchaEnabled 状态
  ├── 若启用 → 清空 code 输入框
  ├── 设置 codeUrl = 'data:image/gif;base64,' + data.img
  └── 设置 uuid = data.uuid
```

#### 2.6.3 租户列表初始化

```
initTenantList()
  ├── 调用 getTenantList(false) API → GET /auth/tenant/list
  ├── 更新 tenantEnabled 状态
  ├── 若启用 → 设置 tenantList = data.voList
  └── 默认选中第一个租户
```

#### 2.6.4 社交登录

```
doSocialLogin(type: string)
  ├── 调用 authRouterUrl(type, tenantId) → GET /auth/binding/{source}
  ├── 成功 → window.location.href = res.data（跳转第三方授权页）
  └── 失败 → ElMessage.error(res.msg)
```

#### 2.6.5 记住密码

```
getLoginData()
  ├── 读取 localStorage: tenantId, username, password, rememberMe
  └── 填充到 loginForm

handleLogin() 中:
  ├── rememberMe=true  → localStorage.setItem(...)
  └── rememberMe=false → localStorage.removeItem(...)
```

### 2.7 路由监听

页面监听路由变化，提取 `query.redirect` 参数作为登录成功后的跳转目标:

```typescript
watch(
  () => router.currentRoute.value,
  (newRoute) => {
    redirect.value = newRoute.query?.redirect
      ? decodeURIComponent(newRoute.query.redirect)
      : '/';
  },
  { immediate: true }
);
```

### 2.8 样式规范

| 属性 | 值 |
|------|-----|
| 表单容器宽度 | 400px |
| 表单内边距 | 25px 25px 5px 25px |
| 输入框高度 | 40px |
| 表单背景 | #ffffff |
| 表单圆角 | 6px |
| 背景图 | `../assets/images/login-background.jpg`（cover 模式） |
| 验证码图片高度 | 40px |
| 底部栏高度 | 40px |
| 底部栏位置 | fixed bottom |

---

## 3. 注册页面（register.vue）

### 3.1 页面布局

```
┌─────────────────────────────────────┐
│           背景图片（全屏）             │
│  ┌───────────────────────────────┐  │
│  │  [标题]              [语言切换] │  │
│  │                               │  │
│  │  [租户下拉选择] (可选)          │  │
│  │  [用户名输入框]                │  │
│  │  [密码输入框]                  │  │
│  │  [确认密码输入框]               │  │
│  │  [验证码输入框] [验证码图片]    │  │
│  │  [注册按钮]                     │  │
│  │                    [登录链接]   │  │
│  └───────────────────────────────┘  │
│                                     │
│  Copyright © 2018-2026 ...          │
└─────────────────────────────────────┘
```

### 3.2 组件结构

| 元素 | 类型 | 说明 |
|------|------|------|
| 标题 | `<h3>` | 显示 `VITE_APP_TITLE` |
| 语言切换 | `<lang-select>` | 国际化切换 |
| 租户选择 | `<el-select>` | 可搜索下拉框，`tenantEnabled` 控制显隐 |
| 用户名 | `<el-input>` | 带用户图标前缀 |
| 密码 | `<el-input type="password">` | 带密码图标前缀，回车触发注册 |
| 确认密码 | `<el-input type="password">` | 带密码图标前缀，回车触发注册 |
| 验证码 | `<el-input>` + `<img>` | 输入框占 63% 宽度，图片占 33%，点击刷新 |
| 注册按钮 | `<el-button type="primary">` | 全宽，loading 状态 |
| 登录链接 | `<router-link>` | 始终显示，跳转至 `/login` |

### 3.3 表单数据模型

```typescript
interface RegisterForm {
  tenantId: string;         // 租户 ID
  username: string;         // 用户名
  password: string;         // 密码
  confirmPassword?: string; // 确认密码
  code?: string;            // 验证码答案
  uuid?: string;            // 验证码 UUID
  userType?: string;        // 用户类型，默认 'sys_user'
}
```

### 3.4 表单校验规则

| 字段 | 规则 | 触发时机 | 错误提示 |
|------|------|----------|----------|
| tenantId | 必填 | blur | register.rule.tenantId.required |
| username | 必填，2-20 字符 | blur | register.rule.username.required / length |
| password | 必填，5-20 字符，禁止 `< > " ' \ \|` | blur | register.rule.password.required / length / pattern |
| confirmPassword | 必填，须与 password 一致 | blur | register.rule.confirmPassword.required / equalToPassword |
| code | 必填 | change | register.rule.code.required |

**自定义校验器 - 密码一致性**:

```typescript
const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error(t('register.rule.confirmPassword.equalToPassword')));
  } else {
    callback();
  }
};
```

### 3.5 页面生命周期

```
onMounted()
  ├── getCode()          → 获取图形验证码
  └── initTenantList()   → 获取租户列表
```

### 3.6 核心交互逻辑

#### 3.6.1 注册流程

```
handleRegister()
  ├── 表单校验
  ├── 调用 register(registerForm) API → POST /auth/register
  ├── 成功 → ElMessageBox.alert 提示注册成功
  │        → 跳转到 /login
  └── 失败 → 重新获取验证码（若启用）
```

#### 3.6.2 验证码刷新

与登录页相同逻辑:

```
getCode()
  ├── 调用 getCodeImg() API
  ├── 更新 captchaEnabled 状态
  ├── 设置 codeUrl 和 uuid
  └── 不清空输入框（与登录页不同）
```

#### 3.6.3 租户列表初始化

与登录页相同逻辑。

### 3.7 样式规范

| 属性 | 值 |
|------|-----|
| 表单容器宽度 | 400px |
| 表单内边距 | 25px 25px 5px 25px |
| 输入框高度 | 40px |
| 表单背景 | #ffffff |
| 表单圆角 | 6px |
| 背景图 | `../assets/images/login-background.jpg`（与登录页共用） |
| 验证码图片高度 | 40px |
| 底部栏高度 | 40px |
| 底部栏位置 | fixed bottom |

---

## 4. 数据流图

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  login.vue   │     │ register.vue │     │  userStore   │
│              │     │              │     │              │
│  loginForm ──┼────>│              │     │              │
│  tenantList ─┼──┐  │  registerForm┼──┐  │  login()     │
│  codeUrl ────┼──┤  │  tenantList ─┼──┤  │  getInfo()   │
│              │  │  │  codeUrl ────┼──┤  │              │
└──────┬───────┘  │  └──────┬───────┘  │  └──────┬───────┘
       │          │         │          │         │
       ▼          │         ▼          │         ▼
┌──────────────┐  │  ┌──────────────┐  │  ┌──────────────┐
│  @/api/login │  │  │  @/api/login │  │  │  Pinia Store │
│              │  │  │              │  │  │              │
│  login()     │◄─┘  │  register()  │◄─┘  │  setUser()   │
│  register()  │     │  getCodeImg()│     │  setToken()  │
│  logout()    │     │  getTenant() │     │              │
│  getCodeImg()│     │              │     └──────────────┘
│  getTenant() │     └──────────────┘
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Axios       │
│  Request     │
│  Interceptor │
│              │
│  → Token 注入 │
│  → RSA 加密  │
│  → 错误处理  │
└──────────────┘
```

---

## 5. 国际化（i18n）

### 5.1 登录页使用的 i18n Key

| Key | 用途 |
|-----|------|
| `login.selectPlaceholder` | 租户选择占位符 |
| `login.username` | 用户名输入框占位符 |
| `login.password` | 密码输入框占位符 |
| `login.code` | 验证码输入框占位符 |
| `login.rememberPassword` | 记住密码复选框文本 |
| `login.login` | 登录按钮文本 |
| `login.logging` | 登录中按钮文本 |
| `login.switchRegisterPage` | 注册链接文本 |
| `login.social.wechat` | 微信按钮提示 |
| `login.social.maxkey` | MaxKey 按钮提示 |
| `login.social.topiam` | TopIAM 按钮提示 |
| `login.social.gitee` | Gitee 按钮提示 |
| `login.social.github` | GitHub 按钮提示 |
| `login.rule.tenantId.required` | 租户必填提示 |
| `login.rule.username.required` | 用户名必填提示 |
| `login.rule.password.required` | 密码必填提示 |
| `login.rule.code.required` | 验证码必填提示 |

### 5.2 注册页使用的 i18n Key

| Key | 用途 |
|-----|------|
| `register.selectPlaceholder` | 租户选择占位符 |
| `register.username` | 用户名输入框占位符 |
| `register.password` | 密码输入框占位符 |
| `register.confirmPassword` | 确认密码输入框占位符 |
| `register.code` | 验证码输入框占位符 |
| `register.register` | 注册按钮文本 |
| `register.registering` | 注册中按钮文本 |
| `register.switchLoginPage` | 登录链接文本 |
| `register.registerSuccess` | 注册成功提示（含用户名变量） |
| `register.rule.tenantId.required` | 租户必填提示 |
| `register.rule.username.required` | 用户名必填提示 |
| `register.rule.username.length` | 用户名长度提示 |
| `register.rule.password.required` | 密码必填提示 |
| `register.rule.password.length` | 密码长度提示 |
| `register.rule.password.pattern` | 密码禁止字符提示 |
| `register.rule.confirmPassword.required` | 确认密码必填提示 |
| `register.rule.confirmPassword.equalToPassword` | 密码不一致提示 |
| `register.rule.code.required` | 验证码必填提示 |

---

## 6. 前端 API 模块

### 6.1 `@/api/login.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `login(data)` | POST | `/auth/login` | 用户登录，自动注入 clientId 和 grantType |
| `register(data)` | POST | `/auth/register` | 用户注册，自动注入 clientId |
| `logout()` | POST | `/auth/logout` | 退出登录，可选关闭 SSE |
| `getCodeImg()` | GET | `/auth/code` | 获取图形验证码 |
| `callback(data)` | POST | `/auth/social/callback` | 社交登录回调 |
| `getInfo()` | GET | `/system/user/getInfo` | 获取用户详细信息 |
| `getTenantList(isToken)` | GET | `/auth/tenant/list` | 获取租户列表 |

### 6.2 `@/api/system/social/auth.ts`

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `authRouterUrl(source, tenantId)` | GET | `/auth/binding/{source}` | 获取第三方授权跳转 URL |
| `authUnlock(authId)` | DELETE | `/auth/unlock/{authId}` | 取消社交授权 |
| `getAuthList()` | GET | `/system/social/list` | 获取已绑定社交账号列表 |

### 6.3 `@/api/types.ts`

| 类型 | 说明 |
|------|------|
| `RegisterForm` | 注册表单数据类型 |
| `LoginData` | 登录请求数据类型 |
| `LoginResult` | 登录响应类型 |
| `VerifyCodeResult` | 验证码响应类型 |
| `TenantVO` | 租户值对象类型 |
| `TenantInfo` | 租户列表响应类型 |

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
