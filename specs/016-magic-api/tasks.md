# Magic-API 动态接口引擎模块实现任务清单（016-magic-api/tasks.md）

> magic-ruoyi Magic-API 模块实现任务清单。
>
> 版本: 1.1.0 | 日期: 2026-04-29

---

## 任务依赖图

```
[T1-后端依赖配置] ──┬──> [T2-后端YAML配置] ──> [T3-后端Java配置类]
                   │
                   │    [T4-前端NPM依赖] ──> [T5-前端Vite配置] ──> [T6-前端Vue组件]
                   │                                              │
                   │                                              └──> [T7-菜单SQL配置]
                   │
                   └──────────────────────────────────────────────────> [T8-集成测试]
```

---

## Phase 1: 后端配置

### Task T1: 后端 Maven 依赖配置

**状态**: Pending
**优先级**: P1（高）
**依赖**: 无

**任务描述**:
修改 `magic-ruoyi-admin/pom.xml`，配置 magic-api 依赖并排除内置编辑器 jar。

**详细步骤**:
1. 确认 `magic-api.version` 在父 pom.xml 中已定义（2.2.2）
2. 在 `magic-ruoyi-admin/pom.xml` 的 `<dependencies>` 中添加 magic-api-spring-boot-starter 依赖
3. 使用 `<exclusions>` 排除 magic-editor jar

**验收标准**:
- Maven 依赖正确添加
- 排除配置生效（无 magic-editor jar）
- `mvn dependency:tree` 显示正确依赖树

**产出文件**:
- `magic-ruoyi-admin/pom.xml`（修改）

---

### Task T2: 后端 application.yml 配置

**状态**: Pending
**优先级**: P1（高）
**依赖**: T1

**任务描述**:
在 `application.yml` 中添加 magic-api 配置块，配置编辑器路径、API前缀、数据库存储、认证等。

**详细步骤**:
1. 在 `application.yml` 文末添加 `--- # magic-api 动态接口引擎配置` 分隔符
2. 配置 `web: /magic/web` 编辑器路径
3. 配置 `prefix: /magic/api` 接口前缀
4. 配置 `resource.type: database` 数据库存储
5. 配置 `resource.table-name: magic_api_file` 存储表名
6. 配置 `security` 认证参数（使用环境变量）
7. 配置 `response` 响应格式与 RuoYi 统一

**验收标准**:
- YAML 配置语法正确
- 配置参数符合 magic-api 规范
- 启动服务无配置错误

**产出文件**:
- `magic-ruoyi-admin/src/main/resources/application.yml`（修改）

---

### Task T3: 后端 MagicApiConfig.java 创建

**状态**: Pending
**优先级**: P1（高）
**依赖**: T2

**任务描述**:
创建 `MagicApiConfig.java` 配置类，实现 Sa-Token 深度集成。

**详细步骤**:
1. 在 `org.fellow99.magic.ruoyi.config` 包下创建 `MagicApiConfig.java`
2. 添加 `@Configuration` 注解
3. 创建 `AuthorizationInterceptor` Bean：
   - `allowVisit()` 使用 `StpUtil.isLogin()` 验证登录
   - `allowEdit()` 使用 `StpUtil.hasRole()` 验证管理员
4. 创建 `RequestInterceptor` Bean：
   - `preHandle()` 检查接口权限配置
   - 使用 `StpUtil.hasPermission()` 校验权限
5. 创建 `ResultProvider` Bean：
   - 将 JsonBean 转换为 R<T> 格式

**验收标准**:
- Java 类正确创建
- Sa-Token API 正确调用
- 无编译错误
- Bean 正确注册

**产出文件**:
- `magic-ruoyi-admin/src/main/java/org/fellow99/magic/ruoyi/config/MagicApiConfig.java`（新建）

---

## Phase 2: 前端配置

### Task T4: 前端 NPM 依赖安装

**状态**: Pending
**优先级**: P1（高）
**依赖**: 无

**任务描述**:
在 `magic-ruoyi-web` 项目中安装 `@fellow99/magic-editor` npm 包。

**详细步骤**:
1. 进入 `magic-ruoyi-web` 目录
2. 执行 `npm install --save @fellow99/magic-editor`
3. 确认 package.json 中依赖正确添加
4. 确认 node_modules 中包正确安装

**验收标准**:
- npm install 成功执行
- package.json 显示正确依赖版本（^1.7.5）
- 无安装错误

**产出文件**:
- `magic-ruoyi-web/package.json`（修改）
- `magic-ruoyi-web/package-lock.json`（自动更新）

---

### Task T5: 前端 Vite 代理配置

**状态**: Pending
**优先级**: P1（高）
**依赖**: T4

**任务描述**:
在 `vite.config.ts` 中添加 `/magic` 路径的代理配置，支持 WebSocket。

