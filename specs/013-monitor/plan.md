# 系统监控模块技术实现方案（013-monitor/plan.md）

> magic-ruoyi 系统监控模块技术实现方案。描述在线用户、登录日志、操作日志、缓存监控的技术实现细节。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. Technical Context

### 1.1 模块定位

系统监控模块提供系统运行状态的可视化监控能力，包括在线用户管理、登录日志审计、操作日志追踪和 Redis 缓存状态查看。模块完全复用 RuoYi-Vue-Plus 上游实现，magic-ruoyi 项目零自定义代码。

### 1.2 上游依赖

| 上游模块 | 包前缀 | 说明 |
|----------|--------|------|
| `ruoyi-common-web` | `org.dromara.common.web` | 全局异常处理、响应封装 |
| `ruoyi-common-satoken` | `org.dromara.common.satoken` | Sa-Token 会话管理 |
| `ruoyi-common-redis` | `org.dromara.common.redis` | Redis 操作封装 |
| `ruoyi-common-log` | `org.dromara.common.log` | 操作日志 AOP 切面 |
| `ruoyi-system` | `org.dromara.system` | 登录日志/操作日志 Service/Mapper |
| `ruoyi-common-doc` | `org.dromara.common.doc` | OpenAPI 文档注解 |

### 1.3 技术栈

| 组件 | 技术 | 说明 |
|------|------|------|
| 会话存储 | Redis（Sa-Token） | Token 会话数据存储在 Redis 中 |
| 日志存储 | MySQL | `sys_logininfor`、`sys_oper_log` 表 |
| 缓存监控 | Redis INFO 命令 | 通过 Redisson 获取 Redis 服务器状态 |
| 在线用户 | Sa-Token Session | 通过 Sa-Token API 查询活跃会话 |

### 1.4 核心约束

- 本模块完全复用 RuoYi-Vue-Plus 上游实现，不编写自定义 Controller/Service/Mapper
- 前端页面复用 RuoYi-Vue-Plus-UI 结构，保持与上游一致性
- 所有接口通过 `@SaCheckPermission` 注解控制访问权限
- 在线用户数据来源于 Sa-Token 存储在 Redis 中的 Token 会话

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 直接复用上游模块，不引入额外抽象 |
| 约定优于配置 | 合规 | 遵循 RuoYi-Vue-Plus 的目录结构、命名规范、响应格式 |
| 实用优于完美 | 合规 | 监控功能按需使用，无需额外配置 |
| 安全优于便利 | 合规 | 所有监控接口均需权限校验，强制踢出操作记录审计日志 |
| 零样板代码 | 合规 | 上游已实现全部 CRUD，无需手写 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入，不复制上游源码 |
| 清晰模块边界 | 合规 | 监控功能归属 `org.dromara` 包，与自定义 `org.fellow99` 严格隔离 |
| 多租户优先 | 合规 | 登录日志和操作日志自动按租户隔离（MyBatis-Plus 租户插件） |

---

## 3. Research Findings

### 3.1 在线用户实现机制

在线用户数据来源于 Sa-Token 的 Token 会话存储。Sa-Token 将每个登录会话序列化后存储在 Redis 中，Key 格式为 `satoken:login:token:{tokenId}`。

**查询流程**:
1. 遍历 Redis 中所有 `satoken:login:token:*` Key
2. 反序列化获取 `LoginUser` 对象
3. 提取用户信息（用户名、部门、IP、浏览器、操作系统、登录时间）
4. 按用户名和 IP 筛选后分页返回

**强制踢出流程**:
1. 调用 `StpUtil.logout(tokenId)` 删除指定 Token 会话
2. Redis 中对应 Key 被删除
3. 用户下次请求时 Token 失效，自动跳转登录页

### 3.2 登录日志实现机制

登录日志通过事件驱动机制记录。

**记录流程**:
1. 登录成功/失败时，发布 `LogininforEvent` 事件
2. `SysLogininforServiceImpl` 监听事件
3. 解析请求信息（IP、浏览器、操作系统、登录地点）
4. 插入 `sys_logininfor` 表

**解锁机制**:
- 登录失败次数达到阈值后，用户被锁定（Redis 计数 Key: `password.error.count:{username}`）
- 解锁操作删除该 Redis Key，允许用户重新尝试登录

### 3.3 操作日志实现机制

操作日志通过 AOP 切面自动记录。

**记录流程**:
1. Controller 方法标注 `@Log(title = "模块名", businessType = BusinessType.XXX)`
2. AOP 切面拦截方法执行
3. 记录请求参数、响应结果、操作人、操作时间、耗时
4. 异步插入 `sys_oper_log` 表

