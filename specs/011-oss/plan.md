# Implementation Plan: 011-OSS 文件存储

**Branch**: `011-oss` | **Date**: 2026-04-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/011-oss/spec.md`

## Summary

文件存储模块提供对象存储文件的统一管理能力，支持 MinIO、阿里云 OSS、腾讯云 COS、七牛云等多种存储服务商。模块包含文件管理（列表查询、删除）和存储配置管理（CRUD）两个子页面。通过 AWS S3 SDK 兼容协议实现多 Provider 统一接口。存储配置为系统级数据，排除在租户过滤之外。

## 1. Technical Context

| 项目 | 值 |
|------|-----|
| Language/Version | Java 21+ |
| Primary Dependencies | Spring Boot 3.5.14, MyBatis-Plus 3.5.16, AWS S3 SDK, RuoYi-Vue-Plus 5.6.0 |
| Storage | MySQL (sys_oss, sys_oss_config 表), 对象存储 (MinIO/阿里云/腾讯云/七牛云) |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Target Platform | Linux server, Undertow :8080 |
| Project Type | Web-service (企业级后台管理模块) |
| Performance Goals | 文件列表查询 P95 < 200ms，文件上传/删除 P95 < 2s |
| Constraints | 多 Provider 兼容，配置切换时不影响已有文件，删除文件需同步删除对象存储中的实际文件 |
| Scale/Scope | 文件数量可达百万级，配置数量通常 < 10 个 |

### 前端技术上下文

| 项目 | 值 |
|------|-----|
| Framework | Vue 3.5 + TypeScript 5.9 |
| UI Library | Element Plus 2.11 |
| Build Tool | Vite 6.4 |

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 使用 AWS S3 SDK 统一接口，多 Provider 通过配置切换 |
| 约定优于配置 | 合规 | 遵循 `/resource/oss` 和 `/resource/oss/config` 路径约定 |
| 实用优于完美 | 合规 | 先支持主流 Provider（MinIO/阿里云/腾讯云/七牛云），按需扩展 |
| 安全优于便利 | 合规 | 全部接口需 Token 认证，权限标识严格校验，密钥信息不在前端明文展示 |
| 零样板代码 | 合规 | MyBatis-Plus BaseMapperPlus 提供基础 CRUD |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `ruoyi-oss` 模块 |
| 清晰模块边界 | 合规 | 上游 `org.dromara` 包负责实现 |
| 多租户优先 | 合规 | sys_oss_config 为系统级表，排除在租户过滤之外。sys_oss 支持租户隔离 |
| 前后端分离 | 合规 | 前端独立 API 模块 `@/api/system/oss` |
| 严格分层 | 合规 | Controller -> Service -> Mapper -> Database |

## 3. Research Findings

### 3.1 上游实现位置

文件存储模块由 RuoYi-Vue-Plus 的 `ruoyi-oss` 模块提供，关键类位于 `org.dromara.oss` 包下。

### 3.2 多 Provider 架构

通过策略模式实现多存储服务商支持:

```
OssProperties (配置抽象)
    ├── MinioOssStrategy      ← MinIO 对象存储
    ├── AliyunOssStrategy     ← 阿里云 OSS
    ├── QcloudCosStrategy     ← 腾讯云 COS
    └── QiniuOssStrategy      ← 七牛云 Kodo
