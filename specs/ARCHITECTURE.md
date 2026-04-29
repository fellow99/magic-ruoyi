# magic-ruoyi 系统架构文档

> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 架构概述

magic-ruoyi 是一个融合架构的企业级后台管理系统，将 **RuoYi-Vue-Plus**（成熟的企业级开发框架）与 **magic-api**（动态接口引擎）深度整合。项目采用 Maven 聚合工程结构，通过级联编译实现模块组装，不修改上游框架源代码。

### 1.1 技术栈总览

| 层级 | 技术选型 |
|------|----------|
| 前端框架 | Vue 3.5 + TypeScript 5.9 + Vite 6 |
| UI 组件库 | Element Plus 2.11 + UnoCSS |
| 状态管理 | Pinia 3.0 |
| 后端框架 | Spring Boot 3.5.14 + JDK 21 |
| 认证授权 | Sa-Token + JWT |
| ORM 框架 | MyBatis-Plus |
| 动态接口 | magic-api 2.2.2 |
| 数据库 | MySQL |
| 缓存 | Redis |
| 对象存储 | MinIO |
| Web 服务器 | Undertow（内嵌）+ Nginx（生产） |
| 工作流 | Warm-Flow |
| 接口文档 | SpringDoc (OpenAPI 3) |

### 1.2 架构设计理念

- **零侵入集成**: 不修改 RuoYi-Vue-Plus 和 magic-api 的源代码，通过 Maven 依赖和配置实现融合
- **模块化组装**: 通过 Maven 聚合工程将所需模块按需引入，排除不需要的模块（如 ruoyi-job、ruoyi-demo、ruoyi-workflow）
- **前后端分离**: 前端独立开发部署，通过 RESTful API 与后端交互
- **多租户原生支持**: 基于 MyBatis-Plus 插件实现数据级租户隔离

---

## 2. 分层架构

系统采用经典的分层架构，各层职责清晰，依赖方向单向向下。

```
┌─────────────────────────────────────────────────┐
│              前端层 (magic-ruoyi-web)             │
│   Vue3 + TypeScript + Element Plus + Pinia       │
└────────────────────┬────────────────────────────┘
                     │ HTTPS / JSON
                     ▼
┌─────────────────────────────────────────────────┐
│              网关层 (Nginx)                       │
│   负载均衡 / SSL终止 / 静态资源 / 反向代理        │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│              应用层 (Spring Boot 3.5.14)          │
│  ┌───────────────────────────────────────────┐  │
│  │  Controller 层 - 请求接收、参数校验         │  │
│  ├───────────────────────────────────────────┤  │
│  │  Service 层 - 业务逻辑、事务管理            │  │
│  ├───────────────────────────────────────────┤  │
│  │  Mapper 层 - MyBatis-Plus 数据访问         │  │
│  ├───────────────────────────────────────────┤  │
│  │  Entity 层 - 数据实体 (domain/bo/vo)       │  │
│  ├───────────────────────────────────────────┤  │
│  │  magic-api 动态接口层 - 脚本引擎            │  │
│  └───────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│              数据层                               │
│   MySQL + Redis + MinIO                          │
└─────────────────────────────────────────────────┘
```

### 2.1 前端层 (magic-ruoyi-web)

前端是独立的 Vue 3 单页应用，通过 Vite 构建。

**核心职责:**
- 用户界面渲染与交互
- 路由管理（Vue Router）
- 全局状态管理（Pinia）
- HTTP 请求封装（Axios），自动携带 JWT Token
- 请求/响应拦截器（加密解密、错误处理）
- 权限指令控制（按钮级权限）
- 国际化支持（vue-i18n）

**关键技术点:**
- 使用 UnoCSS 实现原子化 CSS
- 使用 unplugin-auto-import 和 unplugin-vue-components 实现自动导入
- 使用 vxe-table 处理复杂表格场景
- 使用 crypto-js 和 jsencrypt 实现前端加解密

### 2.2 网关层 (Nginx)

生产环境中 Nginx 作为反向代理和负载均衡器。

**核心职责:**
- SSL/TLS 终止，卸载 HTTPS 加密解密
- 静态资源服务（前端构建产物）
- API 请求反向代理到后端服务
- 负载均衡（多实例部署时）
- Gzip 压缩
- 请求限流

