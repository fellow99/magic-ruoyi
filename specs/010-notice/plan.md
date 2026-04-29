# Implementation Plan: 010-Notice 通知公告

**Branch**: `010-notice` | **Date**: 2026-04-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/010-notice/spec.md`

## Summary

通知公告模块提供系统内部公告的发布与维护能力，支持富文本编辑、公告类型分类（通知/公告）、状态管理（正常/关闭）。公告由管理员创建，面向系统用户展示。模块由 RuoYi-Vue-Plus 上游提供，通过 Maven 依赖引入。富文本内容路径排除 XSS 过滤。

## 1. Technical Context

| 项目 | 值 |
|------|-----|
| Language/Version | Java 21+ |
| Primary Dependencies | Spring Boot 3.5.14, MyBatis-Plus 3.5.16, RuoYi-Vue-Plus 5.6.0 |
| Storage | MySQL (sys_notice 表) |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Target Platform | Linux server, Undertow :8080 |
| Project Type | Web-service (企业级后台管理模块) |
| Performance Goals | 列表查询 P95 < 200ms，富文本内容加载 P95 < 100ms |
| Constraints | 富文本内容需排除 XSS 过滤，公告类型和状态使用字典管理 |
| Scale/Scope | 公告数量有限（通常 < 1000 条），低频写入，中频读取 |

### 前端技术上下文

| 项目 | 值 |
|------|-----|
| Framework | Vue 3.5 + TypeScript 5.9 |
| UI Library | Element Plus 2.11 |
| Rich Text Editor | Editor 组件（封装 wangEditor 或 Quill） |
| Build Tool | Vite 6.4 |

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 直接使用 RuoYi-Vue-Plus 上游模块，不额外抽象 |
| 约定优于配置 | 合规 | 遵循 `/system/notice` 路径约定、`R<T>` 响应格式 |
| 实用优于完美 | 合规 | CRUD 功能直接实现，富文本使用框架内置 Editor 组件 |
| 安全优于便利 | 合规 | 全部接口需 Token 认证，权限标识 `system:notice:*` 严格校验 |
| 零样板代码 | 合规 | MyBatis-Plus BaseMapperPlus 提供基础 CRUD，MapStruct 自动转换 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `ruoyi-system`，不复制上游源码 |
| 清晰模块边界 | 合规 | 上游 `org.dromara.system` 包负责实现 |
| 多租户优先 | 合规 | sys_notice 为系统级表，排除在租户过滤之外 |
| 前后端分离 | 合规 | 前端独立 API 模块 `@/api/system/notice` |
| 严格分层 | 合规 | Controller -> Service -> Mapper -> Database |
| XSS 防护 | 合规 | `/system/notice` 路径排除 XSS 过滤，富文本内容允许 HTML |

## 3. Research Findings

### 3.1 上游实现位置

通知公告模块由 RuoYi-Vue-Plus 的 `ruoyi-system` 模块提供，关键类位于 `org.dromara.system` 包下。

### 3.2 XSS 排除配置

公告模块的富文本内容需要在 XSS 过滤器中排除。RuoYi-Vue-Plus 默认将 `/system/notice` 路径加入 XSS 排除列表，确保 HTML 内容不被转义。

### 3.3 字典依赖

- `sys_notice_type`: 公告类型（1=通知, 2=公告）
- `sys_notice_status`: 公告状态（0=正常, 1=关闭）

### 3.4 创建者名称关联

列表查询时通过关联查询 `sys_user` 表获取 `createByName`（创建者名称），在 VO 层返回。

## 4. Data Model

### 4.1 数据库表: sys_notice

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| notice_id | bigint(20) | NOT NULL | - | 公告 ID（主键，雪花算法） |
| notice_title | varchar(50) | NOT NULL | - | 公告标题 |
| notice_type | char(1) | NOT NULL | - | 公告类型（1=通知, 2=公告） |
| notice_content | longblob | YES | NULL | 公告内容（富文本 HTML） |
| status | char(1) | YES | '0' | 状态（0=正常, 1=关闭） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(255) | YES | NULL | 备注 |

### 4.2 后端对象

| 对象类型 | 类名 | 用途 |
|----------|------|------|
| Entity | `SysNotice` | 数据库实体，继承 `BaseEntity` |
| VO | `NoticeVo` | 前端展示数据，包含 `createByName` |
| Bo | `NoticeBo` | 业务层传输数据 |

### 4.3 前端类型

| 类型 | 用途 |
|------|------|
| `NoticeVO` | 公告返回对象，继承 `BaseEntity` |
| `NoticeForm` | 公告表单对象 |
| `NoticeQuery` | 公告查询对象，继承 `PageQuery` |

### 4.4 数据流转

```
新增: NoticeForm -> POST /system/notice -> Service 插入 sys_notice -> 自动填充 createByName -> R.ok()
修改: NoticeForm -> PUT /system/notice -> Service 更新 sys_notice -> R.ok()
删除: noticeIds -> DELETE /system/notice/{noticeIds} -> Service 删除 sys_notice -> R.ok()
```

## 5. Interface Contracts

### 5.1 API 接口清单

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/notice/list` | `system:notice:list` | 查询公告列表（分页） |
| GET | `/system/notice/{noticeId}` | `system:notice:query` | 查询公告详情 |
| POST | `/system/notice` | `system:notice:add` | 新增公告 |
| PUT | `/system/notice` | `system:notice:edit` | 修改公告 |
| DELETE | `/system/notice/{noticeIds}` | `system:notice:remove` | 删除公告（支持批量） |