```

所有 Provider 基于 AWS S3 SDK 兼容协议，通过 `config_key` 字段区分。

### 3.3 配置管理

- `sys_oss_config` 表存储各 Provider 的连接配置
- 支持设置默认配置（`is_default` 或 `status='0'` 的第一条）
- 配置包含: endpoint、accessKey、secretKey、bucketName、prefix、domain、isHttps、region、accessPolicy
- 修改配置后需重新初始化对应的 OSS Client

### 3.4 文件删除

删除文件时需执行两步操作:
1. 从对象存储中删除实际文件（调用 Provider 的 delete 方法）
2. 删除 `sys_oss` 表中的记录

### 3.5 访问策略

| 值 | 含义 | 说明 |
|----|------|------|
| 0 | 私有读写 | 需要签名 URL 才能访问 |
| 1 | 公开读 | 文件 URL 可直接访问 |
| 2 | 自定义 | 自定义访问策略 |

## 4. Data Model

### 4.1 数据库表: sys_oss

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| oss_id | bigint(20) | NOT NULL | - | 文件 ID（主键，雪花算法） |
| file_name | varchar(255) | YES | '' | 文件名 |
| original_name | varchar(255) | YES | '' | 原始文件名 |
| file_suffix | varchar(10) | YES | '' | 文件后缀 |
| url | varchar(500) | YES | '' | 文件访问 URL |
| create_by | varchar(64) | YES | '' | 上传用户 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | varchar(64) | YES | '' | 更新用户 |
| update_time | datetime | YES | NULL | 更新时间 |
| service | varchar(40) | YES | '' | 服务商标识 |

### 4.2 数据库表: sys_oss_config

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| oss_config_id | bigint(20) | NOT NULL | - | 配置 ID（主键，雪花算法） |
| config_key | varchar(20) | YES | '' | 配置键（minio/aliyun/qcloud/qiniu） |
| access_key | varchar(255) | YES | '' | 访问密钥 |
| secret_key | varchar(255) | YES | '' | 秘钥 |
| bucket_name | varchar(255) | YES | '' | 存储桶名称 |
| prefix | varchar(255) | YES | '' | 文件路径前缀 |
| endpoint | varchar(255) | YES | '' | 访问站点 |
| domain | varchar(255) | YES | '' | 自定义域名 |
| is_https | char(1) | YES | 'N' | 是否 HTTPS |
| region | varchar(255) | YES | '' | 域 |
| status | char(1) | YES | '1' | 状态（0=正常, 1=停用） |
| ext1 | varchar(255) | YES | '' | 扩展字段 1 |
| create_by | varchar(64) | YES | '' | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | varchar(64) | YES | '' | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(255) | YES | '' | 备注 |
| access_policy | char(1) | YES | '1' | 访问策略（0=私有, 1=公开, 2=自定义） |

### 4.3 后端对象

| 对象类型 | 类名 | 用途 |
|----------|------|------|
| Entity | `SysOss` | 文件实体 |
| Entity | `SysOssConfig` | 配置实体 |
| VO | `OssVo` | 文件视图对象 |
| VO | `OssConfigVo` | 配置视图对象 |
| Bo | `OssBo` | 文件业务对象 |
| Bo | `OssConfigBo` | 配置业务对象 |

### 4.4 前端类型

| 类型 | 用途 |
|------|------|
| `OssVO` | 文件返回对象 |
| `OssQuery` | 文件查询对象 |
| `OssForm` | 文件上传表单对象 |

### 4.5 数据流转

```
文件上传: 文件 -> POST /resource/oss/upload -> Service 获取默认 OSS 配置 -> 上传到对象存储 -> 插入 sys_oss -> 返回文件信息
文件删除: ossIds -> DELETE /resource/oss/{ossIds} -> Service 查询 sys_oss -> 从对象存储删除文件 -> 删除 sys_oss 记录 -> R.ok()
配置管理: OssConfigForm -> POST/PUT/DELETE /resource/oss/config -> Service 管理 sys_oss_config -> R.ok()
```

## 5. Interface Contracts

### 5.1 文件管理接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/resource/oss/list` | `system:oss:list` | 查询文件列表（分页） |
| GET | `/resource/oss/listByIds/{ossIds}` | - | 按 ID 列表查询文件 |
| DELETE | `/resource/oss/{ossIds}` | `system:oss:remove` | 删除文件（支持批量） |

### 5.2 配置管理接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/resource/oss/config/list` | `system:ossConfig:list` | 查询配置列表 |
| GET | `/resource/oss/config/{ossConfigId}` | - | 查询配置详情 |
| POST | `/resource/oss/config` | `system:ossConfig:add` | 新增配置 |
| PUT | `/resource/oss/config` | `system:ossConfig:edit` | 修改配置 |
| DELETE | `/resource/oss/config/{ossConfigIds}` | `system:ossConfig:remove` | 删除配置（支持批量） |

### 5.3 前端 API 封装

模块: `@/api/system/oss/index.ts`

| 函数 | 方法 | 说明 |
|------|------|------|
| `listOss(query)` | GET | 查询文件列表 |
| `listByIds(ossId)` | GET | 按 ID 查询 |
| `delOss(ossIds)` | DELETE | 删除文件 |

### 5.4 响应格式

统一使用 `R<T>` 包装。分页接口返回 `R<TableDataInfo<OssVo>>`。

## 6. Implementation Strategy

### 6.1 后端实现

本模块由 RuoYi-Vue-Plus 上游提供，实现要点:

- **Controller**: `SysOssController`、`SysOssConfigController`，位于 `org.dromara.oss.controller` 包
- **Service**: `ISysOssService` / `ISysOssConfigService` 及其实现类
- **Mapper**: `SysOssMapper`、`SysOssConfigMapper`
- **Provider 策略**: 通过 `config_key` 字段路由到对应的 OSS Provider 实现
- **租户排除**: `sys_oss_config` 在租户配置中显式排除

### 6.2 前端实现

- **文件管理页面**: `src/views/system/oss/index.vue`
- **配置管理页面**: `src/views/system/oss/config.vue`
- **API 模块**: `src/api/system/oss/index.ts`
- **类型定义**: `src/api/system/oss/types.ts`
- **路由**: `/system/oss`（文件管理）、`/system/oss-config`（配置管理）

### 6.3 关键实现细节