### 3.4 缓存监控实现机制

缓存监控通过 Redis INFO 命令获取服务器状态。

**信息获取**:
- `INFO commandstats`: 命令执行统计
- `INFO keyspace`: 数据库 Key 分布
- `INFO server`: 服务器信息（版本、运行时间、内存）
- `INFO clients`: 客户端连接信息

---

## 4. Data Model

### 4.1 sys_logininfor（登录日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| info_id | BIGINT | 主键 |
| user_name | VARCHAR(50) | 用户名称 |
| ipaddr | VARCHAR(128) | 登录 IP 地址 |
| login_location | VARCHAR(255) | 登录地点 |
| browser | VARCHAR(50) | 浏览器类型 |
| os | VARCHAR(50) | 操作系统 |
| status | CHAR(1) | 登录状态（0=成功, 1=失败） |
| msg | VARCHAR(255) | 提示消息 |
| login_time | DATETIME | 登录时间 |
| tenant_id | VARCHAR(20) | 租户 ID |

### 4.2 sys_oper_log（操作日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| oper_id | BIGINT | 主键 |
| title | VARCHAR(50) | 模块标题 |
| business_type | INT(2) | 业务类型（0=其它, 1=新增, 2=修改, 3=删除） |
| method | VARCHAR(100) | 方法名称 |
| request_method | VARCHAR(10) | 请求方式 |
| operator_type | INT(1) | 操作类别（0=其它, 1=后台用户, 2=手机端用户） |
| oper_name | VARCHAR(50) | 操作人员 |
| dept_name | VARCHAR(50) | 部门名称 |
| oper_url | VARCHAR(255) | 请求 URL |
| oper_ip | VARCHAR(128) | 操作 IP |
| oper_location | VARCHAR(255) | 操作地点 |
| oper_param | TEXT | 请求参数 |
| json_result | TEXT | 返回参数 |
| status | INT(1) | 操作状态（0=正常, 1=异常） |
| error_msg | TEXT | 错误消息 |
| oper_time | DATETIME | 操作时间 |
| cost_time | BIGINT | 消耗时间（毫秒） |
| tenant_id | VARCHAR(20) | 租户 ID |

### 4.3 在线用户（非持久化，Redis 存储）

| 字段 | 类型 | 说明 |
|------|------|------|
| tokenId | String | 会话编号（Token ID） |
| deptName | String | 部门名称 |
| userName | String | 用户名 |
| ipaddr | String | 登录 IP |
| loginLocation | String | 登录地点 |
| browser | String | 浏览器 |
| os | String | 操作系统 |
| loginTime | Long | 登录时间戳 |

### 4.4 缓存监控（非持久化，Redis INFO）

| 字段 | 类型 | 说明 |
|------|------|------|
| commandStats | List | 命令执行统计 |
| dbSize | Long | Key 总数 |
| info | Map | 服务器信息 |

---

## 5. Interface Contracts

### 5.1 在线用户 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/monitor/online/list` | `monitor:online:list` | 查询在线用户列表 |
| DELETE | `/monitor/online/{tokenId}` | `monitor:online:forceLogout` | 强制踢出用户 |
| GET | `/monitor/online` | - | 获取当前用户在线设备 |
| DELETE | `/monitor/online/myself/{tokenId}` | - | 删除当前用户在线设备 |

### 5.2 登录日志 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/monitor/logininfor/list` | `monitor:logininfor:list` | 查询登录日志列表 |
| DELETE | `/monitor/logininfor/{infoIds}` | `monitor:logininfor:remove` | 删除登录日志 |
| DELETE | `/monitor/logininfor/clean` | `monitor:logininfor:remove` | 清空登录日志 |
| PUT | `/monitor/logininfor/unlock/{userName}` | `monitor:logininfor:unlock` | 解锁用户 |
| GET | `/monitor/logininfor/{infoId}` | `monitor:logininfor:list` | 查询登录日志详情 |

### 5.3 操作日志 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/monitor/operlog/list` | `monitor:operlog:list` | 查询操作日志列表 |
| DELETE | `/monitor/operlog/{operIds}` | `monitor:operlog:remove` | 删除操作日志 |
| DELETE | `/monitor/operlog/clean` | `monitor:operlog:remove` | 清空操作日志 |
| GET | `/monitor/operlog/{operId}` | `monitor:operlog:list` | 查询操作日志详情 |

