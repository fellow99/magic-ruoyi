# Implementation Plan: 012-Client 客户端管理

**Branch**: `012-client` | **Date**: 2026-04-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/012-client/spec.md`

## Summary

客户端管理模块负责管理系统的 OAuth 客户端配置，定义授权类型、Token 参数、设备类型等安全策略。登录流程中的 Client 校验依赖此模块的数据。客户端配置为系统级数据，排除在租户过滤之外。停用的客户端无法用于登录。模块由 RuoYi-Vue-Plus 上游提供，通过 Maven 依赖引入。

## 1. Technical Context

| 项目 | 值 |
|------|-----|
| Language/Version | Java 21+ |
| Primary Dependencies | Spring Boot 3.5.14, MyBatis-Plus 3.5.16, Sa-Token 1.44.0, RuoYi-Vue-Plus 5.6.0 |
| Storage | MySQL (sys_client 表) |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Target Platform | Linux server, Undertow :8080 |
| Project Type | Web-service (企业级后台管理模块) |
| Performance Goals | 客户端校验 P95 < 10ms（登录流程关键路径），列表查询 P95 < 200ms |
| Constraints | clientId 和 clientKey 唯一，授权类型以 JSON 数组字符串存储，逻辑删除 |
| Scale/Scope | 客户端数量有限（通常 < 20 个），低频写入，每次登录必校验 |

### 前端技术上下文

| 项目 | 值 |
|------|-----|
| Framework | Vue 3.5 + TypeScript 5.9 |
| UI Library | Element Plus 2.11 |
| Build Tool | Vite 6.4 |

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 客户端配置直接存储，授权类型使用 JSON 数组字符串 |
| 约定优于配置 | 合规 | 遵循 `/system/client` 路径约定、`R<T>` 响应格式 |
| 实用优于完美 | 合规 | CRUD + 状态切换，满足客户端管理基本需求 |
| 安全优于便利 | 合规 | 全部接口需 Token 认证，停用的客户端无法登录，clientSecret 脱敏展示 |
| 零样板代码 | 合规 | MyBatis-Plus BaseMapperPlus 提供基础 CRUD，逻辑删除自动处理 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `ruoyi-system`，不复制上游源码 |
| 清晰模块边界 | 合规 | 上游 `org.dromara.system` 包负责实现 |
| 多租户优先 | 合规 | sys_client 为系统级表，在租户配置中显式排除 |
| 前后端分离 | 合规 | 前端独立 API 模块 `@/api/system/client` |
| 严格分层 | 合规 | Controller -> Service -> Mapper -> Database |
| 认证与授权 | 合规 | 客户端配置与 Sa-Token 认证集成，登录时校验 clientId、grantType、status |

## 3. Research Findings

### 3.1 上游实现位置

客户端管理模块由 RuoYi-Vue-Plus 的 `ruoyi-system` 模块提供，关键类位于 `org.dromara.system` 包下。

### 3.2 登录流程中的客户端校验

登录时，`AuthController` 或 `SysLoginService` 会执行以下校验:

1. 按 `clientId` 查询 `sys_client` 表
2. 校验客户端是否存在
3. 校验客户端状态是否为 "0"（正常），停用的客户端拒绝登录
4. 校验请求的 `grantType` 是否在客户端的 `grant_type` 列表中
5. 从客户端配置中获取 `activeTimeout` 和 `timeout`，用于 Sa-Token 的 Token 过期配置

### 3.3 授权类型存储

`grant_type` 字段以 JSON 数组字符串格式存储，如 `["password","sms","email"]`。后端在 VO 层将其解析为 `List<String>` 返回给前端，前端以多选组件展示。

### 3.4 逻辑删除

`sys_client` 表使用 `del_flag` 字段实现逻辑删除（0=存在, 1=删除）。MyBatis-Plus 逻辑删除插件自动处理，查询时自动追加 `WHERE del_flag = '0'`。

### 3.5 租户排除

`sys_client` 在租户配置中显式排除，不受租户过滤影响。所有租户共享同一套客户端配置。

## 4. Data Model

### 4.1 数据库表: sys_client

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | bigint(20) | NOT NULL | - | 主键 ID（雪花算法） |
| client_id | varchar(255) | NOT NULL | - | 客户端 ID（UUID，唯一） |
| client_key | varchar(255) | YES | '' | 客户端 Key（唯一） |
| client_secret | varchar(255) | YES | '' | 客户端秘钥 |
| grant_type | varchar(255) | YES | '' | 授权类型（JSON 数组字符串） |
| device_type | varchar(32) | YES | '' | 设备类型 |
| active_timeout | bigint(20) | YES | 1800 | Token 活跃超时时间（秒） |
| timeout | bigint(20) | YES | 604800 | Token 固定超时时间（秒） |
| status | char(1) | YES | '0' | 状态（0=正常, 1=停用） |
| del_flag | char(1) | YES | '0' | 删除标志（0=存在, 1=删除） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |

**索引**: `client_id` 唯一索引, `client_key` 唯一索引

### 4.2 后端对象

| 对象类型 | 类名 | 用途 |
|----------|------|------|
| Entity | `SysClient` | 数据库实体，继承 `BaseEntity`，含 `delFlag` |
| VO | `ClientVo` | 前端展示数据，`grantType` 解析为 `List<String>` |
| Bo | `ClientBo` | 业务层传输数据 |

### 4.3 前端类型

| 类型 | 用途 |
|------|------|
| `ClientVO` | 客户端返回对象 |
| `ClientForm` | 客户端表单对象，继承 `BaseEntity` |
| `ClientQuery` | 客户端查询对象，继承 `PageQuery` |

### 4.4 数据流转

```
新增: ClientForm -> POST /system/client -> Service 生成 clientId (UUID) -> 插入 sys_client -> R.ok()
修改: ClientForm -> PUT /system/client -> Service 更新 sys_client -> R.ok()
状态切换: {clientId, status} -> PUT /system/client/changeStatus -> Service 按 clientId 更新 status -> R.ok()
删除: ids -> DELETE /system/client/{ids} -> Service 逻辑删除 (del_flag='1') -> R.ok()
登录校验: 登录请求 -> 按 clientId 查询 -> 校验存在 + status + grantType -> 获取 Token 参数
```

## 5. Interface Contracts

### 5.1 API 接口清单

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/client/list` | `system:client:list` | 查询客户端列表（分页） |
| GET | `/system/client/{id}` | `system:client:query` | 查询客户端详情 |
| POST | `/system/client` | `system:client:add` | 新增客户端 |
| PUT | `/system/client` | `system:client:edit` | 修改客户端 |
| DELETE | `/system/client/{ids}` | `system:client:remove` | 删除客户端（支持批量，逻辑删除） |
| PUT | `/system/client/changeStatus` | - | 切换客户端状态 |

