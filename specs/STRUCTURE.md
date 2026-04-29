# magic-ruoyi 项目目录结构

## 项目总览

```
magic-ruoyi/
├── pom.xml                          # 根 POM，Maven 多模块管理
├── magic-ruoyi-admin/               # 后端服务模块（Spring Boot）
├── magic-ruoyi-web/                 # 前端工程（Vue 3 + Vite）
├── sql/                             # 数据库脚本
│   └── magic-ruoyi.sql              # 初始化 SQL
├── specs-plan-a/                    # 需求分析文档
├── specs-plan-b/                    # 设计文档
├── specs-plan-c/                    # 实现计划
└── specs/                    # 项目结构文档（本文件所在目录）
```

---

## 后端工程结构（magic-ruoyi-admin）

```
magic-ruoyi-admin/
├── pom.xml                          # 模块 POM，继承根 POM
├── deps/                            # 本地依赖库
├── logs/                            # 运行时日志输出目录
├── src/
│   ├── main/
│   │   ├── java/org/fellow99/magic/ruoyi/
│   │   │   ├── MagicRuoyiApplication.java        # Spring Boot 启动类
│   │   │   ├── MagicRuoyiServletInitializer.java # Servlet 初始化器
│   │   │   ├── controller/                       # 控制器层
│   │   │   │   ├── AuthController.java           # 认证接口（登录/登出/刷新token）
│   │   │   │   ├── CaptchaController.java        # 验证码接口
│   │   │   │   └── IndexController.java          # 首页/入口控制器
│   │   │   ├── domain/                           # 领域模型
│   │   │   │   └── vo/                           # 视图对象（View Objects）
│   │   │   ├── service/                          # 业务逻辑层
│   │   │   │   ├── IAuthStrategy.java            # 认证策略接口
│   │   │   │   ├── SysLoginService.java          # 登录业务
│   │   │   │   ├── SysRegisterService.java       # 注册业务
│   │   │   │   └── impl/                         # 策略实现类
│   │   │   └── listener/                         # 事件监听器
│   │   │       └── UserActionListener.java       # 用户行为监听（Sa-Token）
│   │   └── resources/
│   │       ├── application.yml                   # 主配置文件
│   │       ├── application-dev.yml               # 开发环境配置
│   │       ├── application-local.yml             # 本地环境配置
│   │       ├── application-prod.yml              # 生产环境配置
│   │       ├── banner.txt                        # 启动 Banner
│   │       ├── ip2region.xdb                     # IP 归属地数据库
│   │       ├── logback-plus.xml                  # Logback 日志配置
│   │       └── i18n/                             # 国际化资源
│   │           └── messages*.properties          # 多语言消息文件
│   └── test/                                     # 单元测试
└── target/                                       # 编译输出
```

---

## 前端工程结构（magic-ruoyi-web）