### 2.3 应用层 (Spring Boot)

后端核心，基于 Spring Boot 3.5.14 构建，内嵌 Undertow 作为 Web 容器。

#### 2.3.1 Controller 层

**职责:** 接收 HTTP 请求，参数校验，调用 Service 层，返回统一响应格式。

**关键特征:**
- 使用 `@RestController` 和 `@RequestMapping` 定义路由
- 使用 Jakarta Validation (`@Validated`) 进行参数校验
- 使用 `@ApiEncrypt` 注解标记需要加解密的接口
- 使用 `@SaIgnore` 跳过认证（如登录、验证码接口）
- 使用 `@RateLimiter` 进行接口限流
- 统一返回 `R<T>` 响应对象

**主要 Controller:**
- `AuthController` - 认证相关（登录、注册、登出、租户列表、社交登录）
- `CaptchaController` - 验证码生成
- `IndexController` - 首页路由
- 以及 RuoYi-Vue-Plus 框架提供的系统管理 Controller

#### 2.3.2 Service 层

**职责:** 业务逻辑处理，事务管理，协调多个 Mapper 操作。

**关键特征:**
- 使用 `@Service` 注解标记
- 使用 `@Transactional` 管理事务
- 认证策略模式：`IAuthStrategy` 接口 + 多个实现类（PasswordAuthStrategy、SmsAuthStrategy、EmailAuthStrategy、SocialAuthStrategy、XcxAuthStrategy）
- 通过 `@DS` 注解支持多数据源切换

**核心 Service:**
- `SysLoginService` - 登录流程编排
- `SysRegisterService` - 用户注册流程
- 以及 RuoYi-Vue-Plus 框架提供的系统管理 Service

#### 2.3.3 Mapper 层

**职责:** 数据访问层，通过 MyBatis-Plus 实现 ORM 映射。

**关键特征:**
- 继承 `BaseMapper<T>` 获得基础 CRUD 能力
- Mapper XML 文件位于 `classpath*:mapper/**/*Mapper.xml`
- 扫描路径: `org.dromara.**.mapper` 和 `org.fellow99.**.mapper`
- 主键策略: `ASSIGN_ID`（雪花算法）
- 逻辑删除全局开启

#### 2.3.4 Entity 层

**职责:** 数据模型定义，区分不同场景的数据结构。

**模型分类:**
- `domain` - 数据库实体，与表结构一一对应
- `domain/bo` - Business Object，用于接收请求参数
- `domain/vo` - View Object，用于返回响应数据
- 使用 MapStruct 进行对象转换（`MapstructUtils`）

#### 2.3.5 magic-api 动态接口层

**职责:** 提供无需编写 Java 代码即可创建 API 的能力。

**关键特征:**
- 通过 `magic-api-spring-boot-starter` 集成
- Web 编辑器访问路径: `/magic/web`
- 支持 SQL 模式、脚本模式（Groovy/JavaScript）
- 接口动态注册，即时生效，无需重启
- 接口定义持久化到数据库或文件系统
- 与 Spring Bean 无缝交互，可注入 Service、Mapper

### 2.4 数据层

**MySQL:** 主数据库，存储业务数据、用户信息、权限配置、租户数据、magic-api 接口定义等。

**Redis:** 缓存层，用于:
- Sa-Token 会话存储
- 验证码缓存
- 分布式锁（Lock4j）
- 数据字典缓存
- 接口限流计数

**MinIO:** 对象存储，用于文件上传下载（通过 RuoYi-Vue-Plus 的 OSS 模块集成）。

---

## 3. 核心模块依赖关系

### 3.1 Maven 聚合工程结构

