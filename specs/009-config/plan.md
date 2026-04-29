# Implementation Plan: 009-Config 参数配置

**Branch**: `009-config` | **Date**: 2026-04-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/009-config/spec.md`

## Summary

参数配置模块提供系统运行时动态参数的 CRUD 管理，支持按参数键名快速查询和更新参数值。参数数据缓存在 Redis 中，新增、修改、删除操作自动清除对应缓存，并提供手动刷新全部缓存的接口。模块属于 RuoYi-Vue-Plus 上游系统管理功能，通过 Maven 依赖引入，无需手写实现。

## 1. Technical Context

| 项目 | 值 |
|------|-----|
| Language/Version | Java 21+ |
| Primary Dependencies | Spring Boot 3.5.14, MyBatis-Plus 3.5.16, Redis + Redisson, RuoYi-Vue-Plus 5.6.0 |
| Storage | MySQL (sys_config 表), Redis (参数缓存) |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Target Platform | Linux server, Undertow :8080 |
| Project Type | Web-service (企业级后台管理模块) |
| Performance Goals | 参数查询 P95 < 50ms（缓存命中），列表查询 P95 < 200ms |
| Constraints | 参数键名唯一，系统内置参数限制删除，缓存一致性必须保证 |
| Scale/Scope | 系统参数数量有限（通常 < 100 条），高频读取，低频写入 |

### 前端技术上下文

| 项目 | 值 |
|------|-----|
| Framework | Vue 3.5 + TypeScript 5.9 |
| UI Library | Element Plus 2.11 |
| Build Tool | Vite 6.4 |
| State Management | Pinia 3.0 |

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 直接使用 RuoYi-Vue-Plus 上游模块，不额外抽象 |
| 约定优于配置 | 合规 | 遵循 `/system/config` 路径约定、`R<T>` 响应格式、分页参数命名 |
| 实用优于完美 | 合规 | CRUD 功能直接实现，缓存刷新按需调用 |
| 安全优于便利 | 合规 | 全部接口需 Token 认证，权限标识 `system:config:*` 严格校验 |
| 零样板代码 | 合规 | MyBatis-Plus BaseMapperPlus 提供基础 CRUD，MapStruct 自动转换对象 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `ruoyi-system`，不复制上游源码 |
| 清晰模块边界 | 合规 | 上游 `org.dromara.system` 包负责实现，自定义代码不介入 |
| 多租户优先 | 合规 | sys_config 为系统级表，排除在租户过滤之外 |
| 前后端分离 | 合规 | 前端独立 API 模块 `@/api/system/config`，后端 RESTful 接口 |
| 严格分层 | 合规 | Controller -> Service -> Mapper -> Database，无跨层调用 |

## 3. Research Findings

### 3.1 上游实现位置

参数配置模块由 RuoYi-Vue-Plus 的 `ruoyi-system` 模块提供，关键类位于 `org.dromara.system` 包下。本项目通过 Maven 依赖引入，无需自行实现。

### 3.2 缓存机制

- 参数缓存 Key 格式: `config:{configKey}`
- 缓存存储在 Redis 中
- 新增、修改参数时自动清除对应 Key 的缓存
- 删除参数时清除对应 Key 的缓存
- `refreshCache` 接口清除全部参数缓存（使用 Redis Key 模式匹配）

### 3.3 特殊接口

- `GET /system/config/configKey/{configKey}`: 按参数键名查询参数值，返回纯字符串。优先从缓存读取，缓存未命中时查库并回填缓存。
- `PUT /system/config/updateByKey`: 按参数键名更新参数值，用于快速修改配置。
- `DELETE /system/config/refreshCache`: 手动刷新全部参数缓存。
- `POST /system/config/export`: 导出参数数据为 Excel 文件。

### 3.4 字典依赖

- `sys_yes_no`: 系统内置标识（Y=是, N=否）

## 4. Data Model

### 4.1 数据库表: sys_config

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| config_id | bigint(20) | NOT NULL | - | 参数 ID（主键，雪花算法） |
| config_name | varchar(100) | YES | '' | 参数名称 |
| config_key | varchar(100) | YES | '' | 参数键名（唯一） |
| config_value | varchar(500) | YES | '' | 参数键值 |
| config_type | char(1) | YES | 'N' | 系统内置（Y=是, N=否） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**索引**: `config_key` 唯一索引

### 4.2 后端对象

| 对象类型 | 类名 | 用途 |
|----------|------|------|
| Entity | `SysConfig` | 数据库实体，继承 `BaseEntity` |
| VO | `ConfigVo` | 前端展示数据 |
| Bo | `ConfigBo` | 业务层传输数据 |
| Query | `ConfigQuery` | 查询参数 |

### 4.3 前端类型

| 类型 | 用途 |
|------|------|
| `ConfigVO` | 参数返回对象，继承 `BaseEntity` |
| `ConfigForm` | 参数表单对象 |
| `ConfigQuery` | 参数查询对象，继承 `PageQuery` |

### 4.4 数据流转

```
新增: ConfigForm -> POST /system/config -> Service 校验 configKey 唯一性 -> 插入 sys_config -> 清除 Redis 缓存 -> R.ok()
修改: ConfigForm -> PUT /system/config -> Service 校验 configKey 唯一性（排除自身） -> 更新 sys_config -> 清除 Redis 缓存 -> R.ok()
按键更新: {configKey, configValue} -> PUT /system/config/updateByKey -> 按 configKey 查询 -> 更新 configValue -> 清除 Redis 缓存 -> R.ok()
刷新缓存: DELETE /system/config/refreshCache -> 清除 Redis 中所有 config:* 缓存 -> R.ok()
```

## 5. Interface Contracts

### 5.1 API 接口清单

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/config/list` | `system:config:list` | 查询参数列表（分页） |
| GET | `/system/config/{configId}` | `system:config:query` | 查询参数详情 |
| POST | `/system/config` | `system:config:add` | 新增参数 |
| PUT | `/system/config` | `system:config:edit` | 修改参数 |
| DELETE | `/system/config/{configIds}` | `system:config:remove` | 删除参数（支持批量） |
| GET | `/system/config/configKey/{configKey}` | - | 按参数键名查询参数值 |
| PUT | `/system/config/updateByKey` | - | 按参数键名更新参数值 |
| DELETE | `/system/config/refreshCache` | `system:config:remove` | 刷新参数缓存 |
| POST | `/system/config/export` | `system:config:export` | 导出参数数据 |

