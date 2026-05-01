# Magic-API 动态接口引擎模块技术实现方案（016-magic-api/plan.md）

> magic-ruoyi Magic-API 模块技术实现方案。基于 magic-api 实现动态接口、函数、数据源的在线配置与管理。
>
> 版本: 1.1.0 | 日期: 2026-04-29

---

## 1. Technical Context

### 1.1 模块定位

Magic-API 是一个基于 Java 的接口快速开发框架，允许开发者通过 Web 界面在线编写接口，无需编译即可生效。模块通过 Spring Boot Starter 方式集成到 magic-ruoyi 项目中，提供零代码 API 开发能力。

### 1.2 上游依赖

| 上游模块 | 包前缀 | 说明 |
|----------|--------|------|
| `magic-api-spring-boot-starter` | `org.ssssssss` | Magic-API 核心引擎 |
| `@fellow99/magic-editor` | - | Magic-API 前端编辑器组件（npm包） |
| `magic-api` | `org.ssssssss` | Magic-API 核心库 |
| `magic-script` | `org.ssssssss` | MagicScript 脚本引擎 |

### 1.3 技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 动态接口引擎 | magic-api | 2.2.2 | 在线编写与发布 API |
| 前端编辑器 | @fellow99/magic-editor | 1.7.5 | Vue 组件形式的编辑器 |
| 脚本语言 | MagicScript | - | 专为 API 开发设计的脚本语言 |
| 后端框架 | Spring Boot | 3.5.14 | 运行环境 |
| 认证框架 | Sa-Token | - | 登录认证与权限控制 |

### 1.4 核心约束

- Magic-API 是第三方框架，通过 Maven 依赖引入，不修改其源码
- 使用独立 npm 包 `@fellow99/magic-editor`，排除内置 jar
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
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `magic-api-spring-boot-starter`，排除内置编辑器 jar |
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
| AuthorizationInterceptor | 权限拦截器（可自定义） |
| RequestInterceptor | 请求拦截器（可自定义） |
| ResultProvider | 响应结果处理器（可自定义） |

### 3.2 编辑器访问

**访问路径**: `/magic/web`

**安全控制**:
- 编辑器本身是 Spring MVC 的一个 Controller
- 需要通过 Sa-Token 拦截器保护，防止未授权访问
- 通过自定义 `AuthorizationInterceptor` 实现深度集成

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
| 数据库存储 | 推荐，存储在 `magic_api_file` 表中 |
| 文件存储 | 存储在服务器文件系统 |
| Redis 存储 | 存储在 Redis 中 |

### 3.6 接口路径规则

- 动态接口路径以配置的 `prefix` 为前缀
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
| type | VARCHAR | 赚源类型（api/function/datasource） |
| group_id | VARCHAR | 分组 ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4.2 sys_menu 新增菜单

```sql
-- API编辑菜单（系统工具下级）
menu_id: 150
menu_name: API编辑
parent_id: 3（系统工具）
path: magic-api
component: magic/web/index
perms: tool:magic-api:list
icon: code

-- 子权限按钮
menu_id: 1501-1507
parent_id: 150
 perms: tool:magic-api:{query/add/edit/remove/debug/datasource/function}
```

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
| GET | `/magic/web/api/file/list` | 获取资源列表 |
| POST | `/magic/web/api/file/save` | 保存资源 |
| DELETE | `/magic/web/api/file/delete` | 删除资源 |
| POST | `/magic/web/api/debug` | 调试脚本 |
| GET | `/magic/web/api/version/list` | 获取版本列表 |
| POST | `/magic/web/api/version/rollback` | 版本回滚 |

---

## 6. Implementation Strategy

### 6.1 后端集成

#### 6.1.1 Maven 依赖配置

**文件**: `magic-ruoyi-admin/pom.xml`

