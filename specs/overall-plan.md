# 整体技术方案（Overall Plan）

> magic-ruoyi 项目整体技术实现方案。
> 本文档描述系统架构、模块划分、关键技术决策、交叉关注点、测试策略和部署方案。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 技术上下文

### 1.1 项目定位

magic-ruoyi 是一个企业级应用快速开发平台。它将 RuoYi-Vue-Plus（成熟的企业级后台管理框架）与 magic-api（动态接口引擎）融合为一体，提供账户权限、多租户、组织管理、系统监控、工作流编排、代码生成和零代码 API 开发能力。

### 1.2 核心约束

| 约束项 | 说明 |
|--------|------|
| 不修改上游源码 | 通过 Maven 依赖引入 RuoYi-Vue-Plus 和 magic-api，零源码修改 |
| 独立包名隔离 | 自定义代码使用 `org.fellow99` 包名，与上游 `org.dromara` 和 `org.ssssssss` 严格隔离 |
| 级联编译 | Maven 聚合工程实现一次构建，全模块编译 |
| 前端独立开发 | 前端复制 RuoYi-Vue-Plus-UI 工程结构，通过 Vite proxy 代理 API 请求 |

### 1.3 技术栈

**后端**

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.5.14 |
| 父框架 | RuoYi-Vue-Plus | 5.6.0 |
| 语言 | Java | 21+ |
| ORM | MyBatis-Plus | 3.5.16 |
| 数据库 | MySQL | 最新稳定版 |
| 缓存 | Redis + Redisson | 最新稳定版 |
| 认证 | Sa-Token | 1.44.0 |
| 动态接口 | magic-api | 2.2.2 |
| Web 服务器 | Undertow | 内置 |
| 工作流 | Warm-Flow | 1.8.4 |
| 分布式任务 | SnailJob | 1.9.0 |

**前端**

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.5.22 |
| 语言 | TypeScript | 5.9.3 |
| 构建 | Vite | 6.4.1 |
| UI | Element Plus | 2.11.7 |
| 状态 | Pinia | 3.0.3 |
| 表格 | VXE Table | 4.17.7 |
| 图表 | ECharts | 5.6.0 |

---

## 2. 宪法合规检查

本项目遵循 [项目宪法](./constitution.md) 定义的核心原则。以下逐项检查本技术方案与宪法的一致性。

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 使用成熟框架（RuoYi-Vue-Plus、magic-api），避免过度设计。认证策略通过接口 + Bean 命名实现，不引入额外抽象层 |
| 约定优于配置 | 合规 | 遵循 RuoYi-Vue-Plus 的目录结构、命名规范、响应格式。magic-api 使用默认配置，仅在业务确有需要时覆盖 |
| 实用优于完美 | 合规 | 先交付可用功能，再逐步优化。认证策略实现五种方式但按需启用，工作流模块默认开启但可按需关闭 |
| 安全优于便利 | 合规 | Sa-Token 认证、接口加密（RSA）、验证码、限流、XSS 防护、防重复提交均为默认开启的横切能力 |
| 零样板代码 | 合规 | MyBatis-Plus 代码生成器、magic-api 可视化编排、MapStruct 对象映射，将样板代码降至最低 |
| 渐进式复杂度 | 合规 | magic-api 脚本从单行 SQL 到多步逻辑逐步演进，前端组件从基础表单到复杂交互逐步增强 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入上游框架，自定义代码使用独立包名 `org.fellow99`，`@ComponentScan` 显式声明扫描范围 |
| 清晰模块边界 | 合规 | magic-ruoyi-admin 承担启动、配置、粘合层职责，系统管理、工作流、代码生成直接复用上游模块 |
| 多租户优先 | 合规 | 所有业务表默认启用租户隔离（`tenant_id` + MyBatis-Plus 租户插件），系统级表显式排除 |
| 前后端分离 | 合规 | 前端独立开发服务器（Vite :8000），后端独立服务（Undertow :8080），通过 Vite proxy 代理 API 请求 |

---

## 3. 实现策略概述

### 3.1 架构总览

