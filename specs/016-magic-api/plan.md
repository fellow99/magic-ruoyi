# Magic-API 动态接口引擎模块技术实现方案（016-magic-api/plan.md）

> magic-ruoyi Magic-API 模块技术实现方案。基于 magic-api 实现动态接口、函数、数据源的在线配置与管理。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. Technical Context

### 1.1 模块定位

Magic-API 是一个基于 Java 的接口快速开发框架，允许开发者通过 Web 界面在线编写接口，无需编译即可生效。模块通过 Spring Boot Starter 方式集成到 magic-ruoyi 项目中，提供零代码 API 开发能力。

### 1.2 上游依赖

| 上游模块 | 包前缀 | 说明 |
|----------|--------|------|
| `magic-api-spring-boot-starter` | `org.ssssssss` | Magic-API 核心引擎 |
| `magic-api` | `org.ssssssss` | Magic-API 核心库 |
| `magic-script` | `org.ssssssss` | MagicScript 脚本引擎 |

### 1.3 技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 动态接口引擎 | magic-api | 2.2.2 | 在线编写与发布 API |
| 脚本语言 | MagicScript | - | 专为 API 开发设计的脚本语言 |
| 编辑器 | magic-api Web UI | - | 内置 Web 编辑器 |
| 后端框架 | Spring Boot | 3.5.14 | 运行环境 |

### 1.4 核心约束

- Magic-API 是第三方框架，通过 Maven 依赖引入，不修改其源码
- 编辑器入口为 `/magic/web`，需通过 Sa-Token 控制访问权限
- 动态接口路径不得与既有静态接口（如 `/system/*`、`/monitor/*`）冲突
- 接口脚本执行在沙箱环境中，不得直接访问系统资源
- 编辑器仅允许授权人员访问，不得开放匿名访问

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | Magic-API 提供简洁的脚本编写方式，一行 SQL 即可成为 API |
| 约定优于配置 | 合规 | 使用 Magic-API 默认配置，仅在路径和权限方面做必要定制 |
| 实用优于完美 | 合规 | 先使用基础功能（接口编写、测试），再逐步使用高级功能（函数、数据源） |
| 安全优于便利 | 合规 | 编辑器通过 Sa-Token 控制访问，接口脚本执行在沙箱中 |
| 零样板代码 | 合规 | Magic-API 的核心价值就是消除 CRUD 样板代码 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `magic-api-spring-boot-starter` |
| 清晰模块边界 | 合规 | Magic-API 归属 `org.ssssssss` 包，与自定义 `org.fellow99` 严格隔离 |
| 渐进式复杂度 | 合规 | 脚本从单行 SQL 到多步逻辑逐步演进 |

---

## 3. Research Findings

### 3.1 Magic-API 架构

Magic-API 通过 Spring Boot Starter 方式集成，核心组件包括:

| 组件 | 说明 |
|------|------|
| MagicAPIAutoConfiguration | 自动配置类，注册引擎和编辑器 |
| MagicResourceController | 资源管理（接口、函数、数据源） |
| MagicDebugController | 脚本调试 |
| MagicScriptEngine | MagicScript 脚本执行引擎 |
| MagicDynamicDataSource | 动态数据源管理 |

### 3.2 编辑器访问

**访问路径**: `/magic/web`

**安全控制**:
- 编辑器本身是 Spring MVC 的一个 Controller
- 需要通过 Sa-Token 拦截器保护，防止未授权访问
- 可通过配置项设置编辑器访问密码

### 3.3 MagicScript 脚本语言

MagicScript 是 Magic-API 专用的脚本语言，支持:

| 特性 | 说明 |
|------|------|
| 变量定义 | `var name = "value"` |
| 条件语句 | `if/else` |
| 循环语句 | `for/while` |
| 函数调用 | `db.select("SELECT * FROM user")` |
| HTTP 请求 | `http.get("https://api.example.com")` |
| 日志记录 | `log.info("message")` |
| JSON 处理 | `json.parse()`, `json.stringify()` |

### 3.4 内置函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `db.select(sql)` | 数据库查询 | `db.select("SELECT * FROM sys_user")` |
| `db.selectOne(sql)` | 查询单条记录 | `db.selectOne("SELECT * FROM sys_user WHERE user_id = 1")` |
| `db.insert(sql)` | 数据库插入 | `db.insert("INSERT INTO sys_user ...")` |
| `db.update(sql)` | 数据库更新 | `db.update("UPDATE sys_user SET ...")` |
| `db.delete(sql)` | 数据库删除 | `db.delete("DELETE FROM sys_user WHERE ...")` |
| `db.page(page, size, sql)` | 分页查询 | `db.page(1, 10, "SELECT * FROM sys_user")` |
| `http.get(url)` | HTTP GET 请求 | `http.get("https://api.example.com/data")` |
| `http.post(url, body)` | HTTP POST 请求 | `http.post("https://api.example.com/data", {"key": "value"})` |
| `log.info(msg)` | 日志记录 | `log.info("接口被调用")` |
| `log.error(msg)` | 错误日志 | `log.error("发生错误")` |
| `json.parse(str)` | JSON 解析 | `json.parse('{"name": "test"}')` |
| `json.stringify(obj)` | JSON 序列化 | `json.stringify({"name": "test"})` |

