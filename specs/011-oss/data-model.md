# 011-OSS 文件存储模块 - 数据模型

> 本文档定义文件存储模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_oss - OSS 文件存储表

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

**主键**: `oss_id`

---

### 1.2 sys_oss_config - OSS 配置表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| oss_config_id | bigint(20) | NOT NULL | - | 配置 ID（主键，雪花算法） |
| config_key | varchar(20) | YES | '' | 配置键 |
| access_key | varchar(255) | YES | '' | 访问密钥 |
| secret_key | varchar(255) | YES | '' | 秘钥 |
| bucket_name | varchar(255) | YES | '' | 存储桶名称 |
| prefix | varchar(255) | YES | '' | 前缀 |
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

**主键**: `oss_config_id`

---

## 2. 实体关系图

```
+------------------+       +------------------+
|   sys_oss        |       | sys_oss_config   |
+------------------+       +------------------+
| PK oss_id        |       | PK oss_config_id |
|    file_name     |       |    config_key    |
|    original_name |       |    access_key    |
|    file_suffix   |       |    secret_key    |
|    url           |       |    bucket_name   |
|    service ──────┼──────>|    endpoint      |
|    create_by     |       |    domain        |
+------------------+       |    is_https      |
                           |    region        |
                           |    status        |
                           |    access_policy |
                           +------------------+

sys_oss.service → sys_oss_config.config_key (逻辑关联)
```

---

## 3. 后端对象

### 3.1 SysOss - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oss")
public class SysOss extends BaseEntity {
    private Long ossId;
    private String fileName;
    private String originalName;
    private String fileSuffix;
    private String url;
    private String createByName;
    private String service;
}
```

### 3.2 SysOssConfig - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oss_config")
public class SysOssConfig extends BaseEntity {
    private Long ossConfigId;
    private String configKey;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String prefix;
    private String endpoint;
    private String domain;
    private String isHttps;
    private String region;
    private String status;
    private String ext1;
    private String remark;
    private String accessPolicy;
}
```

---

## 4. 前端类型

### 4.1 OssVO - 文件返回对象

```typescript
export interface OssVO extends BaseEntity {
  ossId: string | number;
  fileName: string;
  originalName: string;
  fileSuffix: string;
  url: string;
  createByName: string;
  service: string;
}
```

### 4.2 OssQuery - 文件查询对象

```typescript
export interface OssQuery extends PageQuery {
  fileName: string;
  originalName: string;
  fileSuffix: string;
  createTime: string;
  service: string;
  orderByColumn: string;
  isAsc: string;
}
```

---

## 5. 字段枚举值

### 5.1 服务商 (service / config_key)

| 值 | 含义 |
|----|------|
| minio | MinIO 对象存储 |
| aliyun | 阿里云 OSS |
| qcloud | 腾讯云 COS |
| qiniu | 七牛云 Kodo |

### 5.2 状态 (status)

| 值 | 含义 |
|----|------|
| '0' | 正常（启用） |
| '1' | 停用 |

### 5.3 是否 HTTPS (is_https)

| 值 | 含义 |
|----|------|
| 'Y' | 是 |
| 'N' | 否 |

### 5.4 访问策略 (access_policy)

| 值 | 含义 |
|----|------|
| '0' | 私有读写 |
| '1' | 公开读 |
| '2' | 自定义 |

---

## 6. 数据流转

### 6.1 文件上传

```
前端上传文件
  → POST /resource/oss/upload
    → Service 获取默认 OSS 配置
    → 上传文件到对象存储
    → 插入 sys_oss 记录
  → 返回文件信息（ossId, url, fileName 等）
```

### 6.2 文件删除

```
前端 ossIds
  → DELETE /resource/oss/{ossIds}
    → Service 查询 sys_oss 记录
    → 从对象存储删除实际文件
    → 删除 sys_oss 记录
  → 返回 R.ok()
```

### 6.3 配置管理

```
前端 OssConfigForm
  → POST/PUT/DELETE /resource/oss/config
    → Service 管理 sys_oss_config
  → 返回 R.ok()
```
