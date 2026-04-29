# 009-Config 参数配置模块 - 数据模型

> 本文档定义参数配置模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_config - 参数配置表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| config_id | bigint(20) | NOT NULL | - | 参数 ID（主键，雪花算法） |
| config_name | varchar(100) | YES | '' | 参数名称 |
| config_key | varchar(100) | YES | '' | 参数键名 |
| config_value | varchar(500) | YES | '' | 参数键值 |
| config_type | char(1) | YES | 'N' | 系统内置（Y=是, N=否） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `config_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_config_key | config_key | 唯一 | 参数键名唯一 |

---

## 2. 实体关系图

```
+------------------+
|   sys_config     |
+------------------+
| PK config_id     |
|    config_name   |
|    config_key    |
|    config_value  |
|    config_type   |
|    remark        |
+------------------+
```

---

## 3. 后端对象

### 3.1 SysConfig - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    private Long configId;
    private String configName;
    private String configKey;
    private String configValue;
    private String configType;
    private String remark;
}
```

### 3.2 ConfigVO - 视图对象

```java
@Data
public class ConfigVO {
    private Long configId;
    private String configName;
    private String configKey;
    private String configValue;
    private String configType;
    private String remark;
    private Date createTime;
}
```

---

## 4. 前端类型

### 4.1 ConfigVO - 参数返回对象

```typescript
export interface ConfigVO extends BaseEntity {
  configId: number | string;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  remark: string;
}
```

### 4.2 ConfigForm - 参数表单对象

```typescript
export interface ConfigForm {
  configId: number | string | undefined;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  remark: string;
}
```

### 4.3 ConfigQuery - 参数查询对象

```typescript
export interface ConfigQuery extends PageQuery {
  configName: string;
  configKey: string;
  configType: string;
}
```

---

## 5. 字段枚举值

### 5.1 系统内置 (config_type)

| 值 | 含义 | 字典类型 |
|----|------|----------|
| 'Y' | 是 | `sys_yes_no` |
| 'N' | 否 | `sys_yes_no` |

---

## 6. 数据流转

### 6.1 新增参数

```
前端 ConfigForm
  → POST /system/config
    → Service 校验 configKey 唯一性
    → 插入 sys_config
    → 清除 Redis 参数缓存
  → 返回 R.ok()
```

### 6.2 修改参数

```
前端 ConfigForm
  → PUT /system/config
    → Service 校验 configKey 唯一性（排除自身）
    → 更新 sys_config
    → 清除 Redis 参数缓存
  → 返回 R.ok()
```

### 6.3 按键名更新

```
前端 { configKey, configValue }
  → PUT /system/config/updateByKey
    → Service 按 configKey 查询
    → 更新 configValue
    → 清除 Redis 参数缓存
  → 返回 R.ok()
```

### 6.4 刷新缓存

```
前端 refreshCache()
  → DELETE /system/config/refreshCache
    → Service 清除 Redis 中所有参数缓存
  → 返回 R.ok()
```

---

## 7. 缓存机制

- 参数数据缓存在 Redis 中
- Key 格式: `config:{configKey}`
- Value: 参数键值（字符串）
- 新增/修改参数时自动清除对应缓存
- 刷新缓存按钮可清除全部参数缓存