```xml
<dependency>
    <groupId>org.ssssssss</groupId>
    <artifactId>magic-api-spring-boot-starter</artifactId>
    <version>${magic-api.version}</version>
    <!-- 排除内置编辑器，使用独立npm包 -->
    <exclusions>
        <exclusion>
            <groupId>org.ssssssss</groupId>
            <artifactId>magic-editor</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#### 6.1.2 application.yml 配置

**文件**: `magic-ruoyi-admin/src/main/resources/application.yml`

添加 magic-api 配置块：

```yaml
--- # magic-api 动态接口引擎配置
magic-api:
  web: /magic/web
  prefix: /magic/api
  response:
    code: 200
    message: 操作成功
    data: data
  resource:
    type: database
    table-name: magic_api_file
    datasource: master
  security:
    username: ${MAGIC_API_USERNAME:}
    password: ${MAGIC_API_PASSWORD:}
  settings:
    allow-path-prefixes:
      - /magic/api/**
  editor-config:
    server-url: http://localhost:8080/
  debug:
    timeout: 60
```

#### 6.1.3 MagicApiConfig.java 创建

**文件**: `magic-ruoyi-admin/src/main/java/org/fellow99/magic/ruoyi/config/MagicApiConfig.java`

实现 Sa-Token 深度集成：

1. **AuthorizationInterceptor**: 编辑器访问权限控制
2. **RequestInterceptor**: 接口执行前权限校验
3. **ResultProvider**: 统一响应格式（与 RuoYi R<T> 格式一致）

### 6.2 前端集成

#### 6.2.1 NPM 依赖安装

**命令**: `npm install --save @fellow99/magic-editor`

#### 6.2.2 Vite 代理配置

**文件**: `magic-ruoyi-web/vite.config.ts`

添加 `/magic` 路径的代理配置：

```typescript
proxy: {
  '/magic': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    ws: true, // WebSocket 支持
  }
}
```

#### 6.2.3 Vue 组件页面创建

**文件**: `magic-ruoyi-web/src/views/magic/web/index.vue`

使用 Vue 3 Composition API：

```vue
<template>
  <div class="magic-api-container">
    <magic-editor :config="config" />
  </div>
</template>

<script setup lang="ts">
import MagicEditor from '@fellow99/magic-editor';
import '@fellow99/magic-editor/dist/magic-editor.css';
// ... 配置逻辑
</script>
```

#### 6.2.4 菜单配置更新

**文件**: `sql/magic-ruoyi.sql`

添加菜单 SQL：

```sql
-- API编辑菜单
insert into sys_menu values('150', 'API编辑', '3', '1', ...);
-- 子权限按钮 1501-1507
```

### 6.3 安全集成

#### 6.3.1 编辑器访问控制

通过自定义 `AuthorizationInterceptor`：

- `allowVisit()`: 使用 `StpUtil.isLogin()` 验证登录状态
- `allowEdit()`: 使用 `StpUtil.hasRole()` 验证管理员角色

#### 6.3.2 接口权限控制

通过自定义 `RequestInterceptor`：

- `preHandle()`: 检查接口配置的权限标识
- 使用 `StpUtil.hasPermission()` 校验权限

#### 6.3.3 响应格式统一

通过自定义 `ResultProvider`：

- 将 Magic-API 的 `JsonBean` 转换为 RuoYi 的 `R<T>` 格式

### 6.4 与 RuoYi 的集成要点

| 成成点 | 说明 |
|--------|------|
| 数据源共享 | Magic-API 默认使用 Spring Boot 主数据源，与 RuoYi 共享 |
| 多数据源 | 通过 Magic-API 的数据源管理功能配置额外数据源 |
| 租户隔离 | 动态接口脚本中需手动处理租户隔离（通过 `TenantHelper`） |
| 日志记录 | 脚本中通过 `log.info()` 记录日志，与 RuoYi 日志系统独立 |
| 权限控制 | 通过自定义拦截器实现 Sa-Token 深度集成 |

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
| 编辑权限校验 | 验证非管理员角色无法编辑接口 |
| 接口权限校验 | 验证无权限用户调用接口返回"权限不足" |
| SQL 注入 | 验证 Magic-API 的参数绑定防止 SQL 注入 |
| 脚本沙箱 | 验证脚本无法执行系统命令或访问文件系统 |

### 7.3 前端测试

| 测试场景 | 测试方法 |
|----------|----------|
| 编辑器组件加载 | 验证 Vue 组件正确渲染 magic-editor |
| WebSocket 连接 | 验证调试功能的 WebSocket 连接正常 |
| 代理配置 | 验证 Vite 代理正确转发请求到后端 |
| 菜单显示 | 验证"系统工具 -> API编辑"菜单正确显示 |

### 7.4 性能测试

| 测试场景 | 测试方法 |
|----------|----------|
| 简单查询性能 | 对比 Magic-API 脚本与 Java 代码的执行时间 |
| 并发请求 | 验证高并发下接口响应时间 |
| 脚本复杂度 | 验证复杂脚本（多步逻辑）的执行性能 |

---

## 8. File Inventory

### 8.1 后端文件

| 文件 | 包路径 | 说明 |
|------|--------|------|
| `MagicApiConfig.java` | `org.fellow99.magic.ruoyi.config` | Magic-API 配置类（Sa-Token集成） |
| `application.yml` | `resources/` | Magic-API 配置块 |
| `pom.xml` | `magic-ruoyi-admin/` | magic-api 依赖 |

### 8.2 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| `index.vue` | `src/views/magic/web/` | Magic-API 编辑器页面组件 |
| `vite.config.ts` | `magic-ruoyi-web/` | Vite 代理配置 |
| `package.json` | `magic-ruoyi-web/` | @fellow99/magic-editor 依赖 |

### 8.3 数据库文件

| 文件 | 路径 | 说明 |
|------|------|------|
| `magic-ruoyi.sql` | `sql/` | sys_menu 菜单配置 |

---

## 9. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 脚本性能问题 | 中 | 限制脚本复杂度，监控执行时间，避免循环查询 |
| 编辑器未授权访问 | 高 | Sa-Token 深度集成，强制登录验证 |
| 动态接口覆盖静态接口 | 高 | 配置 `allow-path-prefixes`，确保不与既有路径冲突 |
| SQL 注入 | 中 | 使用参数绑定（`#{param}`），不拼接 SQL 字符串 |
| 数据源密码泄露 | 高 | 数据源密码加密存储，不明文展示 |
| 脚本执行异常 | 中 | 脚本中捕获异常，返回友好错误信息 |
| WebSocket 连接问题 | 中 | Vite 代理配置 WebSocket 支持，处理连接错误 |

---

## 10. 实现步骤

### Step 1: 后端配置

1. 修改 `magic-ruoyi-admin/pom.xml`，添加 magic-api 依赖并排除内置编辑器
2. 在 `application.yml` 添加 magic-api 配置块
3. 创建 `MagicApiConfig.java` 实现 Sa-Token 集成

### Step 2: 前端配置

1. 安装 `@fellow99/magic-editor` npm 包
2. 修改 `vite.config.ts` 添加 `/magic` 代理
3. 创建 `src/views/magic/web/index.vue` 页面组件

### Step 3: 菜单配置

1. 在 `sql/magic-ruoyi.sql` 添加菜单 SQL
2. 执行 SQL 更新数据库

### Step 4: 测试验证

1. 启动后端服务
2. 启动前端开发服务器
3. 登录系统，访问"系统工具 -> API编辑"菜单
4. 测试接口创建、调试功能

---

## 11. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
| 1.1.0 | 2026-04-29 | 补充后端配置、前端集成、菜单配置等具体实现方案 |