**详细步骤**:
1. 在 `server.proxy` 对象中添加 `/magic` 代理配置
2. 配置 `target: 'http://localhost:8080'`
3. 配置 `changeOrigin: true`
4. 配置 `ws: true` 支持 WebSocket
5. 添加错误处理和 WebSocket 连接配置

**验收标准**:
- Vite 配置语法正确
- 代理路径正确配置
- WebSocket 支持启用

**产出文件**:
- `magic-ruoyi-web/vite.config.ts`（修改）

---

### Task T6: 前端 Vue 组件页面创建

**状态**: Pending
**优先级**: P1（高）
**依赖**: T5

**任务描述**:
创建 `src/views/magic/web/index.vue` 页面组件，使用 Vue 3 Composition API。

**详细步骤**:
1. 创建 `src/views/magic/web/` 目录
2. 创建 `index.vue` 文件
3. 使用 `<script setup lang="ts">` 组合式 API
4. 导入 `MagicEditor` 组件和 CSS
5. 配置 `baseURL` 和 `serverURL` 使用环境变量
6. 添加容器样式确保编辑器占满屏幕

**验收标准**:
- Vue 文件正确创建
- TypeScript 类型正确
- CSS 样式正确引入
- 配置参数正确

**产出文件**:
- `magic-ruoyi-web/src/views/magic/web/index.vue`（新建）

---

### Task T7: 菜单 SQL 配置更新

**状态**: Pending
**优先级**: P1（高）
**依赖**: T6

**任务描述**:
在 `sql/magic-ruoyi.sql` 中添加 sys_menu 菜单配置。

**详细步骤**:
1. 在 `sql/magic-ruoyi.sql` 中找到"系统工具"菜单（menu_id: 3）
2. 添加"API编辑"二级菜单（menu_id: 150）
3. 添加子权限按钮（menu_id: 1501-1507）
4. 配置正确的路径、组件、权限标识

**验收标准**:
- SQL 语法正确
- 菜单层级正确（系统工具 -> API编辑）
- 权限标识格式正确（tool:magic-api:*）

**产出文件**:
- `sql/magic-ruoyi.sql`（修改）

---

## Phase 3: 集成测试

### Task T8: 集成测试与验证

**状态**: Pending
**优先级**: P2（中）
**依赖**: T3, T7

**任务描述**:
完成所有配置后，进行集成测试验证功能正常。

**详细步骤**:
1. 启动后端服务 `mvn spring-boot:run`
2. 启动前端服务 `npm run dev`
3. 登录系统（admin/admin123）
4. 执行菜单 SQL 更新数据库
5. 访问"系统工具 -> API编辑"菜单
6. 测试创建简单接口
7. 测试接口调试功能
8. 测试权限控制（非管理员无法编辑）

**验收标准**:
- 后端启动无错误
- 前端启动无错误
- 编辑器页面正确渲染
- 接口创建功能正常
- 调试功能正常
- 权限控制生效

**产出文件**:
- 无（验证任务）

---

## 任务清单汇总

| ID | 任务名称 | 状态 | 优先级 | 依赖 | 产出文件 |
|----|----------|------|--------|------|----------|
| T1 | 后端 Maven 依赖配置 | Pending | P1 | - | pom.xml |
| T2 | 后端 application.yml 配置 | Pending | P1 | T1 | application.yml |
| T3 | 后端 MagicApiConfig.java 创建 | Pending | P1 | T2 | MagicApiConfig.java |
| T4 | 前端 NPM 依赖安装 | Pending | P1 | - | package.json |
| T5 | 前端 Vite 代理配置 | Pending | P1 | T4 | vite.config.ts |
| T6 | 前端 Vue 组件页面创建 | Pending | P1 | T5 | index.vue |
| T7 | 菜单 SQL 配置更新 | Pending | P1 | T6 | magic-ruoyi.sql |
| T8 | 集成测试与验证 | Pending | P2 | T3, T7 | - |

---

## 执行顺序建议

**并行执行组**:
- Group A: T1, T4（无依赖，可并行）
- Group B: T2（依赖 T1）
- Group C: T5（依赖 T4）
- Group D: T3（依赖 T2）
- Group E: T6（依赖 T5）
- Group F: T7（依赖 T6）
- Group G: T8（依赖 T3, T7）

**推荐执行顺序**:
1. 并行执行 T1 和 T4
2. 完成 T1 后执行 T2
3. 完成 T4 后执行 T5
4. 完成 T2 后执行 T3
5. 完成 T5 后执行 T6
6. 完成 T6 后执行 T7
7. 完成 T3 和 T7 后执行 T8

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本 |
| 1.1.0 | 2026-04-29 | 完善任务清单，添加详细步骤和验收标准 |