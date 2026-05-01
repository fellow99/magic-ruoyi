# Magic-API 动态接口引擎模块规格文档（016-magic-api/spec.md）

> magic-ruoyi Magic-API 模块。基于 magic-api 实现动态接口、函数、数据源的在线配置与管理。
>
> 版本: 1.1.0 | 日期: 2026-04-29

---

## 1. 模块概述

Magic-API 是一个基于 Java 的接口快速开发框架，允许开发者通过 Web 界面在线编写接口，无需编译即可生效。模块提供接口管理、函数管理、数据源管理、在线调试等核心能力。

### 1.1 技术栈

| 组件 | 技术选型 |
|------|----------|
| 动态接口引擎 | magic-api 2.2.2 |
| 前端编辑器 | @fellow99/magic-editor 1.7.5 |
| 编辑器入口 | `/magic/web` |
| 脚本语言 | MagicScript |
| 后端框架 | Spring Boot 3.5.14 |
| 认证框架 | Sa-Token |

### 1.2 核心功能

- **接口管理**: 在线创建、编辑、测试 HTTP 接口
- **函数管理**: 自定义函数库，供接口脚本调用
- **数据源管理**: 多数据源配置与管理
- **在线调试**: 内置接口测试工具
- **版本管理**: 接口版本控制与回滚
- **权限控制**: 接口访问权限配置

### 1.3 模块结构

```
Magic-API
└── 编辑器入口 (/magic/web)
    ├── 接口管理
    ├── 函数管理
    ├── 数据源管理
    └── 在线调试
```

---

## 2. 后端集成配置

### 2.1 Maven 依赖配置

在 `magic-ruoyi-admin/pom.xml` 中配置 magic-api 依赖：

```xml
<!-- magic-api 动态接口引擎 -->
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

**说明**: 排除内置 magic-editor jar，使用独立的 npm 包 `@fellow99/magic-editor`。

### 2.2 application.yml 配置

在 `magic-ruoyi-admin/src/main/resources/application.yml` 中添加 magic-api 配置：

```yaml
--- # magic-api 动态接口引擎配置
magic-api:
  # 编辑器访问路径
  web: /magic/web
  # 接口执行前缀
  prefix: /magic/api
  # 响应配置（与RuoYi响应格式统一）
  response:
    code: 200
    message: 操作成功
    data: data
  # 资源存储配置
  resource:
    # 存储方式：database（数据库）
    type: database
    # 存储表名
    table-name: magic_api_file
    # 使用主数据源
    datasource: master
  # 安全配置（Sa-Token深度集成）
  security:
    # 编辑器访问需要登录认证
    username: ${MAGIC_API_USERNAME:}
    password: ${MAGIC_API_PASSWORD:}
  # 接口配置
  settings:
    # 允许的路径前缀
    allow-path-prefixes:
      - /magic/api/**
  # 编辑器配置
  editor-config:
    # 接口实际路径前缀
    server-url: http://localhost:8080/
  # 调试配置
  debug:
    timeout: 60

# 安全排除配置（magic-api路径由Sa-Token控制）
security:
  excludes:
    # magic-api 编辑器静态资源（需Sa-Token认证才能访问）
    # 注意：/magic/web 和 /magic/api 路径不排除，由Sa-Token拦截器控制
    - /magic/web/**/*.css
    - /magic/web/**/*.js
    - /magic/web/**/*.html
    - /magic/web/**/*.ico
    - /magic/web/**/*.png
    - /magic/web/**/*.jpg
```

### 2.3 MagicApiConfig.java 配置类

创建 `MagicApiConfig.java` 实现 Sa-Token 深度集成：

**文件路径**: `magic-ruoyi-admin/src/main/java/org/fellow99/magic/ruoyi/config/MagicApiConfig.java`

```java
package org.fellow99.magic.ruoyi.config;

import cn.dev33.satoken.stp.StpUtil;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;
import org.ssssssss.magicapi.core.model.ApiInfo;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.dromara.common.core.domain.R;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Magic-API 配置类
 * 实现 Sa-Token 深度集成，确保编辑器和接口的安全控制
 */
@Configuration
public class MagicApiConfig {

    /**
     * 权限拦截器 - Sa-Token 集成
     * 确保编辑器和管理接口需要登录认证
     */
    @Bean
    public AuthorizationInterceptor authorizationInterceptor() {
        return new AuthorizationInterceptor() {
            @Override
            public boolean allowVisit(String username, String password) {
                // 使用 Sa-Token 验证用户身份
                // 只有已登录的用户才能访问编辑器
                return StpUtil.isLogin();
            }

            @Override
            public boolean allowEdit(String username, String password) {
                // 只有管理员角色才能编辑接口
                return StpUtil.hasRole("admin") || StpUtil.hasRole("superadmin");
            }
        };
    }