### 5.2 前端 API 封装

模块: `@/api/system/config/index.ts`

| 函数 | 方法 | 说明 |
|------|------|------|
| `listConfig(query)` | GET | 查询参数列表 |
| `getConfig(configId)` | GET | 查询参数详情 |
| `getConfigKey(configKey)` | GET | 按键名查询值 |
| `addConfig(data)` | POST | 新增参数 |
| `updateConfig(data)` | PUT | 修改参数 |
| `updateConfigByKey(key, value)` | PUT | 按键名更新 |
| `delConfig(configIds)` | DELETE | 删除参数 |
| `refreshCache()` | DELETE | 刷新缓存 |

### 5.3 响应格式

统一使用 `R<T>` 包装。分页接口返回 `R<TableDataInfo<ConfigVo>>`，详情接口返回 `R<ConfigVo>`，按键查询返回 `R<String>`。

## 6. Implementation Strategy

### 6.1 后端实现

本模块由 RuoYi-Vue-Plus 上游提供，实现要点:

- **Controller**: `SysConfigController`，位于 `org.dromara.system.controller.system` 包
- **Service**: `ISysConfigService` / `SysConfigServiceImpl`，封装业务逻辑和缓存操作
- **Mapper**: `SysConfigMapper`，继承 `BaseMapperPlus<SysConfig, ConfigVo>`
- **缓存**: 通过 Redis 缓存参数值，Key 格式 `config:{configKey}`

### 6.2 前端实现

- **页面**: `src/views/system/config/index.vue`
- **API 模块**: `src/api/system/config/index.ts`
- **类型定义**: `src/api/system/config/types.ts`
- **路由**: `/system/config`，动态加载