```
magic-ruoyi-web/
├── package.json                     # 项目依赖与脚本
├── vite.config.ts                   # Vite 构建配置
├── tsconfig.json                    # TypeScript 配置
├── uno.config.ts                    # UnoCSS 配置
├── eslint.config.ts                 # ESLint 配置
├── .env.development                 # 开发环境变量
├── .env.production                  # 生产环境变量
├── index.html                       # 入口 HTML
├── bin/                             # 构建脚本
├── html/                            # 静态 HTML 页面
├── public/                          # 公共静态资源
├── vite/                            # Vite 插件配置
└── src/
    ├── main.ts                      # 应用入口
    ├── App.vue                      # 根组件
    ├── animate.ts                   # 动画配置
    ├── settings.ts                  # 全局设置
    ├── permission.ts                # 路由权限守卫
    ├── api/                         # API 接口定义
    │   ├── login.ts                 # 登录接口
    │   ├── menu.ts                  # 菜单接口
    │   ├── types.ts                 # API 类型定义
    │   ├── system/                  # 系统管理接口
    │   ├── monitor/                 # 监控模块接口
    │   ├── tool/                    # 工具模块接口
    │   ├── workflow/                # 工作流接口
    │   └── demo/                    # 示例接口
    ├── views/                       # 页面视图
    │   ├── index.vue                # 首页
    │   ├── login.vue                # 登录页
    │   ├── register.vue             # 注册页
    │   ├── redirect/                # 重定向页
    │   ├── error/                   # 错误页（404/401等）
    │   ├── system/                  # 系统管理页面
    │   ├── monitor/                 # 系统监控页面
    │   ├── tool/                    # 工具模块页面
    │   ├── workflow/                # 工作流页面
    │   └── demo/                    # 示例页面
    ├── router/                      # 路由配置
    │   └── index.ts                 # 路由定义
    ├── store/                       # Pinia 状态管理
    │   ├── index.ts                 # Store 入口
    │   └── modules/                 # 模块化 Store
    ├── components/                  # 公共组件（22个）
    │   ├── Breadcrumb/              # 面包屑导航
    │   ├── DictTag/                 # 字典标签
    │   ├── Editor/                  # 富文本编辑器
    │   ├── FileUpload/              # 文件上传
    │   ├── Hamburger/               # 汉堡菜单按钮
    │   ├── IconSelect/              # 图标选择器
    │   ├── iFrame/                  # 内嵌 iframe
    │   ├── ImagePreview/            # 图片预览
    │   ├── ImageUpload/             # 图片上传
    │   ├── LangSelect/              # 语言切换
    │   ├── Pagination/              # 分页组件
    │   ├── ParentView/              # 父级视图容器
    │   ├── Process/                 # 流程组件
    │   ├── RightToolbar/            # 右侧工具栏
    │   ├── RoleSelect/              # 角色选择器
    │   ├── RuoYiDoc/                # 文档链接
    │   ├── RuoYiGit/                # Git 链接
    │   ├── Screenfull/              # 全屏切换
    │   ├── SizeSelect/              # 尺寸切换
    │   ├── SvgIcon/                 # SVG 图标
    │   ├── TopNav/                  # 顶部导航
    │   └── UserSelect/              # 用户选择器
    ├── layout/                      # 布局组件
    │   ├── index.vue                # 主布局
    │   └── components/              # 布局子组件
    ├── lang/                        # 国际化
    │   ├── index.ts                 # i18n 入口
    │   ├── zh_CN.ts                 # 中文语言包
    │   └── en_US.ts                 # 英文语言包
    ├── assets/                      # 静态资源（图片/样式）
    ├── hooks/                       # 组合式函数（Composables）
    ├── directive/                   # 自定义指令
    ├── enums/                       # 枚举定义
    ├── plugins/                     # 插件注册
    ├── types/                       # TypeScript 类型定义
    └── utils/                       # 工具函数
```

---

## 前端页面清单

### 系统管理（system/）

| 目录 | 页面名称 | 功能说明 |
|------|----------|----------|
| `system/user/` | 用户管理 | 用户增删改查、密码重置、角色分配 |
| `system/role/` | 角色管理 | 角色权限分配、数据权限设置 |
| `system/menu/` | 菜单管理 | 菜单树维护、按钮权限配置 |
| `system/dept/` | 部门管理 | 组织架构树形管理 |
| `system/post/` | 岗位管理 | 岗位信息维护 |
| `system/dict/` | 字典管理 | 字典类型与字典数据管理 |
| `system/config/` | 参数设置 | 系统参数配置 |
| `system/notice/` | 通知公告 | 公告发布与管理 |
| `system/tenant/` | 租户管理 | 多租户管理 |
| `system/tenantPackage/` | 租户套餐 | 租户套餐配置 |
| `system/oss/` | 文件管理 | 对象存储文件管理 |
| `system/client/` | 客户端管理 | OAuth2 客户端管理 |

### 系统监控（monitor/）

| 目录 | 页面名称 | 功能说明 |
|------|----------|----------|
| `monitor/online/` | 在线用户 | 查看在线用户、强制踢出 |
| `monitor/logininfor/` | 登录日志 | 登录成功/失败记录查询 |
| `monitor/operlog/` | 操作日志 | 用户操作行为审计 |
| `monitor/cache/` | 缓存监控 | Redis 缓存状态查看 |
| `monitor/admin/` | 服务监控 | 服务器资源监控 |
| `monitor/snailjob/` | 任务调度 | SnailJob 分布式任务管理 |

### 工具模块（tool/）

