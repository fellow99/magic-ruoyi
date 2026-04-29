# 012-Client 客户端管理模块 - 数据模型

> 本文档定义客户端管理模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_client - 客户端管理表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | bigint(20) | NOT NULL | - | 主键 ID（雪花算法） |
| client_id | varchar(255) | NOT NULL | - | 客户端 ID（UUID） |
| client_key | varchar(255) | YES | '' | 客户端 Key |
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

**主键**: `id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_client_id | client_id | 唯一 | 客户端 ID 唯一 |
| idx_client_key | client_key | 唯一 | 客户端 Key 唯一 |

---

## 2. 实体关系图

```
+------------------+
|   sys_client     |
+------------------+
| PK id            |
|    client_id     |
|    client_key    |
|    client_secret |
|    grant_type    |
|    device_type   |
|    active_timeout|
|    timeout       |
|    status        |
+------------------+
        |
        | 登录时校验
        ▼
+------------------+
|   登录流程        │
│                  │
│  1. 校验 clientId │
│  2. 校验 grantType│
│  3. 校验 status   │
│  4. 获取 Token 参数│
+------------------+
```

---

## 3. 后端对象

### 3.1 SysClient - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_client")
public class SysClient extends BaseEntity {
    private Long id;
    private String clientId;
    private String clientKey;
    private String clientSecret;
    private String grantType;
    private String deviceType;
    private Long activeTimeout;
    private Long timeout;
    private String status;
    private String delFlag;
}
```

### 3.2 ClientVO - 视图对象

```java
@Data
public class ClientVO {
    private Long id;
    private String clientId;
    private String clientKey;
    private String clientSecret;
    private List<String> grantTypeList;
    private String deviceType;
    private Long activeTimeout;
    private Long timeout;
    private String status;
}
```

---

## 4. 前端类型

### 4.1 ClientVO - 客户端返回对象

```typescript
export interface ClientVO {
  id: string | number;
  clientId: string;
  clientKey: string;
  clientSecret: string;
  grantTypeList: string[];
  deviceType: string;
  activeTimeout: number;
  timeout: number;
  status: string;
}
```

### 4.2 ClientForm - 客户端表单对象

```typescript
export interface ClientForm extends BaseEntity {
  id?: string | number;
  clientId?: string | number;
  clientKey?: string;
  clientSecret?: string;
  grantTypeList?: string[];
  deviceType?: string;
  activeTimeout?: number;
  timeout?: number;
  status?: string;
}
```

### 4.3 ClientQuery - 客户端查询对象

```typescript
export interface ClientQuery extends PageQuery {
  clientId?: string | number;
  clientKey?: string;
  clientSecret?: string;
  grantType?: string;
  deviceType?: string;
  activeTimeout?: number;
  timeout?: number;
  status?: string;
}
```

---

## 5. 字段枚举值

### 5.1 状态 (status)

| 值 | 含义 |
|----|------|
| '0' | 正常（启用） |
| '1' | 停用 |

### 5.2 删除标志 (del_flag)

| 值 | 含义 |
|----|------|
| '0' | 存在 |
| '1' | 已删除 |

### 5.3 授权类型 (grant_type)

| 值 | 对应策略 | 说明 |
|----|----------|------|
| password | PasswordAuthStrategy | 用户名密码登录 |
| sms | SmsAuthStrategy | 短信验证码登录 |
| email | EmailAuthStrategy | 邮箱验证码登录 |
| social | SocialAuthStrategy | 第三方社交登录 |
| xcx | XcxAuthStrategy | 微信小程序登录 |

### 5.4 设备类型 (device_type)

| 值 | 说明 |
|----|------|
| pc | PC 端 |
| app | 移动端 APP |
| xcx | 小程序 |

---

## 6. 数据流转

### 6.1 新增客户端

```
前端 ClientForm
  → POST /system/client
    → Service 生成 clientId (UUID)
    → Service 插入 sys_client
  → 返回 R.ok()
```

### 6.2 修改客户端

```
前端 ClientForm
  → PUT /system/client
    → Service 更新 sys_client
  → 返回 R.ok()
```

### 6.3 状态切换

```
前端 { clientId, status }
  → PUT /system/client/changeStatus
    → Service 按 clientId 更新 status
  → 返回 R.ok()
```

### 6.4 删除客户端

```
前端 ids
  → DELETE /system/client/{ids}
    → Service 逻辑删除 sys_client (del_flag='1')
  → 返回 R.ok()
```

### 6.5 登录时客户端校验

```
登录请求
  → 按 clientId 查询 sys_client
  → 校验 client 是否存在
  → 校验 grantType 是否在 grantTypeList 中
  → 校验 status 是否为 '0'
  → 获取 activeTimeout 和 timeout 用于 Token 配置
```