### 5.2 前端 API 封装

模块: `@/api/system/client/index.ts`

| 函数 | 方法 | 说明 |
|------|------|------|
| `listClient(query)` | GET | 查询客户端列表 |
| `getClient(id)` | GET | 查询客户端详情 |
| `addClient(data)` | POST | 新增客户端 |
| `updateClient(data)` | PUT | 修改客户端 |
| `delClient(ids)` | DELETE | 删除客户端 |
| `changeStatus(clientId, status)` | PUT | 切换状态 |

### 5.3 响应格式

统一使用 `R<T>` 包装。分页接口返回 `R<TableDataInfo<ClientVo>>`，详情接口返回 `R<ClientVo>`。

## 6. Implementation Strategy

### 6.1 后端实现

本模块由 RuoYi-Vue-Plus 上游提供，实现要点:

- **Controller**: `SysClientController`，位于 `org.dromara.system.controller.system` 包
- **Service**: `ISysClientService` / `SysClientServiceImpl`，封装业务逻辑和登录校验
- **Mapper**: `SysClientMapper`，继承 `BaseMapperPlus<SysClient, ClientVo>`
- **逻辑删除**: 通过 MyBatis-Plus `@TableLogic` 注解实现
- **租户排除**: `sys_client` 在 `tenant.excludes` 配置中显式排除

### 6.2 前端实现

- **页面**: `src/views/system/client/index.vue`
- **API 模块**: `src/api/system/client/index.ts`
- **类型定义**: `src/api/system/client/types.ts`
- **路由**: `/system/client`，动态加载

### 6.3 关键实现细节