    /**
     * 请求拦截器 - 权限校验
     * 在接口执行前校验用户权限
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public Object preHandle(ApiInfo info, String path) {
                // 检查接口是否需要权限
                String permission = info.getPermission();
                if (permission != null && !permission.isEmpty()) {
                    // 使用 Sa-Token 校验权限
                    if (!StpUtil.hasPermission(permission)) {
                        return R.fail("权限不足，无法访问该接口");
                    }
                }
                return null;
            }

            @Override
            public Object postHandle(ApiInfo info, String path, Object result) {
                // 返回结果处理
                return result;
            }
        };
    }

    /**
     * 响应结果处理器 - 统一响应格式
     * 使 magic-api 返回格式与 RuoYi 保持一致
     */
    @Bean
    public ResultProvider resultProvider() {
        return (result) -> {
            if (result instanceof JsonBean) {
                JsonBean jsonBean = (JsonBean) result;
                return R.ok(jsonBean.getData())
                    .setCode(jsonBean.getCode())
                    .setMsg(jsonBean.getMessage());
            }
            return R.ok(result);
        };
    }
}
```

### 2.4 安全配置说明

magic-api 的访问接口和管理接口必须在主体框架的安全配置控制下：

| 路径 | 安全控制 | 说明 |
|------|----------|------|
| `/magic/web` | Sa-Token 登录验证 | 编辑器入口，必须登录后访问 |
| `/magic/web/**` | Sa-Token 角色验证 | 编辑器操作，需要管理员角色 |
| `/magic/api/**` | Sa-Token 权限验证 | 动态接口，根据配置校验权限 |

---

## 3. 前端集成配置

### 3.1 NPM 依赖

在 `magic-ruoyi-web/package.json` 中添加依赖：

```json
{
  "dependencies": {
    "@fellow99/magic-editor": "^1.7.5"
  }
}
```

### 3.2 Vite 配置

在 `magic-ruoyi-web/vite.config.ts` 中添加 `/magic/web` 路由代理：

```typescript
server: {
  host: '0.0.0.0',
  port: Number(env.VITE_APP_PORT),
  open: true,
  proxy: {
    // 现有代理配置
    [env.VITE_APP_BASE_API]: {
      target: 'http://localhost:8080',
      changeOrigin: true,
      ws: true,
      rewrite: (path) => path.replace(new RegExp('^' + env.VITE_APP_BASE_API), '')
    },
    // magic-api 编辑器代理
    '/magic': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      ws: true,
      // WebSocket 支持（magic-editor 使用 WebSocket 进行调试）
      configure: (proxy, _options) => {
        proxy.on('error', (err, _req, _res) => {
          console.log('magic-api proxy error:', err);
        });
        proxy.on('proxyReqWs', (proxyReq, req, socket) => {
          socket.on('error', (err) => {
            console.log('WebSocket error:', err);
          });
        });
      }
    }
  }
}
```

### 3.3 Vue 组件页面实现

**文件路径**: `magic-ruoyi-web/src/views/magic/web/index.vue`

使用 Vue 3 组合式 API（Composition API）：

```vue
<template>
  <div class="magic-api-container">
    <magic-editor v-if="config" :config="config" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import MagicEditor from '@fellow99/magic-editor';
import '@fellow99/magic-editor/dist/magic-editor.css';

// 获取当前运行环境
const baseURL = computed(() => {
  // 开发环境使用代理，生产环境使用实际地址
  if (import.meta.env.MODE === 'development') {
    return '/magic/web';
  }
  return import.meta.env.VITE_APP_BASE_URL + '/magic/web';
});

const serverURL = computed(() => {
  // 接口实际路径前缀
  if (import.meta.env.MODE === 'development') {
    return '/magic/api';
  }
  return import.meta.env.VITE_APP_BASE_URL + '/magic/api';
});

// magic-editor 配置
const config = ref({
  baseURL: baseURL.value,      // 编辑器后台服务地址
  serverURL: serverURL.value,  // 接口实际路径前缀
  // 其他配置可参考 magic-editor 文档
});

onMounted(() => {
  console.log('Magic-API Editor initialized');
});
</script>

<style scoped>
.magic-api-container {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

/* 确保 magic-editor 占满容器 */
.magic-api-container :deep(.magic-editor) {
  width: 100%;
  height: 100%;
}
</style>
```

### 3.4 路由配置

在 `magic-ruoyi-web/src/router/index.ts` 的 `dynamicRoutes` 中添加路由（或通过菜单动态加载）：

```typescript
// 通过菜单动态加载，无需在 constantRoutes 中添加
// 菜单配置中的 component 字段指向: 'magic/web/index'
```

---

## 4. 菜单配置

### 4.1 sys_menu 菜单条目

在 `sql/magic-ruoyi.sql` 中添加菜单配置：

```sql
-- Magic-API 编辑器菜单（放在"系统工具"下级）
-- 系统工具的 menu_id = 3