```
┌─────────────────────────────────────────────────────────┐
│                      用户浏览器                          │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP/HTTPS
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Nginx（生产）/ Vite Proxy（开发）            │
│         前端 :8000  │  后端 :8080  │  Magic API :/magic  │
└──────┬──────────────────────┬───────────────────────────┘
       │                      │
       ▼                      ▼
┌──────────────┐    ┌─────────────────────────────────────┐
│  magic-ruoyi-web   │    │      magic-ruoyi-admin            │
│  Vue 3 + TS    │    │    Spring Boot 3.5 + Undertow       │
│  Element Plus  │    │                                     │
│  Pinia         │    │  ┌───────────────────────────────┐  │
│  Vite 6        │    │  │  RuoYi-Vue-Plus 模块           │  │
│                │    │  │  ruoyi-system / ruoyi-generator│  │
│                │    │  └───────────────────────────────┘  │
│                │    │  ┌───────────────────────────────┐  │
│                │    │  │  magic-api 引擎                │  │
│                │    │  │  /magic/web 编辑器             │  │
│                │    │  └───────────────────────────────┘  │
│                │    │  ┌───────────────────────────────┐  │
│                │    │  │  粘合层（org.fellow99）        │  │
│                │    │  │  AuthController / 认证策略     │  │
│                │    │  │  CaptchaController / 扩展      │  │
│                │    │  └───────────────────────────────┘  │
└──────────────┘    └──────────┬────────────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
        ┌──────────┐   ┌──────────┐   ┌──────────────┐
        │  MySQL   │   │  Redis   │   │  MinIO       │
        │ :3306    │   │ :6379    │   │ (可配置)      │
        └──────────┘   └──────────┘   └──────────────┘
```

### 3.2 Maven 聚合工程

项目采用 Maven 多模块聚合结构，实现级联编译。

**POM 继承链**

```
ruoyi-vue-plus (org.dromara:ruoyi-vue-plus:5.6.0)  ← 上游父 POM
    └── magic-ruoyi-modules (org.fellow99:magic-ruoyi-modules:1.0.0)  ← 聚合 POM
            └── magic-ruoyi-admin (org.fellow99:magic-ruoyi-admin:1.0.0)  ← 可执行模块
```

**根 POM（pom.xml）关键配置**

- 继承 `org.dromara:ruoyi-vue-plus:5.6.0`，获取全部依赖版本管理
- 定义项目版本 `magic-ruoyi.version=1.0.0`
- 覆盖 `magic-api.version=2.2.2`、`spring-boot.version=3.5.14`
- 声明模块 `magic-ruoyi-admin`
- 配置构建插件版本和编码

**模块 POM（magic-ruoyi-admin/pom.xml）关键依赖**

| 依赖 | 说明 |
|------|------|
| `ruoyi-system` | 系统管理模块（用户、角色、菜单、部门等） |
| `ruoyi-generator` | 代码生成模块 |
| `ruoyi-common-web` | Web 基础设施（拦截器、过滤器、全局异常处理） |
| `ruoyi-common-tenant` | 多租户支持 |
| `ruoyi-common-ratelimiter` | 接口限流 |
| `ruoyi-common-social` | 第三方社交登录 |
| `ruoyi-common-mail` | 邮件发送 |
| `ruoyi-common-doc` | OpenAPI 文档 |
| `magic-api-spring-boot-starter` | 动态接口引擎 |
| `mysql-connector-j` | MySQL 驱动 |

### 3.3 后端实现策略

#### 3.3.1 启动类与组件扫描

```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "org.fellow99",  // 自定义粘合层
    "org.dromara"    // RuoYi-Vue-Plus 框架组件
})
public class MagicRuoyiApplication { ... }
```

显式声明扫描范围，不依赖隐式扫描。确保自定义组件和框架组件均能被正确注册。

#### 3.3.2 认证策略模式

认证采用策略模式实现，支持五种认证方式。

```
IAuthStrategy (接口)
    ├── PasswordAuthStrategy  (passwordAuthStrategy)  ← 密码认证
    ├── SmsAuthStrategy       (smsAuthStrategy)       ← 短信验证码
    ├── EmailAuthStrategy     (emailAuthStrategy)     ← 邮箱验证码
    ├── SocialAuthStrategy    (socialAuthStrategy)    ← 第三方社交
    └── XcxAuthStrategy       (xcxAuthStrategy)       ← 小程序授权
```