### 6.3 关键实现细节

1. **参数键名唯一性校验**: 新增时检查 configKey 不存在，修改时排除自身
2. **缓存一致性**: 写操作后必须清除对应缓存，确保下次读取获取最新值
3. **系统内置参数保护**: configType='Y' 的参数在删除时应给予提示或限制
4. **导出功能**: 按当前查询条件导出，文件名包含时间戳

### 6.4 项目结构

```
specs/009-config/
├── spec.md          # 功能规格
├── plan.md          # 本文件
├── api.md           # API 接口定义
├── data-model.md    # 数据模型
├── pages.md         # 前端页面定义
└── user-stories.md  # 用户故事

后端（上游 ruoyi-system 模块）:
org.dromara.system.controller.system/
└── SysConfigController.java
org.dromara.system.service/
├── ISysConfigService.java
└── impl/SysConfigServiceImpl.java
org.dromara.system.mapper/
└── SysConfigMapper.java
org.dromara.system.domain/
├── SysConfig.java
├── vo/ConfigVo.java
└── bo/ConfigBo.java

前端（magic-ruoyi-web）:
src/views/system/config/
└── index.vue
src/api/system/config/
├── index.ts
└── types.ts
```

## 7. Testing Considerations

### 7.1 后端测试

| 测试类型 | 测试内容 |
|----------|----------|
| Service 单元测试 | configKey 唯一性校验、缓存清除逻辑 |
| Controller 集成测试 | CRUD 接口请求/响应、权限校验 |
| 缓存测试 | 新增后缓存清除、修改后缓存清除、refreshCache 清除全部 |

### 7.2 前端测试

| 测试类型 | 测试内容 |
|----------|----------|
| 组件测试 | 表单校验规则（必填项）、字典标签渲染 |
| 交互测试 | 搜索、分页、新增/修改对话框、删除确认 |

### 7.3 手工测试场景

1. 新增参数，验证列表中出现新记录
2. 修改参数值，验证缓存刷新后获取最新值
3. 删除参数，验证数据库和缓存同步删除
4. 按键名查询，验证缓存命中和未命中两种路径
5. 刷新缓存，验证全部缓存被清除
6. 导出参数，验证 Excel 文件内容正确

## 8. File Inventory

### 8.1 规格文档

| 文件 | 路径 | 状态 |
|------|------|------|
| spec.md | `specs/009-config/spec.md` | 已完成 |
| plan.md | `specs/009-config/plan.md` | 本文件 |
| api.md | `specs/009-config/api.md` | 已完成 |
| data-model.md | `specs/009-config/data-model.md` | 已完成 |
| pages.md | `specs/009-config/pages.md` | 已完成 |
| user-stories.md | `specs/009-config/user-stories.md` | 已完成 |

### 8.2 后端文件（上游 ruoyi-system）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| SysConfigController.java | `org.dromara.system.controller.system` | 控制器入口 |
| ISysConfigService.java | `org.dromara.system.service` | 服务接口 |
| SysConfigServiceImpl.java | `org.dromara.system.service.impl` | 服务实现 |
| SysConfigMapper.java | `org.dromara.system.mapper` | 数据访问 |
| SysConfig.java | `org.dromara.system.domain` | 实体类 |
| ConfigVo.java | `org.dromara.system.domain.vo` | 视图对象 |
| ConfigBo.java | `org.dromara.system.domain.bo` | 业务对象 |

### 8.3 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| index.vue | `src/views/system/config/index.vue` | 参数配置页面 |
| index.ts | `src/api/system/config/index.ts` | API 函数封装 |
| types.ts | `src/api/system/config/types.ts` | TypeScript 类型定义 |

### 8.4 数据库

| 表名 | 说明 |
|------|------|
| sys_config | 参数配置表 |

## Complexity Tracking

无宪法违规项。本模块完全由上游 RuoYi-Vue-Plus 提供，本项目仅通过 Maven 依赖引入，不产生额外实现复杂度。
