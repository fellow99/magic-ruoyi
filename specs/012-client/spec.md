# 客户端管理模块规格文档（012-client/spec.md）

> magic-ruoyi 客户端管理模块。定义 OAuth 客户端的增删改查、状态管理功能。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 模块概述

客户端管理模块负责管理系统的 OAuth 客户端配置，每个客户端定义了授权类型、Token 参数、设备类型等安全策略。登录流程中的 Client 校验依赖此模块的数据。

### 1.1 技术栈

| 组件 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot + MyBatis-Plus |
| 前端框架 | Vue 3.5 + Element Plus |
| 认证框架 | Sa-Token |

### 1.2 核心功能

- 客户端 CRUD（增删改查）
- 客户端状态管理（启用/停用）
- 授权类型配置
- Token 参数配置（超时时间、活跃超时时间）
- 设备类型配置

---

## 2. 功能规格

### 2.1 客户端列表

**查询条件**:

| 条件 | 类型 | 说明 |
|------|------|------|
| 客户端 ID | 文本输入 | 精确匹配 clientId |
| 客户端 Key | 文本输入 | 模糊匹配 clientKey |
| 授权类型 | 文本输入 | 匹配 grantType |
| 设备类型 | 文本输入 | 匹配 deviceType |
| 状态 | 下拉选择 | 正常/停用 |

**列表字段**:

| 字段 | 说明 |
|------|------|
| 客户端 ID | clientId |
| 客户端 Key | clientKey |
| 客户端秘钥 | clientSecret |
| 授权类型 | grantTypeList |
| 设备类型 | deviceType |
| Token 活跃超时 | activeTimeout |
| Token 固定超时 | timeout |
| 状态 | status |

### 2.2 客户端新增/修改

**表单字段**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 客户端 ID | 自动生成 | - | UUID 格式 |
| 客户端 Key | 文本输入 | 是 | 客户端唯一标识 |
| 客户端秘钥 | 自动生成/手动输入 | 是 | 客户端密钥 |
| 授权类型 | 多选 | 是 | password, sms, email, social, xcx 等 |
| 设备类型 | 文本输入 | 是 | PC, APP, 小程序等 |
| Token 活跃超时 | 数字输入 | 是 | 秒数，超时需重新登录 |
| Token 固定超时 | 数字输入 | 是 | 秒数，Token 总有效期 |
| 状态 | 单选 | 否 | 0=正常, 1=停用 |

### 2.3 客户端状态切换

- 支持快速切换客户端启用/停用状态
- 停用的客户端无法用于登录

---

## 3. 权限控制

| 权限标识 | 功能 | 按钮/操作 |
|----------|------|-----------|
| `system:client:list` | 查看客户端列表 | 页面访问 |
| `system:client:query` | 查看客户端详情 | 查看详情 |
| `system:client:add` | 新增客户端 | 新增按钮 |
| `system:client:edit` | 修改客户端 | 修改按钮 |
| `system:client:remove` | 删除客户端 | 删除按钮 |

---

## 4. 数据模型

### 4.1 核心实体: SysClient

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| clientId | String | 客户端 ID（UUID） |
| clientKey | String | 客户端 Key |
| clientSecret | String | 客户端秘钥 |
| grantTypeList | String | 授权类型列表（JSON 数组） |
| deviceType | String | 设备类型 |
| activeTimeout | Long | Token 活跃超时时间（秒） |
| timeout | Long | Token 固定超时时间（秒） |
| status | String | 状态（0=正常, 1=停用） |

---

## 5. 授权类型

| 值 | 对应策略 | 说明 |
|----|----------|------|
| password | PasswordAuthStrategy | 用户名密码登录 |
| sms | SmsAuthStrategy | 短信验证码登录 |
| email | EmailAuthStrategy | 邮箱验证码登录 |
| social | SocialAuthStrategy | 第三方社交登录 |
| xcx | XcxAuthStrategy | 微信小程序登录 |

---

## 6. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