### 5.4 缓存监控 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/monitor/cache` | `monitor:cache:list` | 获取缓存详细信息 |
| GET | `/monitor/cache/getNames` | `monitor:cache:list` | 获取缓存名称列表 |
| GET | `/monitor/cache/getKeys/{cacheName}` | `monitor:cache:list` | 获取指定缓存的 Key 列表 |
| GET | `/monitor/cache/getValue/{cacheName}/{cacheKey}` | `monitor:cache:list` | 获取缓存值 |
| DELETE | `/monitor/cache/clearCacheName/{cacheName}` | `monitor:cache:list` | 清理指定名称缓存 |
| DELETE | `/monitor/cache/clearCacheKey/{cacheName}/{cacheKey}` | `monitor:cache:list` | 清理指定 Key 缓存 |
| DELETE | `/monitor/cache/clearCacheAll` | `monitor:cache:list` | 清理全部缓存 |

---

## 6. Implementation Strategy

### 6.1 后端实现

本模块完全复用 RuoYi-Vue-Plus 上游实现，无需编写自定义代码。

**入口 Controller（上游）**:

| Controller | 包路径 | 职责 |
|------------|--------|------|
| `SysUserOnlineController` | `org.dromara.web.controller.monitor` | 在线用户管理 |
| `SysLogininforController` | `org.dromara.web.controller.monitor` | 登录日志管理 |
| `SysOperlogController` | `org.dromara.web.controller.monitor` | 操作日志管理 |
| `CacheController` | `org.dromara.web.controller.monitor` | 缓存监控 |

**关键 Service（上游）**:

| Service | 包路径 | 职责 |
|---------|--------|------|
| `ISysLogininforService` | `org.dromara.system.service` | 登录日志 CRUD |
| `ISysOperLogService` | `org.dromara.system.service` | 操作日志 CRUD |

**关键 Mapper（上游）**:

| Mapper | 包路径 | 职责 |
|--------|--------|------|
| `SysLogininforMapper` | `org.dromara.system.mapper` | 登录日志数据访问 |
| `SysOperLogMapper` | `org.dromara.system.mapper` | 操作日志数据访问 |

### 6.2 前端实现

前端页面复用 RuoYi-Vue-Plus-UI 结构，已存在于 `magic-ruoyi-web/src/views/monitor/` 目录。

**页面清单**:

| 页面 | 路径 | 组件 |
|------|------|------|
| 在线用户 | `views/monitor/online/index.vue` | 列表 + 强制踢出 |
| 登录日志 | `views/monitor/logininfor/index.vue` | 列表 + 删除 + 清空 + 解锁 |
| 操作日志 | `views/monitor/operlog/index.vue` | 列表 + 删除 + 清空 + 详情弹窗 |
| 缓存监控 | `views/monitor/cache/index.vue` | 状态展示 + Key 浏览 + 清理 |
| 服务监控 | `views/monitor/admin/index.vue` | Spring Boot Admin iframe |
| 任务调度 | `views/monitor/snailjob/index.vue` | SnailJob 控制台 iframe |

**API 封装**:

| API 模块 | 路径 | 说明 |
|----------|------|------|
| 在线用户 | `src/api/monitor/online/index.ts` | list, forceLogout, getOnline, delOnline |
| 登录日志 | `src/api/monitor/loginInfo/index.ts` | list, delLogininfor, cleanLogininfor, unlock |
| 操作日志 | `src/api/monitor/operlog/index.ts` | list, delOperlog, cleanOperlog |
| 缓存监控 | `src/api/monitor/cache/index.ts` | getCache, listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll |

### 6.3 权限配置

权限通过菜单表 `sys_menu` 配置，与后端 `@SaCheckPermission` 注解保持一致。

**权限标识清单**:

| 权限标识 | 功能 | 页面 |
|----------|------|------|
| `monitor:online:list` | 查看在线用户 | 在线用户 |
| `monitor:online:forceLogout` | 强制踢出 | 在线用户 |
| `monitor:logininfor:list` | 查看登录日志 | 登录日志 |
| `monitor:logininfor:remove` | 删除/清空登录日志 | 登录日志 |
| `monitor:logininfor:unlock` | 解锁用户 | 登录日志 |
| `monitor:operlog:list` | 查看操作日志 | 操作日志 |
| `monitor:operlog:remove` | 删除/清空操作日志 | 操作日志 |
| `monitor:cache:list` | 查看缓存 | 缓存监控 |

### 6.4 配置项

