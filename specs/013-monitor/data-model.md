# 013-Monitor 系统监控模块 - 数据模型

> 本文档定义系统监控模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_logininfor - 登录日志表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| info_id | bigint(20) | NOT NULL | - | 访问 ID（主键，雪花算法） |
| tenant_id | varchar(20) | YES | '000000' | 租户编号 |
| user_name | varchar(50) | YES | '' | 用户账号 |
| status | char(1) | YES | '0' | 登录状态（0=成功, 1=失败） |
| ipaddr | varchar(128) | YES | '' | 登录 IP 地址 |
| login_location | varchar(255) | YES | '' | 登录地点 |
| browser | varchar(50) | YES | '' | 浏览器类型 |
| os | varchar(50) | YES | '' | 操作系统 |
| msg | varchar(255) | YES | '' | 提示消息 |
| login_time | datetime | YES | NULL | 登录时间 |

**主键**: `info_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_user_name | user_name | 普通 | 按用户查询 |
| idx_login_time | login_time | 普通 | 按时间排序 |
| idx_status | status | 普通 | 按状态过滤 |

---

### 1.2 sys_oper_log - 操作日志表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| oper_id | bigint(20) | NOT NULL | - | 日志主键（雪花算法） |
| tenant_id | varchar(20) | YES | '000000' | 租户编号 |
| title | varchar(50) | YES | '' | 模块标题 |
| business_type | int(11) | YES | 0 | 业务类型（0=其它, 1=新增, 2=修改, 3=删除） |
| method | varchar(100) | YES | '' | 方法名称 |
| request_method | varchar(10) | YES | '' | 请求方式 |
| operator_type | int(11) | YES | 0 | 操作类别（0=其它, 1=后台用户, 2=手机端用户） |
| oper_name | varchar(50) | YES | '' | 操作人员 |
| dept_name | varchar(50) | YES | '' | 部门名称 |
| oper_url | varchar(255) | YES | '' | 请求 URL |
| oper_ip | varchar(128) | YES | '' | 操作地址 |
| oper_location | varchar(255) | YES | '' | 操作地点 |
| oper_param | varchar(2000) | YES | '' | 请求参数 |
| json_result | varchar(2000) | YES | '' | 返回参数 |
| status | int(11) | YES | 0 | 操作状态（0=正常, 1=异常） |
| error_msg | varchar(2000) | YES | '' | 错误消息 |
| oper_time | datetime | YES | NULL | 操作时间 |
| cost_time | bigint(20) | YES | 0 | 消耗时间（毫秒） |

**主键**: `oper_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_oper_name | oper_name | 普通 | 按操作人查询 |
| idx_oper_time | oper_time | 普通 | 按时间排序 |
| idx_business_type | business_type | 普通 | 按业务类型过滤 |

---

## 2. 实体关系图

```
+------------------+       +------------------+
| sys_logininfor   |       |   sys_oper_log   |
+------------------+       +------------------+
| PK info_id       |       | PK oper_id       |
|    tenant_id     |       |    tenant_id     |
|    user_name     |       |    title         |
|    status        |       |    business_type |
|    ipaddr        |       |    oper_name     |
|    login_location|       |    oper_ip       |
|    browser       |       |    oper_param    |
|    os            |       |    json_result   |
|    msg           |       |    status        |
|    login_time    |       |    oper_time     |
+------------------+       |    cost_time     |
                           +------------------+

两个表独立，无外键关联
数据通过 AOP 切面自动记录
```

---

## 3. 后端对象

### 3.1 SysLogininfor - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_logininfor")
public class SysLogininfor extends BaseEntity {
    private Long infoId;
    private String tenantId;
    private String userName;
    private String status;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
    private String msg;
    private Date loginTime;
}
```

### 3.2 SysOperLog - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oper_log")
public class SysOperLog extends BaseEntity {
    private Long operId;
    private String tenantId;
    private String title;
    private Integer businessType;
    private String method;
    private String requestMethod;
    private Integer operatorType;
    private String operName;
    private String deptName;
    private String operUrl;
    private String operIp;
    private String operLocation;
    private String operParam;
    private String jsonResult;
    private Integer status;
    private String errorMsg;
    private Date operTime;
    private Long costTime;
}
```