1. **文件上传**: 通过默认 OSS 配置上传，文件名按日期路径组织（如 `2026/01/01/abc123.jpg`）
2. **文件删除**: 必须先从对象存储删除实际文件，再删除数据库记录
3. **配置切换**: 修改配置后需重新初始化 OSS Client，不影响已上传文件
4. **URL 预览**: 文件列表中的 URL 可点击，新窗口打开预览
5. **密钥安全**: 配置列表中的 secretKey 应脱敏展示（如 `****`）

### 6.4 项目结构

```
specs/011-oss/
├── spec.md          # 功能规格
├── plan.md          # 本文件
├── api.md           # API 接口定义
├── data-model.md    # 数据模型
├── pages.md         # 前端页面定义
└── user-stories.md  # 用户故事

后端（上游 ruoyi-oss 模块）:
org.dromara.oss.controller/
├── SysOssController.java
└── SysOssConfigController.java
org.dromara.oss.service/
├── ISysOssService.java
├── ISysOssConfigService.java
└── impl/
org.dromara.oss.mapper/
├── SysOssMapper.java
└── SysOssConfigMapper.java
org.dromara.oss.domain/
├── SysOss.java
├── SysOssConfig.java
├── vo/OssVo.java
├── vo/OssConfigVo.java
├── bo/OssBo.java
└── bo/OssConfigBo.java
org.dromara.oss.core/
└── (OSS Provider 策略实现)

前端（magic-ruoyi-web）:
src/views/system/oss/
├── index.vue          # 文件管理页面
└── config.vue         # 配置管理页面
src/api/system/oss/
├── index.ts
└── types.ts
```

## 7. Testing Considerations

### 7.1 后端测试

| 测试类型 | 测试内容 |
|----------|----------|
| Service 单元测试 | 文件上传逻辑、文件删除（对象存储 + 数据库）、配置 CRUD |
| Controller 集成测试 | 文件列表查询、删除接口、配置管理接口 |
| Provider 测试 | 各 Provider 的上传/下载/删除功能（需 Mock 或集成测试） |

### 7.2 前端测试

| 测试类型 | 测试内容 |
|----------|----------|
| 组件测试 | 文件列表渲染、配置表单渲染 |
| 交互测试 | 搜索、分页、删除确认、配置新增/修改对话框 |

### 7.3 手工测试场景

1. 配置 MinIO，上传文件，验证文件可访问
2. 切换配置到阿里云 OSS，上传文件，验证使用新 Provider
3. 删除文件，验证对象存储和数据库记录均被删除
4. 按文件名、后缀、服务商筛选，验证搜索结果正确
5. 新增/修改/删除存储配置，验证配置生效
6. 文件 URL 点击预览，验证新窗口打开

## 8. File Inventory

### 8.1 规格文档

| 文件 | 路径 | 状态 |
|------|------|------|
| spec.md | `specs/011-oss/spec.md` | 已完成 |
| plan.md | `specs/011-oss/plan.md` | 本文件 |
| api.md | `specs/011-oss/api.md` | 已完成 |
| data-model.md | `specs/011-oss/data-model.md` | 已完成 |
| pages.md | `specs/011-oss/pages.md` | 已完成 |
| user-stories.md | `specs/011-oss/user-stories.md` | 已完成 |

### 8.2 后端文件（上游 ruoyi-oss）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| SysOssController.java | `org.dromara.oss.controller` | 文件管理控制器 |
| SysOssConfigController.java | `org.dromara.oss.controller` | 配置管理控制器 |
| ISysOssService.java | `org.dromara.oss.service` | 文件服务接口 |
| ISysOssConfigService.java | `org.dromara.oss.service` | 配置服务接口 |
| SysOssMapper.java | `org.dromara.oss.mapper` | 文件数据访问 |
| SysOssConfigMapper.java | `org.dromara.oss.mapper` | 配置数据访问 |
| SysOss.java | `org.dromara.oss.domain` | 文件实体 |
| SysOssConfig.java | `org.dromara.oss.domain` | 配置实体 |
| OssVo.java | `org.dromara.oss.domain.vo` | 文件视图对象 |
| OssConfigVo.java | `org.dromara.oss.domain.vo` | 配置视图对象 |

### 8.3 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| index.vue | `src/views/system/oss/index.vue` | 文件管理页面 |
| config.vue | `src/views/system/oss/config.vue` | 配置管理页面 |
| index.ts | `src/api/system/oss/index.ts` | API 函数封装 |
| types.ts | `src/api/system/oss/types.ts` | TypeScript 类型定义 |

### 8.4 数据库

| 表名 | 说明 |
|------|------|
| sys_oss | OSS 文件存储表 |
| sys_oss_config | OSS 配置表 |

## Complexity Tracking

无宪法违规项。本模块由上游 RuoYi-Vue-Plus 提供，多 Provider 架构通过策略模式实现，复杂度由上游框架承担。