1. **clientId 自动生成**: 新增客户端时，后端自动生成 UUID 格式的 clientId
2. **授权类型多选**: 前端使用多选组件（checkbox 或 select multiple），后端以 JSON 数组字符串存储
3. **状态快速切换**: 支持在列表中通过开关组件快速切换启用/停用状态
4. **clientSecret 脱敏**: 列表展示时对 clientSecret 进行脱敏处理（如 `****`）
5. **逻辑删除**: 删除操作仅标记 `del_flag='1'`，不物理删除数据
6. **登录集成**: 客户端配置在登录流程中被 `SysLoginService` 或 `AuthController` 读取，用于校验和 Token 参数配置

### 6.4 项目结构

```
specs/012-client/
├── spec.md          # 功能规格
├── plan.md          # 本文件
├── api.md           # API 接口定义
├── data-model.md    # 数据模型
├── pages.md         # 前端页面定义
└── user-stories.md  # 用户故事

后端（上游 ruoyi-system 模块）:
org.dromara.system.controller.system/
└── SysClientController.java
org.dromara.system.service/
├── ISysClientService.java
└── impl/SysClientServiceImpl.java
org.dromara.system.mapper/
└── SysClientMapper.java
org.dromara.system.domain/
├── SysClient.java
├── vo/ClientVo.java
└── bo/ClientBo.java

前端（magic-ruoyi-web）:
src/views/system/client/
└── index.vue
src/api/system/client/
├── index.ts
└── types.ts
```

## 7. Testing Considerations

### 7.1 后端测试

| 测试类型 | 测试内容 |
|----------|----------|
| Service 单元测试 | clientId 生成逻辑、授权类型 JSON 解析、状态切换、逻辑删除 |
| Controller 集成测试 | CRUD 接口请求/响应、权限校验、状态切换接口 |
| 登录集成测试 | 停用客户端拒绝登录、授权类型不匹配拒绝登录、Token 参数正确应用 |

### 7.2 前端测试

| 测试类型 | 测试内容 |
|----------|----------|
| 组件测试 | 表单校验（clientKey、clientSecret、授权类型、设备类型必填）、授权类型多选渲染 |
| 交互测试 | 搜索、分页、新增/修改对话框、状态切换开关、删除确认 |

### 7.3 手工测试场景

1. 新增客户端，填写所有必填字段，验证 clientId 自动生成
2. 修改客户端授权类型，验证多选组件正确回显
3. 切换客户端状态为停用，尝试使用该客户端登录，验证被拒绝
4. 删除客户端，验证列表不再显示（逻辑删除）
5. 按 clientKey 模糊搜索，验证搜索结果正确
6. 验证 clientSecret 在列表中脱敏展示

## 8. File Inventory

### 8.1 规格文档

| 文件 | 路径 | 状态 |
|------|------|------|
| spec.md | `specs/012-client/spec.md` | 已完成 |
| plan.md | `specs/012-client/plan.md` | 本文件 |
| api.md | `specs/012-client/api.md` | 已完成 |
| data-model.md | `specs/012-client/data-model.md` | 已完成 |
| pages.md | `specs/012-client/pages.md` | 已完成 |
| user-stories.md | `specs/012-client/user-stories.md` | 已完成 |

### 8.2 后端文件（上游 ruoyi-system）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| SysClientController.java | `org.dromara.system.controller.system` | 控制器入口 |
| ISysClientService.java | `org.dromara.system.service` | 服务接口 |
| SysClientServiceImpl.java | `org.dromara.system.service.impl` | 服务实现 |
| SysClientMapper.java | `org.dromara.system.mapper` | 数据访问 |
| SysClient.java | `org.dromara.system.domain` | 实体类 |
| ClientVo.java | `org.dromara.system.domain.vo` | 视图对象 |
| ClientBo.java | `org.dromara.system.domain.bo` | 业务对象 |

### 8.3 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| index.vue | `src/views/system/client/index.vue` | 客户端管理页面 |
| index.ts | `src/api/system/client/index.ts` | API 函数封装 |
| types.ts | `src/api/system/client/types.ts` | TypeScript 类型定义 |

### 8.4 数据库

| 表名 | 说明 |
|------|------|
| sys_client | 客户端管理表 |

## Complexity Tracking

无宪法违规项。本模块完全由上游 RuoYi-Vue-Plus 提供，本项目仅通过 Maven 依赖引入。客户端校验逻辑与认证流程集成，复杂度由上游框架承担。