```yaml
# application.yml 相关配置
sa-token:
  token-name: Authorization
  is-concurrent: true
  is-share: false

# Redis 配置（缓存监控依赖）
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

## 7. Testing Considerations

### 7.1 后端测试

| 测试场景 | 测试方法 |
|----------|----------|
| 在线用户列表查询 | Mock Sa-Token Session，验证返回数据格式 |
| 强制踢出用户 | 验证 Token 被删除，用户无法继续访问 |
| 登录日志查询 | 插入测试数据，验证分页和筛选 |
| 登录日志清空 | 验证清空后列表为空 |
| 解锁用户 | 验证 Redis 错误计数 Key 被删除 |
| 操作日志查询 | 验证 AOP 切面正确记录日志 |
| 缓存监控 | Mock Redis INFO 命令，验证返回数据 |

### 7.2 前端测试

| 测试场景 | 测试方法 |
|----------|----------|
| 在线用户列表渲染 | Mock API 返回，验证表格显示 |
| 强制踢出确认弹窗 | 验证点击后调用 forceLogout API |
| 登录日志筛选 | 验证用户名和状态筛选参数传递 |
| 操作日志详情弹窗 | 验证点击详情后弹窗展示完整信息 |
| 缓存监控图表 | 验证 ECharts 命令统计柱状图渲染 |

### 7.3 集成测试

| 测试场景 | 验证点 |
|----------|--------|
| 登录成功后在线用户列表增加 | Token 写入 Redis，列表查询返回新会话 |
| 强制踢出后用户 Token 失效 | 用户请求返回 401 |
| 登录失败记录写入日志 | `sys_logininfor` 表新增记录 |
| 操作日志自动记录 | 标注 `@Log` 的接口执行后，`sys_oper_log` 表新增记录 |
| 缓存清理后 Key 消失 | Redis 中对应 Key 被删除 |

---

## 8. File Inventory

### 8.1 后端文件（上游 RuoYi-Vue-Plus）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| `SysUserOnlineController.java` | `org.dromara.web.controller.monitor` | 在线用户 Controller |
| `SysLogininforController.java` | `org.dromara.web.controller.monitor` | 登录日志 Controller |
| `SysOperlogController.java` | `org.dromara.web.controller.monitor` | 操作日志 Controller |
| `CacheController.java` | `org.dromara.web.controller.monitor` | 缓存监控 Controller |
| `ISysLogininforService.java` | `org.dromara.system.service` | 登录日志 Service 接口 |
| `SysLogininforServiceImpl.java` | `org.dromara.system.service.impl` | 登录日志 Service 实现 |
| `ISysOperLogService.java` | `org.dromara.system.service` | 操作日志 Service 接口 |
| `SysOperLogServiceImpl.java` | `org.dromara.system.service.impl` | 操作日志 Service 实现 |
| `SysLogininforMapper.java` | `org.dromara.system.mapper` | 登录日志 Mapper |
| `SysOperLogMapper.java` | `org.dromara.system.mapper` | 操作日志 Mapper |
| `SysLogininfor.java` | `org.dromara.system.domain` | 登录日志实体 |
| `SysOperLog.java` | `org.dromara.system.domain` | 操作日志实体 |
| `Log.java` | `org.dromara.common.log.annotation` | 操作日志注解 |
| `LogAspect.java` | `org.dromara.common.log.aspect` | 操作日志 AOP 切面 |
| `LogininforEvent.java` | `org.dromara.common.log.event` | 登录日志事件 |

### 8.2 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| `index.vue` | `src/views/monitor/online/` | 在线用户页面 |
| `index.vue` | `src/views/monitor/logininfor/` | 登录日志页面 |
| `index.vue` | `src/views/monitor/operlog/` | 操作日志页面 |
| `oper-info-dialog.vue` | `src/views/monitor/operlog/` | 操作日志详情弹窗 |
| `index.vue` | `src/views/monitor/cache/` | 缓存监控页面 |
| `index.vue` | `src/views/monitor/admin/` | Spring Boot Admin 页面 |
| `index.vue` | `src/views/monitor/snailjob/` | SnailJob 控制台页面 |
| `index.ts` | `src/api/monitor/online/` | 在线用户 API |
| `types.ts` | `src/api/monitor/online/` | 在线用户类型 |
| `index.ts` | `src/api/monitor/loginInfo/` | 登录日志 API |
| `types.ts` | `src/api/monitor/loginInfo/` | 登录日志类型 |
| `index.ts` | `src/api/monitor/operlog/` | 操作日志 API |
| `types.ts` | `src/api/monitor/operlog/` | 操作日志类型 |
| `index.ts` | `src/api/monitor/cache/` | 缓存监控 API |
| `types.ts` | `src/api/monitor/cache/` | 缓存监控类型 |

### 8.3 数据库表

| 表名 | 说明 | SQL 文件 |
|------|------|----------|
| `sys_logininfor` | 登录日志表 | `sql/magic-ruoyi.sql` |
| `sys_oper_log` | 操作日志表 | `sql/magic-ruoyi.sql` |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
