# 系统监控模块规格文档（013-monitor/spec.md）

> magic-ruoyi 系统监控模块。定义在线用户、登录日志、操作日志、缓存监控功能。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 模块概述

系统监控模块提供系统运行状态的可视化监控，包括在线用户管理、登录日志审计、操作日志追踪和 Redis 缓存状态查看。模块包含 4 个子页面。

### 1.1 技术栈

| 组件 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot + MyBatis-Plus |
| 前端框架 | Vue 3.5 + Element Plus |
| 会话存储 | Redis（Sa-Token） |
| 日志存储 | MySQL（sys_logininfor, sys_oper_log） |
| 缓存监控 | Redis INFO 命令 |

### 1.2 核心功能

- 在线用户管理（查看、强制踢出）
- 登录日志（查询、删除、清空、解锁）
- 操作日志（查询、删除、清空、详情查看）
- 缓存监控（Redis 状态、缓存列表、清理缓存）

### 1.3 模块结构

```
系统监控
├── 在线用户 (online/index.vue)       → /monitor/online
├── 登录日志 (logininfor/index.vue)   → /monitor/logininfor
├── 操作日志 (operlog/index.vue)      → /monitor/operlog
└── 缓存监控 (cache/index.vue)        → /monitor/cache
```

---

## 2. 在线用户管理

### 2.1 功能描述

查看当前系统中所有在线用户会话，支持按用户名和 IP 地址筛选，支持强制踢出指定用户。

### 2.2 列表字段

| 字段 | 说明 |
|------|------|
| 会话编号 | tokenId |
| 部门名称 | deptName |
| 用户名 | userName |
| 登录 IP | ipaddr |
| 登录地点 | loginLocation |
| 浏览器 | browser |
| 操作系统 | os |
| 登录时间 | loginTime |

### 2.3 操作

- **强制踢出**: 删除指定用户的 Token 会话

---

## 3. 登录日志

### 3.1 功能描述

记录用户登录成功和失败的日志，支持查询、删除、清空和解锁用户。

### 3.2 列表字段

| 字段 | 说明 |
|------|------|
| 访问 ID | infoId |
| 用户名称 | userName |
| 登录状态 | status（0=成功, 1=失败） |
| 登录地址 | ipaddr |
| 登录地点 | loginLocation |
| 浏览器 | browser |
| 操作系统 | os |
| 提示消息 | msg |
| 登录时间 | loginTime |

### 3.3 操作

- **删除**: 删除选中的登录日志
- **清空**: 清空全部登录日志
- **解锁**: 解锁因登录失败被锁定的用户

---

## 4. 操作日志

### 4.1 功能描述

记录用户在系统中的操作行为，支持按操作 IP、模块名称、操作人、业务类型、状态筛选。

### 4.2 列表字段

| 字段 | 说明 |
|------|------|
| 日志编号 | operId |
| 系统模块 | title |
| 操作类型 | businessType |
| 操作人员 | operName |
| 所属部门 | deptName |
| 请求 URL | operUrl |
| 操作地址 | operIp |
| 操作地点 | operLocation |
| 请求参数 | operParam |
| 返回参数 | jsonResult |
| 操作状态 | status |
| 错误消息 | errorMsg |
| 操作时间 | operTime |
| 消耗时间 | costTime |

### 4.3 操作

- **详情**: 查看操作日志详细信息（弹窗展示）
- **删除**: 删除选中的操作日志
- **清空**: 清空全部操作日志

---

## 5. 缓存监控

### 5.1 功能描述

查看 Redis 缓存的运行状态，包括命令统计、数据库大小、服务器信息，支持按缓存名称和键名浏览、查看缓存内容、清理缓存。

### 5.2 展示内容

- **命令统计**: Redis 各命令执行次数（柱状图）
- **数据库大小**: Key 的总数
- **服务器信息**: Redis 版本、运行时间、内存使用等

### 5.3 操作

- **查看缓存名称列表**: 列出所有缓存前缀
- **查看键名列表**: 列出指定前缀下的所有 Key
- **查看缓存内容**: 查看指定 Key 的值
- **清理指定名称缓存**: 删除指定前缀的所有 Key
- **清理指定键名缓存**: 删除指定 Key
- **清理全部缓存**: 清空 Redis 所有数据

---

## 6. 权限控制

| 权限标识 | 功能 | 页面 |
|----------|------|------|
| `monitor:online:list` | 查看在线用户 | 在线用户 |
| `monitor:online:forceLogout` | 强制踢出 | 在线用户 |
| `monitor:logininfor:list` | 查看登录日志 | 登录日志 |
| `monitor:logininfor:remove` | 删除登录日志 | 登录日志 |
| `monitor:logininfor:unlock` | 解锁用户 | 登录日志 |
| `monitor:operlog:list` | 查看操作日志 | 操作日志 |
| `monitor:operlog:remove` | 删除操作日志 | 操作日志 |
| `monitor:cache:list` | 查看缓存 | 缓存监控 |

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