**注册机制**: Bean 命名遵循 `grantType + "AuthStrategy"` 约定，如 `passwordAuthStrategy`。

**调用流程**:

1. `AuthController.login()` 接收登录请求
2. 解析 `grantType` 和 `clientId`
3. 校验客户端配置和租户状态
4. 通过 `IAuthStrategy.login(body, client, grantType)` 静态方法路由到对应策略
5. 策略实现完成认证逻辑，生成 JWT Token
6. 返回 `LoginVo`（包含 accessToken、expireIn、clientId）

**安全控制**:

- 密码错误次数限制: 最多 5 次，锁定 10 分钟（Redis 计数）
- 验证码校验: 登录前必须通过图形验证码
- 分布式锁: `@Lock4j` 防止并发登录问题
- 接口加密: `@ApiEncrypt` 标注敏感接口

#### 3.3.3 多租户实现

通过 MyBatis-Plus 租户插件实现自动 SQL 注入。

**工作原理**:

1. 请求进入时，从 Token 或请求头解析 `tenantId`
2. `TenantHelper.dynamic(tenantId, () -> {...})` 设置当前线程租户上下文
3. MyBatis-Plus 拦截器自动在 SQL 中追加 `WHERE tenant_id = ?`
4. 系统级表（`sys_menu`、`sys_tenant` 等）在配置中显式排除

**排除表清单**:

```yaml
tenant:
  excludes:
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

#### 3.3.4 Magic API 集成

magic-api 作为 Spring Boot Starter 引入，提供可视化 API 编辑器。

**访问路径**: `/magic/web`

**关键特性**:

- 脚本热更新，无需重启服务
- 支持 SQL、Groovy 脚本编写
- 内置数据库操作函数
- 支持权限配置和访问控制
- 路径不得与既有静态接口冲突

### 3.4 前端实现策略

#### 3.4.1 工程结构

前端复制 RuoYi-Vue-Plus-UI 工程结构，保持与后端框架的一致性。

**核心目录**:

| 目录 | 职责 |
|------|------|
| `src/api/` | API 接口定义，按功能域组织 |
| `src/views/` | 页面视图，按模块组织 |
| `src/components/` | 公共组件（22 个） |
| `src/store/` | Pinia 状态管理 |
| `src/router/` | 路由配置，支持动态路由 |
| `src/layout/` | 布局组件 |
| `src/utils/` | 工具函数 |

#### 3.4.2 动态路由

前端路由从后端动态加载。

**流程**:

1. 用户登录成功后，前端调用 `/system/menu/getRouters` 获取菜单路由
2. `permissionStore` 将菜单数据转换为 Vue Router 路由配置
3. 通过 `router.addRoute()` 动态注册路由
4. 路由守卫（`permission.ts`）拦截未认证访问

#### 3.4.3 认证流程

1. 用户输入账号密码，前端通过 RSA 公钥加密请求体
2. 调用 `/auth/login` 接口
3. 后端验证成功，返回 JWT Token
4. 前端存储 Token 到 localStorage 和 Pinia Store
5. 后续请求通过 Axios 拦截器自动附加 `Authorization` 请求头
6. Token 过期时，自动跳转登录页

#### 3.4.4 开发服务器配置

Vite 开发服务器通过 proxy 代理 API 请求到后端。

```typescript
// vite.config.ts 代理配置
server: {
  port: 8000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/magic': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

### 3.5 关键配置

| 配置项 | 开发环境 | 生产环境 |
|--------|----------|----------|
| 后端端口 | 8080 | 8080 |
| 前端端口 | 8000 | Nginx 静态服务 |
| 数据库 | `host.docker.internal:3306` / `magic-ruoyi` | 生产 MySQL 实例 |
| Redis | `host.docker.internal:6379` | 生产 Redis 实例 |
| 文件存储 | MinIO（可配置） | MinIO / 云存储 |
| Magic API 编辑器 | `/magic/web` | `/magic/web`（需认证） |

---

## 4. 交叉关注点

### 4.1 错误处理

**后端统一异常处理**:

- `GlobalExceptionHandler`（来自 `ruoyi-common-web`）统一捕获未处理异常
- 业务异常使用 `ServiceException`，携带错误码和消息
- 参数校验异常使用 `@Validated` + `ValidatorUtils` 统一处理
- 所有异常响应统一使用 `R<T>` 格式封装

**前端错误处理**:

- Axios 响应拦截器统一处理 HTTP 错误状态码
- 401 自动清除 Token 并跳转登录页
- 业务错误通过 ElMessage 展示友好提示
- 不暴露技术细节给终端用户

### 4.2 日志

**日志框架**: Logback Plus（`logback-plus.xml`）

**日志级别**:

| 环境 | org.dromara | org.fellow99 | org.springframework |
|------|-------------|--------------|---------------------|
| dev | debug | debug | warn |
| prod | warn | info | warn |

**日志输出**:

- 控制台输出: `sys-console.log`
- 错误日志: `sys-error.log`
- 信息日志: `sys-info.log`
- 文件滚动策略: 按天滚动，保留 30 天

**操作日志审计**:

- `@Log` 注解标记需要记录操作日志的接口
- 登录/注销通过 `LogininforEvent` 事件异步记录
- 日志内容: 操作人、操作时间、操作模块、操作类型、请求参数、响应结果、耗时

### 4.3 安全

**认证与授权**:

| 机制 | 实现 |
|------|------|
| JWT 认证 | Sa-Token，token 名称 `Authorization` |
| 并发登录 | 允许（`is-concurrent: true`） |
| Token 共享 | 不共享（`is-share: false`），每次登录生成新 Token |
| 权限校验 | `@SaCheckPermission` 注解 |
| 角色校验 | `@SaCheckRole` 注解 |
| 白名单 | `@SaIgnore` 注解 + `security.excludes` 配置 |

**接口加密**:

- 全局接口加密默认启用（`api-decrypt.enabled: true`）
- RSA 非对称加密: 前端公钥加密请求，后端私钥解密
- 响应加密: 后端公钥加密，前端私钥解密
- 敏感接口必须标注 `@ApiEncrypt`

**XSS 防护**:

- XSS 过滤默认启用（`xss.enabled: true`）
- `XssFilter` + `XssHttpServletRequestWrapper` 过滤请求参数
- `/system/notice` 等富文本路径排除 XSS 过滤

**限流**:

- 登录接口: IP 级别限流，60 秒内最多 20 次
- 验证码接口: 同理限流
- 使用 `@RateLimiter` 注解配置

**防重复提交**:

- `@RepeatSubmit` 注解防止写操作重复提交
- 基于 Token + 时间窗口实现，默认 3 秒内同一请求只处理一次

### 4.4 数据权限

- 数据权限通过角色配置的范围控制（全部数据、本部门、本部门及以下、仅本人、自定义）
- MyBatis-Plus 数据权限拦截器自动在 SQL 中追加权限条件
- `DataPermissionHelper.ignore()` 可临时忽略数据权限

### 4.5 缓存

- Redis 作为统一缓存存储
- Redisson 提供分布式锁和高级数据结构
- 缓存 Key 前缀可配置（`redisson.keyPrefix`）
- 热点数据缓存命中率目标: 90%+

### 4.6 国际化

- 后端: `i18n/messages*.properties` 多语言消息文件
- 前端: `src/lang/` 目录，支持中文（zh_CN）和英文（en_US）
- 错误消息通过 `MessageUtils.message()` 获取国际化文本

---

## 5. 测试方案

### 5.1 后端测试

**单元测试**:

| 工具 | 用途 |
|------|------|
| JUnit 5 | 测试框架 |
| Mockito | Mock 依赖对象 |
| Spring Boot Test | 集成测试 |

**测试策略**:

- Service 层测试使用 Mock 隔离 Mapper 依赖
- Controller 层测试使用 `MockMvc` 模拟 HTTP 请求
- 核心业务逻辑测试覆盖率目标: 80%+
- 打包默认跳过测试（`skipTests=true`），但测试代码必须存在且可运行

**测试目录结构**:

```
magic-ruoyi-admin/src/test/
└── java/org/fellow99/magic/ruoyi/
    ├── controller/          # Controller 层测试
    ├── service/             # Service 层测试
    └── config/              # 配置测试
```

### 5.2 前端测试

| 工具 | 用途 |
|------|------|
| Vitest | 测试框架（已包含在 devDependencies） |
| Vue Test Utils | Vue 组件测试 |

**测试策略**:

- 工具函数（`src/utils/`）编写单元测试
- 公共组件编写组件测试
- 核心页面流程编写集成测试

### 5.3 接口测试

- 使用 Spring Boot Test 编写接口集成测试
- 使用 Postman / Apifox 进行手工接口测试
- magic-api 动态接口通过编辑器内置测试功能验证

---

## 6. 部署策略

### 6.1 开发环境

使用 Docker Compose 启动基础设施服务。

**docker-compose.yml 服务清单**:

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库，初始化 `magic-ruoyi` 数据库 |
| Redis | 6379 | 缓存和会话存储 |
| MinIO | 9000/9001 | 对象存储（API 端口 / 控制台端口） |

**启动命令**:

```bash
# 启动基础设施
docker-compose up -d

# 启动后端
cd magic-ruoyi-admin && mvn spring-boot:run -Pdev

# 启动前端
cd magic-ruoyi-web && npm run dev
```

### 6.2 生产环境

**架构**:

```
                    ┌──────────┐
                    │  Nginx   │
                    │  :80/443 │
                    └────┬─────┘
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
        ┌─────────┐ ┌─────────┐ ┌─────────┐
        │ Admin-1 │ │ Admin-2 │ │ Admin-N │
        │ :8080   │ │ :8080   │ │ :8080   │
        └────┬────┘ └────┬────┘ └────┬────┘
             │           │           │
             └───────────┼───────────┘
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
        ┌─────────┐ ┌─────────┐ ┌─────────┐
        │  MySQL  │ │  Redis  │ │  MinIO  │
        └─────────┘ └─────────┘ └─────────┘
```

**部署要点**:

- Nginx 反向代理前端静态资源和后端 API
- 后端多实例部署，通过 Nginx 负载均衡
- MySQL 主从复制（可选）
- Redis 哨兵模式或集群模式（可选）
- MinIO 分布式模式（可选）

**Nginx 配置示例**:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /opt/magic-ruoyi/web/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://backend_cluster;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # Magic API 编辑器
    location /magic/ {
        proxy_pass http://backend_cluster;
        proxy_set_header Host $host;
    }
}

