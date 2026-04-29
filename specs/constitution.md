# magic-ruoyi 项目宪法

> 本文档定义 magic-ruoyi 项目的核心原则、架构约束和编码标准。
> 所有贡献者必须遵守本宪法。任何偏离需经过充分讨论并记录理由。
>
> 版本: 1.0.0 | 最后更新: 2026-04-28

---

## 1. 核心价值观

### 1.1 简单优于复杂

能用一行解决的不用两行，能用标准库的不引入新依赖。架构决策优先考虑可理解性而非炫技。新成员应在一天内读懂核心流程。

### 1.2 约定优于配置

遵循 RuoYi-Vue-Plus 和 magic-api 的共同约定。目录结构、命名规范、响应格式、异常处理全部采用框架默认约定，仅在业务确有需要时才覆盖。

### 1.3 实用优于完美

先交付可用功能，再逐步优化。不为了抽象而抽象，不为了设计模式而设计模式。代码解决实际问题才有价值。

### 1.4 安全优于便利

认证、授权、加密、限流、XSS 防护、防重复提交是默认开启的横切能力。任何功能不得以"方便"为由绕过安全边界。

### 1.5 零样板代码

能自动生成的不手写，能复用的不重复。利用 MyBatis-Plus 的代码生成器、magic-api 的可视化编排、MapStruct 的对象映射，将样板代码降至最低。

### 1.6 渐进式复杂度

简单场景保持简单，复杂场景允许扩展。magic-api 脚本从单行 SQL 到多步逻辑逐步演进，前端组件从基础表单到复杂交互逐步增强。

---

## 2. 架构原则

### 2.1 依赖整合，非复制

- 通过 Maven 依赖引入上游框架（RuoYi-Vue-Plus、magic-api），**绝不复制上游源码**
- 自定义代码使用独立包名 `org.fellow99`，与上游 `org.dromara` 和 `org.ssssssss` 严格隔离
- `@ComponentScan` 显式声明扫描范围，不依赖隐式扫描

### 2.2 清晰模块边界

| 模块 | 职责 | 包前缀 |
|------|------|--------|
| magic-ruoyi-admin | 启动引导、配置、粘合层 | `org.fellow99` |
| RuoYi-Vue-Plus 模块 | 系统管理、工作流、代码生成、监控 | `org.dromara` |
| magic-api 引擎 | 可视化 API 构建、动态 REST | `org.ssssssss` |

自定义模块只承担启动、配置、认证扩展、租户扩展、验证码扩展等整合职责。系统管理、工作流、代码生成等基础能力直接复用上游模块。

### 2.3 插件化设计

- 认证策略通过 `IAuthStrategy` 接口 + Spring Bean 命名注册实现可插拔
- 新功能以独立模块或独立 Bean 形式接入，不修改既有代码
- magic-api 脚本作为运行时插件，支持热加载、版本管理、权限隔离

### 2.4 多租户优先

- 多租户不是可选功能，是架构的一等公民
- 所有业务表默认启用租户隔离（`tenant_id` 字段 + MyBatis-Plus 租户插件）
- 系统级表（`sys_menu`、`sys_tenant`、`sys_tenant_package` 等）显式排除租户过滤
- 租户切换通过 `TenantHelper.dynamic(tenantId, () -> {...})` 实现，不手动设置上下文
- 默认租户 ID 为 `000000`

### 2.5 前后端分离

- 前端独立开发服务器（Vite :8000），后端独立服务（Undertow :8080）
- 通过 Vite proxy 代理 API 请求，开发环境不依赖 Nginx
- 前端通过 JWT token 认证，不依赖 Session
- 前后端通过 OpenAPI 契约保持一致

---

## 3. 开发流程原则

### 3.1 源码事实优先

所有规格、计划、API、数据模型文档必须以当前源码、SQL、POM、package.json 为事实来源。不确定内容必须显式标注，不得猜测。

### 3.2 契约先行

- 后端 API 变更必须同步更新前端 API 客户端
- 动态接口（magic-api）不得覆盖既有 RuoYi 路径
- 权限标识（`perms`）与后端 `@SaCheckPermission` 注解保持一致

### 3.3 可构建可验证

- 后端以 Maven Reactor 作为构建入口，`mvn clean package` 必须通过
- 前端以 Vite 脚本作为构建入口，`npm run build` 必须通过
- 打包默认跳过测试（`skipTests=true`），但测试代码必须存在且可运行
- 文档必须记录准备步骤、构建入口和关键配置

### 3.4 即时反馈

- 开发环境启用热重载：后端 Spring DevTools，前端 Vite HMR
- magic-api 脚本保存即生效，无需重启服务
- 前端 ESLint + Prettier 在保存时自动格式化

### 3.5 分支策略

- `main` 分支始终可构建、可部署
- 功能开发在独立分支进行，完成后合并
- 不直接在 `main` 上提交代码

---

## 4. 代码规范

### 4.1 后端 Java 规范

#### 4.1.1 基础约定