| 目录 | 页面名称 | 功能说明 |
|------|----------|----------|
| `tool/gen/` | 代码生成 | 数据库表代码自动生成 |

### 工作流（workflow/）

| 目录 | 页面名称 | 功能说明 |
|------|----------|----------|
| `workflow/category/` | 流程分类 | 工作流分类管理 |
| `workflow/processDefinition/` | 流程定义 | 流程模型设计与发布 |
| `workflow/processInstance/` | 流程实例 | 运行中的流程实例管理 |
| `workflow/task/` | 任务管理 | 待办/已办任务处理 |
| `workflow/leave/` | 请假流程 | 请假申请示例 |
| `workflow/spel/` | SpEL 表达式 | 流程表达式管理 |

### 示例页面（demo/）

| 目录 | 页面名称 | 功能说明 |
|------|----------|----------|
| `demo/demo/` | 基础示例 | 组件使用示例 |
| `demo/tree/` | 树形示例 | 树形结构示例 |

---

## 关键配置文件说明

### 后端配置

| 文件 | 说明 |
|------|------|
| `pom.xml`（根） | Maven 多模块父 POM，管理依赖版本、插件配置 |
| `magic-ruoyi-admin/pom.xml` | 后端模块依赖，继承根 POM |
| `application.yml` | 主配置文件，包含服务器端口、Sa-Token、MyBatis-Plus、多租户、工作流等核心配置 |
| `application-dev.yml` | 开发环境配置（数据源、Redis 等） |
| `application-local.yml` | 本地环境配置 |
| `application-prod.yml` | 生产环境配置 |
| `logback-plus.xml` | 日志框架配置，定义日志级别、输出格式、文件滚动策略 |
| `i18n/messages*.properties` | 国际化资源文件，支持多语言错误提示与消息 |

### 前端配置

| 文件 | 说明 |
|------|------|
| `package.json` | 前端依赖管理，版本 `5.5.3-2.5.3`，基于 Vue 3.5 + Element Plus |
| `vite.config.ts` | Vite 构建配置，包含插件注册、代理设置、构建优化 |
| `tsconfig.json` | TypeScript 编译选项 |
| `uno.config.ts` | UnoCSS 原子化 CSS 配置 |
| `eslint.config.ts` | ESLint 代码规范配置 |
| `.env.development` | 开发环境变量（API 基础路径等） |
| `.env.production` | 生产环境变量 |

### application.yml 核心配置项摘要

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 后端服务端口 |
| `captcha.enable` | `true` | 是否启用验证码 |
| `captcha.type` | `MATH` | 验证码类型（数学计算） |
| `sa-token.token-name` | `Authorization` | Token 请求头名称 |
| `sa-token.is-concurrent` | `true` | 是否允许并发登录 |
| `tenant.enable` | `true` | 是否开启多租户 |
| `mybatis-plus.enableLogicDelete` | `true` | 全局逻辑删除开关 |
| `mybatis-plus.idType` | `ASSIGN_ID` | 主键策略（雪花算法） |
| `api-decrypt.enabled` | `true` | 是否开启接口加密（RSA） |
| `warm-flow.enabled` | `true` | 是否开启工作流引擎 |
| `warm-flow.ui` | `true` | 是否开启工作流设计器 |
| `springdoc.api-docs.enabled` | `true` | 是否开启 Swagger 接口文档 |
| `sse.enabled` | `true` | 是否开启 SSE 服务端推送 |
| `websocket.enabled` | `false` | 是否开启 WebSocket |
| `xss.enabled` | `true` | 是否开启 XSS 防护 |

---

## 技术栈概览

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot | 应用框架 |
| Sa-Token | 认证授权框架 |
| MyBatis-Plus | ORM 框架 |
| Undertow | Web 服务器 |
| Warm-Flow | 工作流引擎 |
| SnailJob | 分布式任务调度 |
| Redis | 缓存与会话存储 |
| Springdoc | OpenAPI 接口文档 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3.5 | 前端框架（Composition API） |
| TypeScript | 类型系统 |
| Vite 6 | 构建工具 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由管理 |
| Vue I18n | 国际化 |
| UnoCSS | 原子化 CSS |
| Axios | HTTP 客户端 |
| ECharts | 数据可视化 |
| VXETable | 高级表格组件 |
