# Magic-API 动态接口引擎模块前端页面文档（016-magic-api/pages.md）

> magic-ruoyi Magic-API 模块前端页面定义。
>
> 版本: 1.1.0 | 日期: 2026-04-29

---

## 1. 页面概览

| 页面 | 访问路径 | 描述 | 组件 |
|------|----------|------|------|
| Magic-API 编辑器 | `/magic/web` | 在线接口开发编辑器 | @fellow99/magic-editor |

**说明**: Magic-API 模块使用独立的 npm 包 `@fellow99/magic-editor` 作为 Vue 组件嵌入前端页面。

---

## 2. 页面实现

### 2.1 页面组件结构

```
src/views/magic/web/
└── index.vue          # Magic-API 编辑器页面组件
```

### 2.2 Vue 组件实现（Vue 3 Composition API）

**文件路径**: `src/views/magic/web/index.vue`

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

// 定义配置类型
interface MagicEditorConfig {
  baseURL: string;
  serverURL: string;
  [key: string]: any;
}

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
const config = ref<MagicEditorConfig>({
  baseURL: baseURL.value,      // 编辑器后台服务地址
  serverURL: serverURL.value,  // 接口实际路径前缀
  // 其他配置可参考 magic-editor 文档
});

onMounted(() => {
  console.log('Magic-API Editor initialized');
  console.log('baseURL:', baseURL.value);
  console.log('serverURL:', serverURL.value);
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

### 2.3 配置说明

| 配置项 | 说明 | 开发环境值 | 生产环境值 |
|--------|------|-----------|-----------|
| baseURL | 编辑器后台服务地址 | `/magic/web` | `{VITE_APP_BASE_URL}/magic/web` |
| serverURL | 接口实际路径前缀 | `/magic/api` | `{VITE_APP_BASE_URL}/magic/api` |

**环境变量**:
- `VITE_APP_BASE_URL`: 生产环境的后端服务地址
- `import.meta.env.MODE`: 当前运行模式（development/production）

---

## 3. 编辑器布局

### 3.1 编辑器界面结构

```
┌─────────────────────────────────────────────────────────────────┐
│  [顶部导航栏]  接口 | 函数 | 数据源 | 文档 | 设置                │
├─────────────────────────────────────────────────────────────────┤
│  [左侧面板]                    │  [中间编辑区]                    │
│  ┌──────────────────────────┐ │  ┌────────────────────────────┐ │
│  │ 接口树/函数树/数据源列表  │ │  │ 代码编辑器                  │ │
│  │                          │ │  │                             │ │
│  │ ├─ 用户管理              │ │  │ // 编写 MagicScript 脚本    │ │
│  │ │  ├─ 查询用户列表       │ │  │ var list = db.select(...)   │ │
│  │ │  └─ 新增用户           │ │  │ return list;                │ │
│  │ ├─ 订单管理              │ │  │                             │ │
│  │ │  └─ 创建订单           │ │  │                             │ │
│  │ └─ ...                   │ │  │                             │ │
│  └──────────────────────────┘ │  └────────────────────────────┘ │
│                                │                                 │
│                                │  [底部面板]                      │
│                                │  ┌────────────────────────────┐ │
│                                │  │ 调试控制台 / 响应结果       │ │
│                                │  │                             │ │
│                                │  │ [发送请求] [清除]           │ │
│                                │  └────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 功能区域

#### 3.2.1 接口管理

| 功能 | 说明 |
|------|------|
| 接口树 | 按分组展示所有接口 |
| 新增接口 | 创建新接口，定义路径和方法 |
| 编辑接口 | 编写 MagicScript 脚本 |
| 测试接口 | 内置调试工具发送请求 |
| 发布/下线 | 控制接口可用性 |
| 导入/导出 | 接口配置迁移 |

#### 3.2.2 函数管理

| 功能 | 说明 |
|------|------|
| 函数列表 | 展示所有自定义函数 |
| 新增函数 | 创建可复用函数 |
| 编辑函数 | 编写函数体脚本 |
| 测试函数 | 在线测试函数执行 |

#### 3.2.3 数据源管理

| 功能 | 说明 |
|------|------|
| 数据源列表 | 展示所有配置的数据源 |
| 新增数据源 | 配置数据库连接 |
| 编辑数据源 | 修改连接信息 |
| 测试连接 | 验证数据库连通性 |

#### 3.2.4 在线调试

| 功能 | 说明 |
|------|------|
| 请求参数 | 设置 Query/Body/Header 参数 |
| 发送请求 | 执行接口并获取响应 |
| 响应展示 | 显示响应头、响应体 |
| 日志展示 | 显示执行日志和性能数据 |

### 3.3 编辑器特性

| 特性 | 说明 |
|------|------|
| 语法高亮 | MagicScript 语法高亮 |
| 代码补全 | 自动补全函数名、变量名 |
| 错误提示 | 实时语法错误检测 |
| 快捷键 | 支持常用编辑快捷键 |
| 撤销/重做 | 支持编辑历史回退 |

---

## 4. Vite 配置

### 4.1 代理配置

**文件路径**: `vite.config.ts`

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
      ws: true, // WebSocket 支持（magic-editor 使用 WebSocket 进行调试）
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

### 4.2 WebSocket 配置说明

magic-editor 使用 WebSocket 进行调试通信：

- **连接路径**: `/magic/web/console`
- **功能**: 实时调试日志、断点调试、结果推送
- **配置**: Vite 代理需启用 `ws: true`

---

## 5. 访问控制

### 5.1 编辑器访问

- 编辑器路径: `/magic/web`
- 前端路由: `/magic/web`
- 通过 Sa-Token 进行访问控制
- 仅授权用户可访问编辑器
- 仅管理员角色可编辑接口

### 5.2 接口访问

- 动态接口路径: `/magic/api/{path}`
- 接口权限由编辑器中配置决定
- 可配置是否需要 Token 认证
- 通过 Sa-Token hasPermission() 校验

---

## 6. 菜单配置

### 6.1 菜单路由

| 属性 | 值 |
|------|-----|
| menu_id | 150 |
| menu_name | API编辑 |
| parent_id | 3（系统工具） |
| order_num | 1 |
| path | magic-api |
| component | magic/web/index |
| perms | tool:magic-api:list |
| icon | code |

### 6.2 菜单层级

```
系统工具 (menu_id: 3)
│
├── 代码生成 (menu_id: 115)
│
├── API编辑 (menu_id: 150)
│   ├── 接口查询 (1501)
│   ├── 接口新增 (1502)
│   ├── 接口修改 (1503)
│   ├── 接口删除 (1504)
│   ├── 接口调试 (1505)
│   ├── 数据源管理 (1506)
│   └── 函数管理 (1507)
```

---

## 7. NPM 依赖

### 7.1 包信息

| 属性 | 值 |
|------|-----|
| 包名 | @fellow99/magic-editor |
| 版本 | ^1.7.5 |
| 主入口 | dist/magic-editor.umd.min.js |
| CSS入口 | dist/magic-editor.css |

### 7.2 安装命令

```bash
npm install --save @fellow99/magic-editor
```

### 7.3 package.json 配置

```json
{
  "dependencies": {
    "@fellow99/magic-editor": "^1.7.5"
  }
}
```

---

## 8. 样式配置

### 8.1 容器样式

```css
.magic-api-container {
  width: 100%;
  height: 100vh;  /* 占满整个视口 */
  margin: 0;
  padding: 0;
  overflow: hidden;
}
```

### 8.2 编辑器样式

```css
/* 确保 magic-editor 占满容器 */
.magic-api-container :deep(.magic-editor) {
  width: 100%;
  height: 100%;
}
```

### 8.3 样式说明

- 使用 `100vh` 确保编辑器占满视口高度
- 使用 `:deep()` 深度选择器穿透 scoped 样式
- `overflow: hidden` 防止页面滚动条

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
| 1.1.0 | 2026-04-29 | 补充 Vue 3 Composition API 实现、Vite 配置、菜单配置等详细内容 |