```
magic-ruoyi (org.fellow99:magic-ruoyi-modules:1.0.0)  [pom]
│
├── magic-ruoyi-admin (org.fellow99:magic-ruoyi-admin:1.0.0)  [jar]
│   │
│   ├── 继承自 RuoYi-Vue-Plus 5.6.0 (org.dromara:ruoyi-vue-plus)
│   │
│   └── 依赖模块:
│       ├── ruoyi-common-web          # Web 基础组件
│       ├── ruoyi-common-doc          # 接口文档 (SpringDoc)
│       ├── ruoyi-common-social       # 社交登录
│       ├── ruoyi-common-ratelimiter  # 接口限流
│       ├── ruoyi-common-mail         # 邮件服务
│       ├── ruoyi-common-tenant       # 多租户支持
│       ├── ruoyi-system              # 系统管理模块
│       ├── ruoyi-generator           # 代码生成模块
│       ├── magic-api-spring-boot-starter  # 动态接口引擎
│       ├── spring-boot-admin-starter-client # 监控客户端
│       └── mysql-connector-j         # MySQL 驱动
```

### 3.2 模块说明

| 模块 | 说明 | 状态 |
|------|------|------|
| `magic-ruoyi-modules` | Maven 聚合父工程，定义版本和模块列表 | 启用 |
| `magic-ruoyi-admin` | 主启动模块，Web 服务入口 | 启用 |
| `ruoyi-system` | 系统管理（用户、角色、菜单、部门、字典等） | 启用 |
| `ruoyi-generator` | 代码生成器（根据数据库表生成 CRUD 代码） | 启用 |
| `ruoyi-common-tenant` | 多租户插件（自动注入租户过滤条件） | 启用 |
| `ruoyi-common-social` | 第三方社交登录（JustAuth 集成） | 启用 |
| `ruoyi-common-ratelimiter` | 基于 Redis 的接口限流 | 启用 |
| `ruoyi-common-doc` | SpringDoc OpenAPI 3 文档 | 启用 |
| `ruoyi-common-mail` | 邮件发送服务 | 启用 |
| `ruoyi-common-web` | Web 基础组件（全局异常、跨域、XSS 过滤） | 启用 |
| `ruoyi-job` | 定时任务（PowerJob 集成） | 已注释 |
| `ruoyi-demo` | 示例模块 | 已注释 |
| `ruoyi-workflow` | 工作流模块 | 已注释 |

### 3.3 组件扫描策略

`MagicRuoyiApplication` 通过 `@ComponentScan` 显式指定扫描路径:

```java
@ComponentScan(basePackages = {
    "org.fellow99",  // magic-ruoyi 自定义组件
    "org.dromara"    // RuoYi-Vue-Plus 框架组件
})
```

这使得自定义 Controller、Service 和框架提供的组件都能被 Spring 容器正确发现和注册。

---

## 4. 数据流描述

### 4.1 请求处理流程

```
浏览器发起请求
    │
    ▼
Nginx 接收请求
    ├── 静态资源请求 → 直接返回前端构建产物
    └── API 请求 → 反向代理到后端 8080 端口
         │
         ▼
    Spring Boot (Undertow)
         │
         ▼
    Sa-Token 拦截器
         ├── 检查 Token 有效性
         ├── 解析用户身份
         └── 权限校验（通过 @SaCheckPermission 注解）
         │
         ▼
    XSS 过滤器 → 清理恶意脚本
         │
         ▼
    Controller 接收请求
         ├── @Validated 参数校验
         ├── @ApiEncrypt 解密请求体
         └── 调用 Service 层
         │
         ▼
    Service 层处理业务逻辑
         ├── 事务管理 (@Transactional)
         ├── 调用 Mapper 层
         └── 调用外部服务（邮件、OSS 等）
         │
         ▼
    Mapper 层执行数据库操作
         ├── MyBatis-Plus 自动注入租户条件
         ├── 逻辑删除自动处理
         └── 返回实体对象
         │
         ▼
    Service 转换为 VO 对象返回
         │
         ▼
    Controller 封装为 R<T> 统一响应
         ├── @ApiEncrypt 加密响应体
         └── 返回 JSON
         │
         ▼
    Nginx → 浏览器
```

### 4.2 响应格式

所有 API 统一返回 `R<T>` 格式:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

---

## 5. 认证流程

系统采用 **Sa-Token** 作为认证框架，基于 **JWT** 实现无状态认证。

### 5.1 登录流程