### 5.2 前端 API 封装

模块: `@/api/system/notice/index.ts`

| 函数 | 方法 | 说明 |
|------|------|------|
| `listNotice(query)` | GET | 查询公告列表 |
| `getNotice(noticeId)` | GET | 查询公告详情 |
| `addNotice(data)` | POST | 新增公告 |
| `updateNotice(data)` | PUT | 修改公告 |
| `delNotice(noticeIds)` | DELETE | 删除公告 |

### 5.3 响应格式

统一使用 `R<T>` 包装。分页接口返回 `R<TableDataInfo<NoticeVo>>`，详情接口返回 `R<NoticeVo>`。

## 6. Implementation Strategy

### 6.1 后端实现

本模块由 RuoYi-Vue-Plus 上游提供，实现要点:

- **Controller**: `SysNoticeController`，位于 `org.dromara.system.controller.system` 包
- **Service**: `ISysNoticeService` / `SysNoticeServiceImpl`，封装业务逻辑
- **Mapper**: `SysNoticeMapper`，继承 `BaseMapperPlus<SysNotice, NoticeVo>`
- **XSS 排除**: `/system/notice` 路径在 XSS 过滤器配置中排除

### 6.2 前端实现

- **页面**: `src/views/system/notice/index.vue`
- **API 模块**: `src/api/system/notice/index.ts`
- **类型定义**: `src/api/system/notice/types.ts`
- **路由**: `/system/notice`，动态加载
- **富文本组件**: 使用 `Editor` 组件（封装 wangEditor/Quill）

### 6.3 关键实现细节

1. **富文本内容**: 公告内容以 HTML 格式存储，前端使用 Editor 组件编辑和展示
2. **创建者名称**: 列表查询时关联 `sys_user` 表获取 `createByName`
3. **状态默认值**: 新增公告时 status 默认为 "0"（正常）
4. **字典展示**: 公告类型和状态使用 `dict-tag` 组件展示

### 6.4 项目结构

```
specs/010-notice/
├── spec.md          # 功能规格
├── plan.md          # 本文件
├── api.md           # API 接口定义
├── data-model.md    # 数据模型
├── pages.md         # 前端页面定义
└── user-stories.md  # 用户故事

后端（上游 ruoyi-system 模块）:
org.dromara.system.controller.system/
└── SysNoticeController.java
org.dromara.system.service/
├── ISysNoticeService.java
└── impl/SysNoticeServiceImpl.java
org.dromara.system.mapper/
└── SysNoticeMapper.java
org.dromara.system.domain/
├── SysNotice.java
├── vo/NoticeVo.java
└── bo/NoticeBo.java

前端（magic-ruoyi-web）:
src/views/system/notice/
└── index.vue
src/api/system/notice/
├── index.ts
└── types.ts
src/components/Editor/
└── (富文本编辑器组件)
```

## 7. Testing Considerations

### 7.1 后端测试

| 测试类型 | 测试内容 |
|----------|----------|
| Service 单元测试 | 公告 CRUD 逻辑、创建者名称关联查询 |
| Controller 集成测试 | CRUD 接口请求/响应、权限校验 |
| XSS 排除测试 | 验证富文本 HTML 内容不被转义 |

### 7.2 前端测试

| 测试类型 | 测试内容 |
|----------|----------|
| 组件测试 | 表单校验（标题和类型必填）、富文本编辑器渲染 |
| 交互测试 | 搜索、分页、新增/修改对话框、删除确认 |

### 7.3 手工测试场景

1. 新增公告，填写标题、类型、富文本内容，验证保存成功
2. 修改公告，验证富文本内容正确回显
3. 删除公告，验证数据库记录被删除
4. 按标题模糊搜索，验证搜索结果正确
5. 按类型筛选，验证过滤结果正确
6. 富文本内容包含 HTML 标签，验证不被 XSS 过滤转义

## 8. File Inventory

### 8.1 规格文档

| 文件 | 路径 | 状态 |
|------|------|------|
| spec.md | `specs/010-notice/spec.md` | 已完成 |
| plan.md | `specs/010-notice/plan.md` | 本文件 |
| api.md | `specs/010-notice/api.md` | 已完成 |
| data-model.md | `specs/010-notice/data-model.md` | 已完成 |
| pages.md | `specs/010-notice/pages.md` | 已完成 |
| user-stories.md | `specs/010-notice/user-stories.md` | 已完成 |

### 8.2 后端文件（上游 ruoyi-system）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| SysNoticeController.java | `org.dromara.system.controller.system` | 控制器入口 |
| ISysNoticeService.java | `org.dromara.system.service` | 服务接口 |
| SysNoticeServiceImpl.java | `org.dromara.system.service.impl` | 服务实现 |
| SysNoticeMapper.java | `org.dromara.system.mapper` | 数据访问 |
| SysNotice.java | `org.dromara.system.domain` | 实体类 |
| NoticeVo.java | `org.dromara.system.domain.vo` | 视图对象 |
| NoticeBo.java | `org.dromara.system.domain.bo` | 业务对象 |

### 8.3 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| index.vue | `src/views/system/notice/index.vue` | 通知公告页面 |
| index.ts | `src/api/system/notice/index.ts` | API 函数封装 |
| types.ts | `src/api/system/notice/types.ts` | TypeScript 类型定义 |

### 8.4 数据库

| 表名 | 说明 |
|------|------|
| sys_notice | 通知公告表 |

## Complexity Tracking

无宪法违规项。本模块完全由上游 RuoYi-Vue-Plus 提供，本项目仅通过 Maven 依赖引入。