-- 二级菜单：API编辑
insert into sys_menu values('150',  'API编辑',     '3',   '1', 'magic-api',        'magic/web/index',         '', 1, 0, 'C', '0', '0', 'tool:magic-api:list',        'code',          103, 1, sysdate(), null, null, 'Magic-API动态接口编辑器菜单');

-- API编辑按钮权限
insert into sys_menu values('1501', '接口查询',    '150', '1', '#', '', '', 1, 0, 'F', '0', '0', 'tool:magic-api:query',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1502', '接口新增',    '150', '2', '#', '', '', 1, 0, 'F', '0', '0', 'tool:magic-api:add',            '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1503', '接口修改',    '150', '3', '#', '', '', 1, 0, 'F', '0', '0', 'tool:magic-api:edit',           '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1504', '接口删除',    '150', '4', '#', '', '', 1, 0, 'F', '0', '0', 'tool:magic-api:remove',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1505', '接口调试',    '150', '5', '#', '', '', 1, 0, 'F', '0', '0', 'tool:magic-api:debug',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1506', '数据源管理',  '150', '6', '#', '', '', 1, 0, 'F', '0', '0', 'tool:magic-api:datasource',     '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1507', '函数管理',    '150', '7', '#', '', '', 1, 0, 'F', '0', '0', 'tool:magic-api:function',       '#', 103, 1, sysdate(), null, null, '');
```

### 4.2 菜单层级说明

```
系统工具 (menu_id: 3)
│
├── 代码生成 (menu_id: 115)
│
├── API编辑 (menu_id: 150)  ← 新增
│   ├── 接口查询 (1501)
│   ├── 接口新增 (1502)
│   ├── 接口修改 (1503)
│   ├── 接口删除 (1504)
│   ├── 接口调试 (1505)
│   ├── 数据源管理 (1506)
│   └── 函数管理 (1507)
```

---

## 5. 接口管理

### 5.1 功能

- **创建接口**: 定义请求路径、请求方法、请求参数、响应格式
- **编辑接口**: 在线编写 MagicScript 脚本实现业务逻辑
- **测试接口**: 内置测试工具，支持发送请求并查看响应
- **发布/下线**: 控制接口是否可用
- **导入/导出**: 接口配置的导入导出

### 5.2 接口配置项

| 配置项 | 说明 |
|------|------|
| 路径 | 接口请求路径，支持路径参数 |
| 请求方法 | GET/POST/PUT/DELETE 等 |
| 请求参数 | Query/Body/Header/Path 参数 |
| 响应格式 | JSON/XML 等 |
| 权限配置 | 接口访问权限标识（与Sa-Token集成） |
| 描述 | 接口说明文档 |

---

## 6. 函数管理

### 6.1 功能

- **创建函数**: 定义可复用的函数
- **函数分类**: 按功能分组管理
- **函数测试**: 在线测试函数执行结果

### 6.2 内置函数

| 函数 | 说明 |
|------|------|
| db.select() | 数据库查询 |
| db.insert() | 数据库插入 |
| db.update() | 数据库更新 |
| db.delete() | 数据库删除 |
| http.get() | HTTP GET 请求 |
| http.post() | HTTP POST 请求 |
| log.info() | 日志记录 |

---

## 7. 数据源管理

### 7.1 功能

- **添加数据源**: 配置数据库连接信息
- **测试连接**: 验证数据源可用性
- **切换数据源**: 接口脚本中指定使用的数据源

### 7.2 数据源配置项

| 配置项 | 说明 |
|------|------|
| 名称 | 数据源标识 |
| JDBC URL | 数据库连接地址 |
| 用户名 | 数据库用户名 |
| 密码 | 数据库密码 |
| 驱动类 | JDBC 驱动类名 |

---

## 8. 在线调试

### 8.1 功能

- 发送 HTTP 请求测试接口
- 查看请求参数、响应头、响应体
- 查看执行日志和性能数据

---

## 9. 权限控制

Magic-API 编辑器本身需要登录访问，接口权限通过以下方式控制：

| 控制方式 | 说明 |
|----------|------|
| 编辑器访问权限 | 通过 Sa-Token 控制编辑器访问 |
| 编辑权限 | 只有管理员角色可编辑接口 |
| 接口权限 | 通过权限标识控制接口调用 |
| 数据源权限 | 通过数据源配置控制数据库访问 |

---

## 10. 安全考虑

- 编辑器仅允许授权人员访问（Sa-Token 登录验证）
- 编辑操作仅限管理员角色
- 接口脚本执行在沙箱环境中
- 敏感操作（删除接口、修改数据源）需要确认
- 接口执行日志记录所有操作

---

## 11. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
| 1.1.0 | 2026-04-29 | 补充后端配置、前端集成、菜单配置等具体实现细节 |