```
用户输入账号密码
    │
    ▼
前端加密请求体 (RSA + AES)
    │
    ▼
POST /auth/login
    │
    ▼
AuthController.login()
    ├── 解析请求体为 LoginBody
    ├── 校验 clientId 和 grantType
    ├── 校验租户状态 (loginService.checkTenant)
    └── 调用 IAuthStrategy.login()
         │
         ▼
    策略模式分发 (根据 grantType)
         ├── password → PasswordAuthStrategy
         ├── sms      → SmsAuthStrategy
         ├── email    → EmailAuthStrategy
         ├── social   → SocialAuthStrategy
         └── xcx      → XcxAuthStrategy
         │
         ▼
    具体策略实现
         ├── 验证凭证（密码/验证码/第三方 token）
         ├── 检查用户状态（是否禁用、是否删除）
         ├── 检查密码错误次数（超过阈值锁定）
         └── 构建 LoginUser 对象
         │
         ▼
    StpUtil.login(userId)
         ├── 生成 JWT Token
         ├── 存储会话信息到 Redis
         └── 返回 Token
         │
         ▼
    构建 LoginVo (token + 用户信息 + 权限列表)
         │
         ▼
    发送 SSE 欢迎消息 (延迟 5 秒)
         │
         ▼
    返回 R<LoginVo> 给前端
         │
         ▼
    前端解密响应，存储 Token 到 localStorage
```

### 5.2 请求认证流程

```
请求到达后端
    │
    ▼
Sa-Token 拦截器
    ├── 从 Authorization Header 提取 Token
    ├── 验证 Token 有效性（过期、篡改）
    ├── 从 Redis 获取会话信息
    └── 将用户信息绑定到当前线程
         │
         ▼
    @SaCheckPermission 注解校验
         ├── 检查用户是否拥有指定权限
         ├── 超级管理员跳过权限检查
         └── 无权限则抛出 NotLoginException / NotPermissionException
         │
         ▼
    进入 Controller 方法
```

### 5.3 Token 配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| Token 名称 | `Authorization` | HTTP Header 名称 |
| 并发登录 | `true` | 允许同一账号多端同时在线 |
| Token 共享 | `false` | 每次登录生成新 Token |
| Token 类型 | JWT | 无状态，自包含用户信息 |

---

## 6. 多租户流程

系统基于 **MyBatis-Plus 租户插件** 实现数据级多租户隔离。

### 6.1 租户配置

```yaml
tenant:
  enable: true          # 开启多租户
  excludes:             # 排除表（不进行租户过滤）
    - sys_menu
    - sys_tenant
    - sys_tenant_package
    - sys_role_dept
    - sys_role_menu
    - sys_user_post
    - sys_user_role
    - sys_client
    - sys_oss_config
    - flow_spel
```

### 6.2 租户处理流程

```
请求进入后端
    │
    ▼
TenantHelper 解析租户 ID
    ├── 从请求参数获取 tenantId
    ├── 从 Token 中获取 tenantId
    └── 从域名映射推断 tenantId
         │
         ▼
    TenantHelper.dynamic(tenantId)
         ├── 将 tenantId 绑定到当前线程 (ThreadLocal)
         └── 设置租户上下文
         │
         ▼
    MyBatis-Plus 租户插件拦截 SQL
         ├── 检查当前表是否在排除列表中
         ├── 如果不在排除列表，自动追加 WHERE tenant_id = ?
         ├── INSERT 时自动注入 tenant_id 字段
         └── UPDATE/DELETE 时自动追加租户条件
         │
         ▼
    执行 SQL，仅返回当前租户数据
         │
         ▼
    请求结束，清理租户上下文
```

### 6.3 租户排除表说明

排除表是不需要进行租户过滤的表，主要包括:
- **系统基础表**: `sys_menu`（菜单全局共享）、`sys_tenant`（租户列表）
- **关联表**: `sys_role_dept`、`sys_role_menu`、`sys_user_post`、`sys_user_role`
- **客户端配置**: `sys_client`（认证客户端配置）
- **OSS 配置**: `sys_oss_config`（对象存储配置）

---

## 7. Magic API 集成方式

### 7.1 集成原理

magic-api 通过 `magic-api-spring-boot-starter` 以 Spring Boot Starter 方式集成，与 RuoYi-Vue-Plus 框架无代码冲突。