### 3.5 数据存储

Magic-API 的接口、函数、数据源配置存储方式可配置:

| 存储方式 | 说明 |
|----------|------|
| 数据库存储 | 默认方式，存储在 `magic_api_*` 表中 |
| 文件存储 | 存储在服务器文件系统 |
| Redis 存储 | 存储在 Redis 中 |

### 3.6 接口路径规则

- 动态接口路径以配置的 `path` 为前缀
- 默认前缀为 `/magic/api`
- 不得与既有静态接口路径冲突
- 支持路径参数: `/magic/api/user/{id}`

---

## 4. Data Model

### 4.1 Magic-API 内置存储表

Magic-API 使用内置表存储配置，表名以 `magic_api_` 为前缀。

| 表名 | 说明 |
|------|------|
| `magic_api_file` | 接口/函数/数据源配置存储表 |

**核心字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR | 资源 ID |
| name | VARCHAR | 资源名称 |
| path | VARCHAR | 接口路径 |
| method | VARCHAR | 请求方法（GET/POST/PUT/DELETE） |
| script | TEXT | 脚本内容 |
| type | VARCHAR | 资源类型（api/function/datasource） |
| group_id | VARCHAR | 分组 ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4.2 接口配置项

| 配置项 | 类型 | 说明 |
|--------|------|------|
| 路径 | String | 接口请求路径，支持路径参数 |
| 请求方法 | String | GET/POST/PUT/DELETE/PATCH |
| 请求参数 | Object | Query/Body/Header/Path 参数定义 |
| 响应格式 | String | JSON/XML |
| 权限配置 | String | 接口访问权限标识 |
| 描述 | String | 接口说明文档 |
| 脚本 | String | MagicScript 脚本内容 |

### 4.3 数据源配置项

| 配置项 | 类型 | 说明 |
|--------|------|------|
| 名称 | String | 数据源标识 |
| JDBC URL | String | 数据库连接地址 |
| 用户名 | String | 数据库用户名 |
| 密码 | String | 数据库密码 |
| 驱动类 | String | JDBC 驱动类名 |

---

## 5. Interface Contracts

### 5.1 Magic-API 编辑器

| 路径 | 说明 |
|------|------|
| `/magic/web` | Web 编辑器入口 |
| `/magic/web/**` | 编辑器静态资源和 API |

### 5.2 动态接口

| 路径模式 | 说明 |
|----------|------|
| `/magic/api/**` | 动态接口默认路径前缀 |

动态接口的具体路径由用户在编辑器中定义，例如:
- `GET /magic/api/user/list` - 查询用户列表
- `POST /magic/api/user/create` - 创建用户
- `GET /magic/api/user/{id}` - 查询用户详情

### 5.3 编辑器管理 API（Magic-API 内置）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/magic/api/file/list` | 获取资源列表 |
| POST | `/magic/api/file/save` | 保存资源 |
| DELETE | `/magic/api/file/delete` | 删除资源 |
| POST | `/magic/api/debug` | 调试脚本 |
| GET | `/magic/api/version/list` | 获取版本列表 |
| POST | `/magic/api/version/rollback` | 版本回滚 |

---

## 6. Implementation Strategy

### 6.1 后端集成

Magic-API 通过 Spring Boot Starter 方式集成到 magic-ruoyi-admin 模块。

**依赖配置**（`magic-ruoyi-admin/pom.xml`）:

```xml
<dependency>
    <groupId>org.ssssssss</groupId>
    <artifactId>magic-api-spring-boot-starter</artifactId>
    <version>${magic-api.version}</version>
</dependency>
```

**配置项**（`application.yml`）:

```yaml
magic-api:
  web: /magic/web                    # 编辑器访问路径
  path: /magic/api                   # 接口前缀
  resource:
    type: db                         # 存储方式（db/file/redis）
    tableName: magic_api_file        # 存储表名
    datasource:                      # 数据源配置（可选，默认使用主数据源）
  prefix: magic-api                  # 缓存前缀
  response:                          # 响应配置
    code: 0                          # 成功状态码
    message: success                 # 成功消息
    data: data                       # 数据字段
  auth:                              # 认证配置
    enabled: true                    # 是否启用编辑器认证
    username: admin                  # 编辑器用户名
    password: ${MAGIC_API_PASSWORD}  # 编辑器密码（环境变量）
  allow-path-prefixes:               # 允许的路径前缀
    - /magic/api/**
```

### 6.2 安全集成

**编辑器访问控制**:

Magic-API 编辑器自带简单的用户名密码认证。如需与 Sa-Token 集成，可通过以下方式:

1. 使用 Magic-API 自带的 `auth.enabled` 配置启用基础认证
2. 通过 Spring Security 拦截器或 Sa-Token 拦截器保护 `/magic/web` 路径
3. 在 `application.yml` 中配置 `security.excludes` 排除 Magic-API 路径，使用其自带认证

**接口访问控制**:

动态接口的权限控制通过以下方式实现:

1. 在脚本中使用 `request.getHeader("Authorization")` 获取 Token
2. 通过 Sa-Token API 验证 Token 有效性
3. 在脚本中实现权限校验逻辑

### 6.3 前端集成

Magic-API 编辑器是内置的 Web UI，无需额外前端开发。

**访问方式**:
- 开发环境: `http://localhost:8080/magic/web`
- 生产环境: `http://your-domain/magic/web`（需 Nginx 代理）

**Vite 代理配置**（`vite.config.ts`）:

```typescript
server: {
  proxy: {
    '/magic': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

### 6.4 与 RuoYi 的集成要点

| 集成点 | 说明 |
|--------|------|
| 数据源共享 | Magic-API 默认使用 Spring Boot 主数据源，与 RuoYi 共享 |
| 多数据源 | 通过 Magic-API 的数据源管理功能配置额外数据源 |
| 租户隔离 | 动态接口脚本中需手动处理租户隔离（通过 `TenantHelper`） |
| 日志记录 | 脚本中通过 `log.info()` 记录日志，与 RuoYi 日志系统独立 |
| 权限控制 | 动态接口需自行实现权限校验，不自动继承 RuoYi 权限体系 |

### 6.5 配置项

```yaml
# application.yml 相关配置
magic-api:
  web: /magic/web
  path: /magic/api
  resource:
    type: db
    tableName: magic_api_file
  auth:
    enabled: true
    username: admin
    password: ${MAGIC_API_PASSWORD}

# 安全排除（如使用 Magic-API 自带认证）
security:
  excludes:
    - /magic/web/**
    - /magic/api/**
```

---

## 7. Testing Considerations

### 7.1 接口测试

| 测试场景 | 测试方法 |
|----------|----------|
| 简单查询接口 | 编写 `return db.select("SELECT * FROM sys_user")`，验证返回数据 |
| 带参数接口 | 编写 `return db.select("SELECT * FROM sys_user WHERE user_name = #{name}")`，验证参数传递 |
| 分页接口 | 编写 `return db.page(page, size, "SELECT * FROM sys_user")`，验证分页结果 |
| POST 接口 | 编写插入逻辑，验证请求体解析和数据写入 |
| 路径参数 | 编写 `/magic/api/user/{id}`，验证路径参数提取 |

### 7.2 安全测试

| 测试场景 | 测试方法 |
|----------|----------|
| 编辑器未授权访问 | 验证未登录时访问 `/magic/web` 被拒绝 |
| 接口匿名访问 | 验证动态接口不得开放匿名访问 |
| SQL 注入 | 验证 Magic-API 的参数绑定防止 SQL 注入 |
| 脚本沙箱 | 验证脚本无法执行系统命令或访问文件系统 |

### 7.3 性能测试

| 测试场景 | 测试方法 |
|----------|----------|
| 简单查询性能 | 对比 Magic-API 脚本与 Java 代码的执行时间 |
| 并发请求 | 验证高并发下接口响应时间 |
| 脚本复杂度 | 验证复杂脚本（多步逻辑）的执行性能 |

---

## 8. File Inventory

### 8.1 后端文件（Magic-API 框架）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| `MagicAPIAutoConfiguration` | `org.ssssssss.magicapi` | 自动配置类 |
| `MagicResourceController` | `org.ssssssss.magicapi` | 资源管理 Controller |
| `MagicDebugController` | `org.ssssssss.magicapi` | 调试 Controller |
| `MagicScriptEngine` | `org.ssssssss.script` | 脚本执行引擎 |
| `MagicDynamicDataSource` | `org.ssssssss.magicapi` | 动态数据源管理 |

### 8.2 配置文件

| 文件 | 路径 | 说明 |
|------|------|------|
| `application.yml` | `magic-ruoyi-admin/src/main/resources/` | Magic-API 配置 |
| `pom.xml` | `magic-ruoyi-admin/` | magic-api 依赖 |

### 8.3 前端文件

Magic-API 编辑器为内置 Web UI，无需额外前端文件。

| 资源 | 路径 | 说明 |
|------|------|------|
| 编辑器入口 | `/magic/web` | Web 编辑器 |
| 编辑器静态资源 | `/magic/web/**` | CSS/JS/图片等 |

### 8.4 数据库表

| 表名 | 说明 | 创建方式 |
|------|------|----------|
| `magic_api_file` | 接口/函数/数据源配置存储表 | Magic-API 自动创建 |

---

## 9. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 脚本性能问题 | 中 | 限制脚本复杂度，监控执行时间，避免循环查询 |
| 编辑器未授权访问 | 高 | 启用 `auth.enabled`，设置强密码，生产环境通过 Nginx 限制 IP |
| 动态接口覆盖静态接口 | 高 | 配置 `allow-path-prefixes`，确保不与既有路径冲突 |
| SQL 注入 | 中 | 使用参数绑定（`#{param}`），不拼接 SQL 字符串 |
| 数据源密码泄露 | 高 | 数据源密码加密存储，不明文展示 |
| 脚本执行异常 | 中 | 脚本中捕获异常，返回友好错误信息 |

---

## 10. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