- Java 版本: 21+
- 类名: `PascalCase`（`AuthController`、`SysLoginService`）
- 方法名: `camelCase`（`checkTenant`、`buildLoginUser`）
- 常量: `UPPER_SNAKE_CASE`（`LOGIN_FAIL`、`CAPTCHA_CODE_KEY`）
- 包名: 全小写，点分隔（`org.fellow99.magic.ruoyi.auth`）

#### 4.1.2 注解规范

- 控制器: `@RestController` + `@RequestMapping`
- 服务类: `@Service` + 命名 Bean（如 `@Service("password" + IAuthStrategy.BASE_NAME)`）
- 日志: `@Slf4j`（Lombok）
- 参数校验: `@Validated` + `ValidatorUtils.validate()`
- 权限: `@SaCheckPermission("module:action")`
- 限流: `@RateLimiter(time = 60, count = 20)`
- 加密: `@ApiEncrypt`（敏感接口必须标注）
- 跳过认证: `@SaIgnore`（仅限白名单接口）

#### 4.1.3 响应封装

- 所有 API 响应统一使用 `R<T>` 泛型包装
- 成功: `R.ok(data)` 或 `R.ok("消息")`
- 失败: `R.fail("错误信息")`
- 分页: `R.ok(TableDataInfo<T>)`

#### 4.1.4 异常处理

- 业务异常使用 `ServiceException`，不直接抛 `RuntimeException`
- 密码错误次数限制: 最多 5 次，锁定 10 分钟
- 分布式锁使用 Lock4j 防止并发问题
- 异常日志使用 `log.info()` 记录关键信息，不输出完整堆栈到前端

### 4.2 前端 TypeScript 规范

#### 4.2.1 基础约定

- TypeScript 优先: 所有 `.vue` 文件使用 `<script lang="ts">`
- 禁止使用 `any` 类型，无法确定时使用 `unknown` 并添加注释
- 组件名: `PascalCase`（`UserDialog.vue`、`DeptTree.vue`）
- 文件命名: `kebab-case`（`user-info.ts`、`dept-tree.vue`）
- API 函数名: `camelCase`，以动作开头（`getUserList`、`deleteUser`）

#### 4.2.2 Vue 3 约定

- Composition API 优先: 使用 `<script setup>` 语法
- 响应式数据: `ref` 用于基本类型，`reactive` 用于对象
- 组件通信: props/emit 优先，Pinia 用于跨组件状态
- 生命周期: 使用 `onMounted`、`onUnmounted` 等组合式 API

#### 4.2.3 组件化设计

- 每个组件职责单一，不超过 300 行
- 可复用组件放在 `src/components/` 目录
- 页面级组件放在 `src/views/` 目录，按功能域子目录组织
- 表单封装: 使用 `useForm` 组合式函数统一处理表单逻辑

#### 4.2.4 API 请求规范

- 使用 Axios 封装（`@/utils/request`）
- 每个功能域独立 API 模块（`src/api/system/user.ts`）
- 请求函数必须定义 TypeScript 接口（`UserVO`、`UserQuery`）
- 支持 RSA 加密请求和解密响应
- 支持自动重试登录（`isRelogin` 标志）

#### 4.2.5 状态管理

- Pinia 为唯一状态管理方案
- 每个 Store 职责单一（`userStore`、`permissionStore`、`settingsStore`）
- Store 中只存放状态和同步/异步操作，不存放 UI 逻辑

---

## 5. 分层规范

### 5.1 严格分层

```
Controller → Service → Mapper → Database
```

- **Controller 层**: 仅负责请求接收、参数校验、响应封装。不包含任何业务逻辑。
- **Service 层**: 封装全部业务逻辑。可调用多个 Mapper 或其他 Service。
- **Mapper 层**: 仅负责数据访问。复杂 SQL 写在 XML 中，简单查询使用 MyBatis-Plus 内置方法。
- **禁止跨层调用**: Controller 不得直接调用 Mapper，Service 不得直接操作 HTTP 请求。

### 5.2 对象转换规范

| 对象类型 | 后缀 | 用途 | 所在包 |
|----------|------|------|--------|
| View Object | `Vo` | 前端展示数据 | `domain/vo/` |
| Business Object | `Bo` | 业务层传输数据 | `domain/bo/` |
| Entity | 无后缀 | 数据库实体 | `domain/` |
| Query Object | `Query` | 查询参数 | `domain/vo/` |

- 使用 MapStruct（mapstruct-plus）进行对象转换，不手写 `set/get` 链
- `Vo` 和 `Bo` 不得互相依赖，各自独立定义
- Entity 仅存在于 Mapper 层和 Service 层内部

### 5.3 Controller 规范

- 每个 Controller 对应一个业务模块
- `@RequestMapping` 使用模块前缀（如 `/system/user`）
- RESTful 风格: `GET` 查询、`POST` 新增、`PUT` 修改、`DELETE` 删除
- 导出接口使用独立路径（如 `/system/user/export`）

### 5.4 Service 规范

- 接口定义在 `service/` 目录，实现在 `service/impl/` 目录
- 接口以 `I` 开头（`ISysUserService`），实现以 `Impl` 结尾（`SysUserServiceImpl`）
- 复杂业务逻辑拆分为私有方法，单一方法不超过 50 行