upstream backend_cluster {
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;
    server 127.0.0.1:8082;
}
```

### 6.3 构建流程

**后端构建**:

```bash
# 清理并打包
mvn clean package -DskipTests

# 产物
magic-ruoyi-admin/target/magic-ruoyi-admin.jar
```

**前端构建**:

```bash
# 安装依赖
npm install

# 生产构建
npm run build:prod

# 产物
magic-ruoyi-web/dist/
```

---

## 7. 模块职责矩阵

| 模块 | 包前缀 | 职责 | 依赖 |
|------|--------|------|------|
| magic-ruoyi-admin | `org.fellow99` | 启动引导、配置、粘合层、认证扩展 | ruoyi-system, ruoyi-generator, magic-api |
| ruoyi-system | `org.dromara.system` | 用户、角色、菜单、部门、租户、字典等系统管理 | ruoyi-common-* |
| ruoyi-generator | `org.dromara.generator` | 代码生成器 | ruoyi-common-* |
| ruoyi-common-web | `org.dromara.common.web` | Web 基础设施（拦截器、过滤器、异常处理） | Spring Boot |
| ruoyi-common-tenant | `org.dromara.common.tenant` | 多租户插件 | MyBatis-Plus |
| ruoyi-common-satoken | `org.dromara.common.satoken` | Sa-Token 集成 | Sa-Token |
| magic-api | `org.ssssssss` | 动态接口引擎 | Spring Boot |

---

## 8. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| RuoYi-Vue-Plus 版本升级导致不兼容 | 高 | 锁定版本号，升级前充分测试 |
| magic-api 脚本性能问题 | 中 | 限制脚本复杂度，监控执行时间 |
| 多租户数据泄露 | 高 | 租户插件自动注入 SQL，代码审查确保无绕过 |
| 前端动态路由加载失败 | 中 | 路由加载失败时回退到默认页面 |
| Redis 单点故障 | 中 | 生产环境使用 Redis 哨兵或集群模式 |
| 接口加密密钥泄露 | 高 | 密钥定期轮换，不硬编码在代码中 |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，整体技术方案定义 |
