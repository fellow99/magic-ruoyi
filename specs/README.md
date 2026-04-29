# magic-ruoyi 规范文档索引

> **文档总数**: 105个文件，31330行代码  
> **生成时间**: 2026-04-29  
> **文档版本**: 1.0.0

---

## 项目概述

magic-ruoyi（魔法积木）是一个企业级应用快速开发平台，融合了：
- **RuoYi-Vue-Plus**: 成熟的企业级开发框架（多租户、权限、监控、工作流）
- **magic-api**: 动态接口引擎（零代码API开发）

---

## 整体规范文档

| 文档 | 说明 | 路径 |
|------|------|------|
| **README.md** | 文档索引（本文件） | [README.md](./README.md) |
| **constitution.md** | 项目宪法原则 | [constitution.md](./constitution.md) |
| **TECH.md** | 技术选型清单 | [TECH.md](./TECH.md) |
| **ARCHITECTURE.md** | 系统架构设计 | [ARCHITECTURE.md](./ARCHITECTURE.md) |
| **STRUCTURE.md** | 项目目录结构 | [STRUCTURE.md](./STRUCTURE.md) |
| **overall-spec.md** | 整体功能规格 | [overall-spec.md](./overall-spec.md) |
| **overall-plan.md** | 整体技术方案 | [overall-plan.md](./overall-plan.md) |
| **overall-data-model.md** | 数据模型设计 | [overall-data-model.md](./overall-data-model.md) |
| **overall-api.md** | API接口契约 | [overall-api.md](./overall-api.md) |
| **API.md** | REST API清单 | [API.md](./API.md) |
| **SPECS_CHECKLIST.md** | 文档完成追踪 | [SPECS_CHECKLIST.md](./SPECS_CHECKLIST.md) |

---

## 功能模块文档

每个模块包含以下文档类型：
- `spec.md` - 功能规格说明（技术无关）
- `plan.md` - 技术实现方案
- `api.md` - API接口清单
- `pages.md` - 前端页面设计
- `data-model.md` - 数据模型定义
- `user-stories.md` - 用户故事

---

### 系统管理模块

| 模块 | 功能说明 | spec | plan | api | pages | data-model | user-stories |
|------|----------|------|------|-----|-------|------------|--------------|
| **001-auth** | 认证模块（密码/短信/邮件/社交/小程序） | [spec](./001-auth/spec.md) | [plan](./001-auth/plan.md) | [api](./001-auth/api.md) | [pages](./001-auth/pages.md) | - | [stories](./001-auth/user-stories.md) |
| **002-tenant** | 租户管理、套餐分配 | [spec](./002-tenant/spec.md) | [plan](./002-tenant/plan.md) | [api](./002-tenant/api.md) | [pages](./002-tenant/pages.md) | [model](./002-tenant/data-model.md) | [stories](./002-tenant/user-stories.md) |
| **003-user** | 用户管理、个人中心 | [spec](./003-user/spec.md) | [plan](./003-user/plan.md) | [api](./003-user/api.md) | [pages](./003-user/pages.md) | [model](./003-user/data-model.md) | [stories](./003-user/user-stories.md) |
| **004-role** | 角色管理、数据权限 | [spec](./004-role/spec.md) | [plan](./004-role/plan.md) | [api](./004-role/api.md) | [pages](./004-role/pages.md) | [model](./004-role/data-model.md) | [stories](./004-role/user-stories.md) |
| **005-menu** | 菜单管理、动态路由 | [spec](./005-menu/spec.md) | [plan](./005-menu/plan.md) | [api](./005-menu/api.md) | [pages](./005-menu/pages.md) | [model](./005-menu/data-model.md) | [stories](./005-menu/user-stories.md) |
| **006-dept** | 部门管理（树形结构） | [spec](./006-dept/spec.md) | [plan](./006-dept/plan.md) | [api](./006-dept/api.md) | [pages](./006-dept/pages.md) | [model](./006-dept/data-model.md) | [stories](./006-dept/user-stories.md) |
| **007-post** | 岗位管理 | [spec](./007-post/spec.md) | [plan](./007-post/plan.md) | [api](./007-post/api.md) | [pages](./007-post/pages.md) | [model](./007-post/data-model.md) | [stories](./007-post/user-stories.md) |
| **008-dict** | 字典类型和数据管理 | [spec](./008-dict/spec.md) | [plan](./008-dict/plan.md) | [api](./008-dict/api.md) | [pages](./008-dict/pages.md) | [model](./008-dict/data-model.md) | [stories](./008-dict/user-stories.md) |
| **009-config** | 参数配置 | [spec](./009-config/spec.md) | [plan](./009-config/plan.md) | [api](./009-config/api.md) | [pages](./009-config/pages.md) | [model](./009-config/data-model.md) | [stories](./009-config/user-stories.md) |
| **010-notice** | 通知公告 | [spec](./010-notice/spec.md) | [plan](./010-notice/plan.md) | [api](./010-notice/api.md) | [pages](./010-notice/pages.md) | [model](./010-notice/data-model.md) | [stories](./010-notice/user-stories.md) |