### 5.5 Mapper 规范

- Mapper 接口继承 `BaseMapperPlus<Entity, Vo>`
- 包扫描路径: `org.dromara.**.mapper` 和 `org.fellow99.**.mapper`
- 复杂 SQL 写在 `resources/mapper/` 下的 XML 文件中
- 禁止在 Mapper 中编写业务逻辑

---

## 6. 安全原则

### 6.1 认证与授权

- 使用 Sa-Token 进行 JWT 认证，token 名称为 `Authorization`
- 允许同一账号并发登录（`is-concurrent: true`）
- 不共享 token（`is-share: false`），每次登录生成新 token
- 所有非白名单接口必须通过 `@SaCheckPermission` 或 `@SaCheckRole` 校验
- 动态接口（magic-api）必须配置访问权限，不得开放匿名访问

### 6.2 接口加密

- 全局接口加密默认启用（`api-decrypt.enabled: true`）
- RSA 非对称加密: 前端公钥加密请求，后端私钥解密；后端公钥加密响应，前端私钥解密
- 敏感接口（登录、注册、密码修改）必须使用 `@ApiEncrypt` 注解
- 密钥对定期轮换，不得硬编码在代码中

### 6.3 验证码

- 验证码默认启用，类型为数学计算（MATH）
- 验证码存储于 Redis，过期自动失效
- 图形验证码使用 `WaveAndCircleCaptcha`（波浪+圆圈干扰）
- 验证码校验失败不返回具体错误原因，统一返回"验证码错误"

### 6.4 限流

- 登录接口: IP 级别限流，60 秒内最多 20 次
- 短信/邮箱验证码: 手机号/邮箱级别限流，60 秒内最多 1 次
- 限流配置集中管理，不得散落在代码中

### 6.5 XSS 防护

- XSS 过滤默认启用
- `/system/notice` 等富文本路径排除 XSS 过滤
- 用户输入在前端展示时必须转义

### 6.6 防重复提交

- 写操作接口使用 `@RepeatSubmit` 注解防止重复提交
- 基于 token + 时间窗口实现，默认 3 秒内同一请求只处理一次

### 6.7 审计日志

- 所有写操作记录操作日志（`@Log` 注解）
- 登录/注销操作通过 `LogininforEvent` 事件记录
- 日志包含: 操作人、操作时间、操作模块、操作类型、请求参数、响应结果、耗时

---

## 7. 质量原则

### 7.1 可测试性

- 业务逻辑必须可单元测试，不依赖外部服务
- 使用 `spring-boot-starter-test` 编写测试
- Service 层测试使用 Mock 隔离 Mapper 依赖
- 测试覆盖率目标: 核心业务逻辑 80%+

### 7.2 代码审查

- 所有合并到 `main` 的代码必须经过审查
- 审查重点: 安全边界是否被绕过、分层是否被破坏、命名是否清晰
- 不审查功能正确性（由测试保证），审查代码质量和架构一致性

### 7.3 错误处理

- 所有异常必须有明确的处理策略，不得吞掉异常
- 前端错误提示友好，不暴露技术细节
- 后端错误日志详细，便于排查问题
- 全局异常处理器统一处理未捕获异常

### 7.4 性能要求

- 接口响应时间: P95 < 500ms（不含外部服务调用）
- 数据库查询必须使用索引，禁止全表扫描
- 列表查询必须分页，禁止一次性返回全部数据
- 缓存命中率目标: 热点数据 90%+

### 7.5 可观测性

- 关键业务操作记录日志
- 接口耗时通过 AOP 或拦截器统计
- 异常率、慢查询、缓存命中率等指标可监控
- 生产环境启用 Spring Boot Admin 监控

### 7.6 文档要求

- 公共 API 必须有 JavaDoc / TSDoc 注释
- 复杂业务逻辑必须有注释说明"为什么"而非"做什么"
- 架构变更必须更新相关文档（ARCHITECTURE.md、TECH.md 等）
- README.md 必须包含构建步骤和关键配置说明

---

## 附录 A: 技术栈速查

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.5.14 |
| 父框架 | RuoYi-Vue-Plus | 5.6.0 |
| 语言 | Java | 21+ |
| ORM | MyBatis-Plus | - |
| 数据库 | MySQL | - |
| 缓存 | Redis + Redisson | - |
| 认证 | Sa-Token | - |
| API 构建 | magic-api | 2.2.2 |
| Web 服务器 | Undertow | - |

### 前端

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.5.x |
| 语言 | TypeScript | 5.9.x |
| 构建 | Vite | 6.4.x |
| UI | Element Plus | 2.11.x |
| 状态 | Pinia | 3.0.x |
| 表格 | vxe-table | 4.17.x |

---

## 附录 B: 宪法修订流程

本宪法的修订需满足以下条件:

1. 提出修订理由和影响范围
2. 在团队内讨论并达成共识
3. 更新本文档并标注修订日期
4. 通知所有贡献者

未经修订流程，不得随意违反本宪法中的任何条款。