### 7.2 核心能力

| 能力 | 说明 |
|------|------|
| Web 编辑器 | 访问 `/magic/web` 进入可视化接口编辑界面 |
| SQL 模式 | 直接编写 SQL，自动处理参数绑定和结果映射 |
| 脚本模式 | 使用 Groovy/JavaScript 编写复杂业务逻辑 |
| 动态注册 | 保存后立即生效，无需重启应用 |
| Spring Bean 交互 | 可注入任意 Spring Bean（Service、Mapper 等） |
| 接口分组 | 按模块组织接口，便于管理 |
| 版本管理 | 支持接口版本回滚 |
| 权限控制 | 可配置接口访问权限 |

### 7.3 与 RuoYi-Vue-Plus 的交互

```
magic-api 脚本
    │
    ├── 可注入 Spring Bean
    │   ├── sysUserService → 操作用户
    │   ├── sysMenuService → 操作菜单
    │   └── 任意自定义 Service
    │
    ├── 可使用 RuoYi 工具类
    │   ├── LoginHelper → 获取当前登录用户
    │   ├── TenantHelper → 获取当前租户
    │   └── MessageUtils → 国际化消息
    │
    └── 可访问数据库
        ├── 直接编写 SQL
        └── 通过 Mapper Bean 调用
```

### 7.4 接口持久化

magic-api 的接口定义默认持久化到数据库（或文件系统），包含:
- 接口路径、请求方法
- 脚本内容
- 参数定义
- 返回结构
- 权限配置

---

## 8. 部署拓扑

### 8.1 开发环境

```
┌──────────────────────────────────────────┐
│              开发者本地环境                │
│                                          │
│  前端 (magic-ruoyi-web)                   │
│  ├── 端口: 8000                           │
│  ├── 构建工具: Vite                       │
│  └── 热更新: HMR                          │
│                                          │
│  后端 (magic-ruoyi-admin)                 │
│  ├── 端口: 8080                           │
│  ├── Web容器: Undertow                    │
│  └── 启动类: MagicRuoyiApplication        │
│                                          │
│  数据库服务                               │
│  ├── MySQL (本地或远程)                    │
│  ├── Redis (本地或远程)                    │
│  └── MinIO (本地或远程)                    │
│                                          │
│  前端通过 Vite proxy 代理 API 到 8080      │
└──────────────────────────────────────────┘
```

**启动方式:**
- 后端: `mvn spring-boot:run` 或 IDE 直接运行 `MagicRuoyiApplication`
- 前端: `npm run dev`（Vite 开发服务器，端口 8000）
- 前端通过 Vite 配置的反向代理将 `/api` 等请求转发到后端 8080 端口

### 8.2 生产环境

```
                    ┌─────────────┐
                    │   用户浏览器  │
                    └──────┬──────┘
                           │ HTTPS
                           ▼
                    ┌─────────────┐
                    │    Nginx     │
                    │  (443/80)    │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │ 静态资源  │ │ 后端实例1 │ │ 后端实例2 │
        │ (dist/)  │ │ (8080)   │ │ (8080)   │
        └──────────┘ └────┬─────┘ └────┬─────┘
                          │            │
                          └─────┬──────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
              ┌─────────┐ ┌─────────┐ ┌─────────┐
              │  MySQL  │ │  Redis  │ │  MinIO  │
              └─────────┘ └─────────┘ └─────────┘
```

**Nginx 配置要点:**
- 监听 443 端口（HTTPS），配置 SSL 证书
- 静态资源（前端构建产物）直接由 Nginx 服务
- `/api/**` 等 API 请求反向代理到后端集群
- 配置负载均衡策略（轮询、IP Hash 等）
- 开启 Gzip 压缩
- 配置安全头（X-Frame-Options、X-Content-Type-Options 等）

**后端部署:**
- 打包: `mvn clean package -DskipTests`
- 产物: `magic-ruoyi-admin.jar`
- 启动: `java -jar magic-ruoyi-admin.jar --spring.profiles.active=prod`
- 支持多实例部署，通过 Nginx 负载均衡

### 8.3 环境配置