---

### 资源管理模块

| 模块 | 功能说明 | spec | plan | api | pages | data-model | user-stories |
|------|----------|------|------|-----|-------|------------|--------------|
| **011-oss** | OSS文件存储（MinIO/阿里云/腾讯云） | [spec](./011-oss/spec.md) | [plan](./011-oss/plan.md) | [api](./011-oss/api.md) | [pages](./011-oss/pages.md) | [model](./011-oss/data-model.md) | [stories](./011-oss/user-stories.md) |
| **012-client** | OAuth客户端管理 | [spec](./012-client/spec.md) | [plan](./012-client/plan.md) | [api](./012-client/api.md) | [pages](./012-client/pages.md) | [model](./012-client/data-model.md) | [stories](./012-client/user-stories.md) |

---

### 系统监控模块

| 模块 | 功能说明 | spec | plan | api | pages | data-model | user-stories |
|------|----------|------|------|-----|-------|------------|--------------|
| **013-monitor** | 在线用户、登录日志、操作日志、缓存监控 | [spec](./013-monitor/spec.md) | [plan](./013-monitor/plan.md) | [api](./013-monitor/api.md) | [pages](./013-monitor/pages.md) | [model](./013-monitor/data-model.md) | [stories](./013-monitor/user-stories.md) |

---

### 开发工具模块

| 模块 | 功能说明 | spec | plan | api | pages | data-model | user-stories |
|------|----------|------|------|-----|-------|------------|--------------|
| **014-codegen** | 代码生成器 | [spec](./014-codegen/spec.md) | [plan](./014-codegen/plan.md) | [api](./014-codegen/api.md) | [pages](./014-codegen/pages.md) | [model](./014-codegen/data-model.md) | [stories](./014-codegen/user-stories.md) |
| **015-workflow** | 工作流引擎（Warm-Flow） | [spec](./015-workflow/spec.md) | [plan](./015-workflow/plan.md) | [api](./015-workflow/api.md) | [pages](./015-workflow/pages.md) | [model](./015-workflow/data-model.md) | [stories](./015-workflow/user-stories.md) |
| **016-magic-api** | 动态接口引擎 | [spec](./016-magic-api/spec.md) | [plan](./016-magic-api/plan.md) | [api](./016-magic-api/api.md) | [pages](./016-magic-api/pages.md) | - | [stories](./016-magic-api/user-stories.md) |

---

## 文档维护

文档遵循 `speckit` 技能规范生成，每个文档包含：
- 技术上下文
- 宪法合规检查
- 实现策略
- 测试考虑
- 文件清单

如有疑问或需要更新，请参考 [constitution.md](./constitution.md) 中的原则和规范。