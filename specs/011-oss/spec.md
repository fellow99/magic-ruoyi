# 文件存储模块规格文档（011-oss/spec.md）

> magic-ruoyi 文件存储模块。定义 OSS 对象存储文件管理和存储配置功能。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 模块概述

文件存储模块负责管理系统上传的文件，支持多种对象存储服务（MinIO、阿里云 OSS、腾讯云 COS、七牛云等）。模块包含文件列表管理和存储配置管理两个子页面。

### 1.1 技术栈

| 组件 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot + MyBatis-Plus |
| 前端框架 | Vue 3.5 + Element Plus |
| 对象存储 | AWS S3 SDK（兼容多种 OSS） |

### 1.2 核心功能

- OSS 文件列表查询、删除
- 按文件名、后缀、服务类型筛选
- OSS 存储配置 CRUD
- 支持多种存储服务商

### 1.3 模块结构

```
文件存储
├── 文件管理 (index.vue)        → /system/oss
└── 配置管理 (config.vue)       → /system/oss-config
```

---

## 2. 文件管理

### 2.1 文件列表

**查询条件**:

| 条件 | 类型 | 说明 |
|------|------|------|
| 文件名 | 文本输入 | 模糊匹配 fileName |
| 原名 | 文本输入 | 模糊匹配 originalName |
| 文件后缀 | 文本输入 | 模糊匹配 fileSuffix |
| 服务商 | 文本输入 | 匹配 service |
| 创建时间 | 日期范围 | 时间范围筛选 |

**列表字段**:

| 字段 | 说明 |
|------|------|
| 文件名 | fileName |
| 原名 | originalName |
| 文件后缀 | fileSuffix |
| 地址 | url（可点击预览） |
| 服务商 | service |
| 创建者 | createByName |
| 创建时间 | createTime |

### 2.2 文件删除

- 支持单条删除和批量删除
- 删除时同时从对象存储中删除实际文件

---

## 3. 配置管理

### 3.1 配置列表

管理 OSS 存储配置，支持多配置切换。

### 3.2 配置字段

| 字段 | 说明 |
|------|------|
| 配置名称 | 配置显示名称 |
| 访问站点 | OSS 访问端点 |
| 自定义域名 | 自定义 CDN 域名 |
| 存储桶 | Bucket 名称 |
| 前缀 | 文件路径前缀 |
| 访问密钥 | Access Key |
| 秘钥 | Secret Key |
| 是否 HTTPS | 是否使用 HTTPS |
| 域 | Region |
| 服务商 | 存储服务商 |
| 是否默认 | 是否为默认配置 |
| 状态 | 启用/停用 |

---

## 4. 权限控制

| 权限标识 | 功能 | 按钮/操作 |
|----------|------|-----------|
| `system:oss:list` | 查看文件列表 | 页面访问 |
| `system:oss:query` | 查看文件详情 | 查看详情 |
| `system:oss:remove` | 删除文件 | 删除按钮 |
| `system:ossConfig:list` | 查看配置列表 | 配置页面访问 |
| `system:ossConfig:add` | 新增配置 | 新增配置按钮 |
| `system:ossConfig:edit` | 修改配置 | 修改配置按钮 |
| `system:ossConfig:remove` | 删除配置 | 删除配置按钮 |

---

## 5. 数据模型

### 5.1 核心实体: SysOss

| 字段 | 类型 | 说明 |
|------|------|------|
| ossId | Long | 文件 ID（主键） |
| fileName | String | 文件名 |
| originalName | String | 原始文件名 |
| fileSuffix | String | 文件后缀 |
| url | String | 文件访问 URL |
| createByName | String | 创建者名称 |
| service | String | 服务商标识 |
| createTime | Date | 创建时间 |

### 5.2 核心实体: SysOssConfig

| 字段 | 类型 | 说明 |
|------|------|------|
| ossConfigId | Long | 配置 ID（主键） |
| configKey | String | 配置键 |
| accessKey | String | 访问密钥 |
| secretKey | String | 秘钥 |
| bucketName | String | 存储桶名称 |
| prefix | String | 前缀 |
| endpoint | String | 访问站点 |
| domain | String | 自定义域名 |
| isHttps | String | 是否 HTTPS |
| region | String | 域 |
| status | String | 状态 |
| ext1 | String | 扩展字段 1 |
| remark | String | 备注 |
| accessPolicy | String | 访问策略 |

---

## 6. 支持的服务商

| 服务商 | 标识 | 说明 |
|--------|------|------|
| MinIO | minio | 自建对象存储 |
| 阿里云 OSS | aliyun | 阿里云对象存储 |
| 腾讯云 COS | qcloud | 腾讯云对象存储 |
| 七牛云 | qiniu | 七牛云对象存储 |

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