| 环境 | Profile | 说明 |
|------|---------|------|
| 开发 | `dev` | 开发环境配置，详细日志 |
| 本地 | `local` | 本地调试配置 |
| 生产 | `prod` | 生产环境配置，优化性能 |

通过 Maven Profile 和 Spring Profile 双重控制:
- Maven Profile 控制编译时的资源过滤（`@profiles.active@`）
- Spring Profile 控制运行时的配置加载（`spring.profiles.active`）

---

## 9. 安全机制

### 9.1 接口加密

系统采用 RSA 非对称加密保护敏感接口数据:

- **请求加密**: 前端使用公钥加密请求体，后端使用私钥解密
- **响应加密**: 后端使用私钥加密响应体，前端使用公钥解密
- 通过 `@ApiEncrypt` 注解标记需要加密的接口（如登录、注册）

### 9.2 XSS 防护

- 全局 XSS 过滤器开启
- 自动清理请求参数中的恶意脚本
- 特定接口可通过 `excludeUrls` 排除

### 9.3 接口限流

- 基于 Redis 实现分布式限流
- 通过 `@RateLimiter` 注解配置
- 支持按 IP、按用户等限流策略

### 9.4 分布式锁

- 基于 Lock4j + Redis 实现
- 默认获取锁超时 3 秒
- 锁过期时间 30 秒

---

## 10. 扩展点

### 10.1 新增认证策略

实现 `IAuthStrategy` 接口，注册为 Spring Bean:

```java
@Component("customAuthStrategy")
public class CustomAuthStrategy implements IAuthStrategy {
    @Override
    public LoginVo login(String body, SysClientVo client) {
        // 自定义认证逻辑
    }
}
```

### 10.2 新增业务模块

1. 在 `magic-ruoyi-admin/pom.xml` 中添加依赖
2. 在 `org.fellow99` 包下创建 Controller、Service、Mapper
3. 确保包路径在 `@ComponentScan` 范围内

### 10.3 新增多租户排除表

在 `application.yml` 的 `tenant.excludes` 列表中添加表名。

---

## 附录

### A. 项目目录结构

```
magic-ruoyi/
├── pom.xml                          # Maven 聚合父工程
├── magic-ruoyi-admin/               # 后端主启动模块
│   ├── pom.xml                      # 模块依赖定义
│   └── src/main/
│       ├── java/org/fellow99/magic/ruoyi/
│       │   ├── MagicRuoyiApplication.java   # 启动类
│       │   ├── controller/                  # 自定义 Controller
│       │   ├── service/                     # 自定义 Service
│       │   │   ├── IAuthStrategy.java       # 认证策略接口
│       │   │   ├── SysLoginService.java     # 登录服务
│       │   │   ├── SysRegisterService.java  # 注册服务
│       │   │   └── impl/                    # 策略实现
│       │   │       ├── PasswordAuthStrategy.java
│       │   │       ├── SmsAuthStrategy.java
│       │   │       ├── EmailAuthStrategy.java
│       │   │       ├── SocialAuthStrategy.java
│       │   │       └── XcxAuthStrategy.java
│       │   ├── domain/                      # 数据模型
│       │   │   └── vo/                      # View Objects
│       │   └── listener/                    # 事件监听器
│       └── resources/
│           ├── application.yml              # 主配置
│           ├── application-dev.yml          # 开发环境
│           ├── application-prod.yml         # 生产环境
│           └── application-local.yml        # 本地环境
├── magic-ruoyi-web/                 # 前端项目
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── .env.development
│   ├── .env.production
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── api/                     # API 接口定义
│       ├── views/                   # 页面组件
│       ├── components/              # 通用组件
│       ├── store/                   # Pinia 状态管理
│       ├── router/                  # 路由配置
│       └── utils/                   # 工具函数
└── sql/                             # 数据库脚本
```

### B. 关键版本号

| 组件 | 版本 |
|------|------|
| magic-ruoyi | 1.0.0 |
| RuoYi-Vue-Plus | 5.6.0 |
| Spring Boot | 3.5.14 |
| magic-api | 2.2.2 |
| Vue | 3.5.22 |
| Element Plus | 2.11.7 |
| TypeScript | 5.9.3 |
| Node.js | >= 20.15.0 |