### 3.3 CacheVO - 缓存视图对象

```java
@Data
public class CacheVO {
    private List<Map<String, String>> commandStats;
    private Long dbSize;
    private Map<String, String> info;
}
```

---

## 4. 前端类型

### 4.1 在线用户

```typescript
export interface OnlineVO extends BaseEntity {
  tokenId: string;
  deptName: string;
  userName: string;
  ipaddr: string;
  loginLocation: string;
  browser: string;
  os: string;
  loginTime: number;
}

export interface OnlineQuery extends PageQuery {
  ipaddr: string;
  userName: string;
}
```

### 4.2 登录日志

```typescript
export interface LoginInfoVO {
  infoId: string | number;
  tenantId: string | number;
  userName: string;
  status: string;
  ipaddr: string;
  loginLocation: string;
  browser: string;
  os: string;
  msg: string;
  loginTime: string;
}

export interface LoginInfoQuery extends PageQuery {
  ipaddr: string;
  userName: string;
  status: string;
  orderByColumn: string;
  isAsc: string;
}
```

### 4.3 操作日志

```typescript
export interface OperLogVO extends BaseEntity {
  operId: string | number;
  tenantId: string;
  title: string;
  businessType: number;
  businessTypes: number[] | undefined;
  method: string;
  requestMethod: string;
  operatorType: number;
  operName: string;
  deptName: string;
  operUrl: string;
  operIp: string;
  operLocation: string;
  operParam: string;
  jsonResult: string;
  status: number;
  errorMsg: string;
  operTime: string;
  costTime: number;
}

export interface OperLogQuery extends PageQuery {
  operIp: string;
  title: string;
  operName: string;
  businessType: string;
  status: string;
  orderByColumn: string;
  isAsc: string;
}
```

### 4.4 缓存监控

```typescript
export interface CacheVO {
  commandStats: Array<{ name: string; value: string }>;
  dbSize: number;
  info: { [key: string]: string };
}
```

---

## 5. 字段枚举值

### 5.1 登录状态 (status)

| 值 | 含义 |
|----|------|
| '0' | 登录成功 |
| '1' | 登录失败 |

### 5.2 操作状态 (status)

| 值 | 含义 |
|----|------|
| 0 | 操作正常 |
| 1 | 操作异常 |

### 5.3 业务类型 (business_type)

| 值 | 含义 |
|----|------|
| 0 | 其它 |
| 1 | 新增 |
| 2 | 修改 |
| 3 | 删除 |
| 4 | 授权 |
| 5 | 导出 |
| 6 | 导入 |
| 7 | 强退 |
| 8 | 生成代码 |
| 9 | 清空数据 |

### 5.4 操作类别 (operator_type)

| 值 | 含义 |
|----|------|
| 0 | 其它 |
| 1 | 后台用户 |
| 2 | 手机端用户 |

---

## 6. 数据流转

### 6.1 登录日志记录

```
用户登录
  → SysLoginService 记录登录日志
    → 插入 sys_logininfor
    → 包含 IP、浏览器、操作系统、登录结果
```

### 6.2 操作日志记录

```
用户操作（AOP 切面拦截）
  → LogAspect 捕获操作信息
    → 记录请求参数、返回结果、耗时
    → 插入 sys_oper_log
```

### 6.3 在线用户查询

```
GET /monitor/online/list
  → 从 Redis 读取 Sa-Token 会话数据
  → 解析 Token 信息（用户、IP、浏览器等）
  → 返回在线用户列表
```

### 6.4 缓存监控

```
GET /monitor/cache
  → Redis INFO 命令获取服务器信息
  → DBSIZE 命令获取 Key 总数
  → INFO COMMANDSTATS 获取命令统计
  → 返回 CacheVO
```

---

## 7. 日志记录机制

### 7.1 登录日志

- 由 `SysLoginService` 在登录流程中主动记录
- 登录成功和失败都会记录
- 记录内容包括 IP 归属地（通过 ip2region.xdb 解析）

### 7.2 操作日志

- 通过 AOP 切面 `@Log` 注解自动记录
- 拦截标注了 `@Log` 的 Controller 方法
- 异步写入数据库，不影响主流程性能
- 记录请求参数和返回结果（JSON 格式）
