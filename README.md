# magic-ruoyi（魔法积木）

> 企业级应用快速开发平台，融合 RuoYi-Vue-Plus 与 magic-api

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen.svg)]()
[![Vue](https://img.shields.io/badge/Vue-3.5-34495e.svg)]()
[![JDK](https://img.shields.io/badge/JDK-17%2F21-orange.svg)]()
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)]()

## 项目简介

magic-ruoyi（魔法积木）是一个企业级应用快速开发平台，将 RuoYi-Vue-Plus 企业级开发框架与 magic-api 动态接口引擎融合为一体。通过 Maven 聚合工程实现级联编译，前后端分离架构，开箱即用。

- **后端**：`org.fellow99:magic-ruoyi-admin`，基于 Spring Boot 3.5.14，JDK 17/21
- **前端**：`magic-ruoyi-web`，基于 Vue 3 + TypeScript + Element Plus

## 核心特性

| 特性 | 说明 |
|------|------|
| 企业级框架 | 继承 RuoYi-Vue-Plus 全部能力：多租户、权限管理、系统监控、工作流、代码生成 |
| 动态接口引擎 | 集成 magic-api，支持零代码在线开发 HTTP API，可视化脚本编辑 |
| Maven 聚合工程 | 级联编译，统一版本管理，模块化构建 |
| 前后端分离 | 后端 Spring Boot REST API，前端 Vue 3 SPA，独立部署 |
| 安全体系 | Sa-Token 认证授权、接口加密、XSS 防护、防重复提交、接口限流 |
| 多租户 | 原生支持多租户隔离，租户级数据、权限、配置 |
| 实时监控 | Spring Boot Admin、Actuator、SSE/WebSocket 实时通信、SnailJob 定时任务 |
| 工作流 | Warm-Flow 工作流引擎，流程定义、实例管理、任务审批 |

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.14 | 应用框架 |
| JDK | 17 / 21 | 运行环境 |
| Maven | 3.x | 构建工具 |
| MyBatis-Plus | - | ORM 框架 |
| Sa-Token | - | 认证授权 |
| magic-api | 2.2.2 | 动态接口引擎 |
| MySQL | 8.x | 关系数据库 |
| Redis | - | 缓存 |
| Spring Boot Admin | - | 应用监控 |
| Warm-Flow | - | 工作流引擎 |
| SnailJob | - | 分布式任务调度 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.22 | 前端框架 |
| TypeScript | 5.9 | 类型系统 |
| Vite | 6.4 | 构建工具 |
| Element Plus | 2.11.7 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.13 | HTTP 客户端 |
| UnoCSS | 66.5 | 原子化 CSS |
| VueUse | 13.9 | 组合式工具库 |
| ECharts | 5.6 | 数据可视化 |
| vxe-table | 4.17 | 高级表格 |

## 快速开始

### 环境要求

- JDK 17 或 21
- Maven 3.6+
- Node.js >= 20.15.0
- npm >= 8.19.0
- MySQL 8.x
- Redis

### 后端启动

```bash
# 1. 克隆项目
git clone <repository-url>
cd magic-ruoyi

# 2. 初始化数据库
# 导入 sql/ 目录下的 SQL 脚本到 MySQL

# 3. 修改配置
# 编辑 magic-ruoyi-admin/src/main/resources/application.yml
# 配置数据库连接、Redis 等

# 4. Maven 编译（级联编译所有模块）
mvn clean install -DskipTests

# 5. 启动后端
cd magic-ruoyi-admin
mvn spring-boot:run
```

后端默认启动端口：`8080`

### 前端启动

```bash
# 1. 进入前端目录
cd magic-ruoyi-web

# 2. 安装依赖
npm install

# 3. 配置环境变量
# 编辑 .env.development，配置后端 API 地址

# 4. 启动开发服务器
npm run dev
```

前端默认访问地址：`http://localhost:8000`

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | admin123 |

## 目录结构

```
magic-ruoyi/
├── pom.xml                      # Maven 聚合根工程（magic-ruoyi-modules）
├── magic-ruoyi-admin/           # 后端主模块（Spring Boot 应用）
│   ├── pom.xml                  # 模块依赖：RuoYi 组件 + magic-api
│   ├── src/main/java/           # Java 源码
│   ├── src/main/resources/      # 配置文件
│   └── deps/                    # 依赖覆盖
├── magic-ruoyi-web/             # 前端工程（Vue 3 + TypeScript）
│   ├── package.json             # 前端依赖
│   ├── vite.config.ts           # Vite 构建配置
│   ├── tsconfig.json            # TypeScript 配置
│   ├── src/                     # 前端源码
│   └── .env.*                   # 环境配置
├── sql/                         # 数据库初始化脚本
├── specs/                # 规格文档
└── README.md                    # 项目说明
```

### 模块说明

| 模块 | 说明 |
|------|------|
| `magic-ruoyi-modules`（根） | Maven 聚合工程，groupId=`org.fellow99`，统一管理版本和子模块 |
| `magic-ruoyi-admin` | Spring Boot 启动模块，web 服务入口，聚合 RuoYi 各组件和 magic-api |
| `magic-ruoyi-web` | 前端管理控制台，Vue 3 SPA 应用 |

## 相关链接

- [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus) - 上游企业级开发框架
- [magic-api](https://www.ssssssss.org/) - 动态接口引擎
- [Spring Boot](https://spring.io/projects/spring-boot) - 应用框架
- [Vue 3](https://vuejs.org/) - 前端框架
- [Element Plus](https://element-plus.org/) - UI 组件库
