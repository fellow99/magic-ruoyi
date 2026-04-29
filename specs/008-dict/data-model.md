# 008-Dict 字典管理模块 - 数据模型

> 本文档定义字典管理模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 sys_dict_type - 字典类型表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| dict_id | bigint(20) | NOT NULL | - | 字典 ID（主键，雪花算法） |
| dict_name | varchar(100) | YES | '' | 字典名称 |
| dict_type | varchar(100) | YES | '' | 字典类型（唯一标识） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `dict_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_dict_type | dict_type | 唯一 | 字典类型唯一 |

---

### 1.2 sys_dict_data - 字典数据表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| dict_code | bigint(20) | NOT NULL | - | 字典数据 ID（主键，雪花算法） |
| dict_type | varchar(100) | YES | '' | 字典类型（关联 sys_dict_type） |
| dict_label | varchar(100) | YES | '' | 字典标签（显示文本） |
| dict_value | varchar(100) | YES | '' | 字典键值（存储值） |
| css_class | varchar(100) | YES | '' | 样式属性（CSS 类名） |
| list_class | varchar(100) | YES | '' | 回显样式（Tag 类型） |
| is_default | char(1) | YES | 'N' | 是否默认（Y=是, N=否） |
| dict_sort | int(11) | YES | 0 | 排序 |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `dict_code`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_dict_type | dict_type | 普通 | 按类型查询 |

---

## 2. 实体关系图

```
+------------------+       +------------------+
|  sys_dict_type   |       |  sys_dict_data   |
+------------------+       +------------------+
| PK dict_id       |<----+ | PK dict_code     |
|    dict_name     |      | |    dict_type (FK)|
|    dict_type     |      | |    dict_label    |
|    remark        |      | |    dict_value    |
+------------------+      | |    css_class     |
                          | |    list_class    |
                          | |    dict_sort     |
                          | |    remark        |
                          | +------------------+
                          |
                          + 1:N 关系
```

---

## 3. 后端对象

### 3.1 SysDictType - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {
    private Long dictId;
    private String dictName;
    private String dictType;
    private String remark;
}
```

### 3.2 SysDictData - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {
    private Long dictCode;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private String listClass;
    private String isDefault;
    private Integer dictSort;
    private String remark;
}
```

### 3.3 DictTypeVO / DictDataVO - 视图对象

```java
@Data
public class DictTypeVO {
    private Long dictId;
    private String dictName;
    private String dictType;
    private String remark;
    private Date createTime;
}

@Data
public class DictDataVO {
    private Long dictCode;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private String listClass;
    private Integer dictSort;
    private String remark;
    private Date createTime;
}
```

---

## 4. 前端类型

### 4.1 字典类型

```typescript
export interface DictTypeVO extends BaseEntity {
  dictId: number | string;
  dictName: string;
  dictType: string;
  remark: string;
}

export interface DictTypeForm {
  dictId: number | string | undefined;
  dictName: string;
  dictType: string;
  remark: string;
}

export interface DictTypeQuery extends PageQuery {
  dictName: string;
  dictType: string;
}
```

### 4.2 字典数据

```typescript
export interface DictDataVO extends BaseEntity {
  dictCode: string;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  listClass: ElTagType;
  dictSort: number;
  remark: string;
}

export interface DictDataForm {
  dictType?: string;
  dictCode: string | undefined;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  listClass: ElTagType;
  dictSort: number;
  remark: string;
}

export interface DictDataQuery extends PageQuery {
  dictName: string;
  dictType: string;
  dictLabel: string;
}
```

---

## 5. 字段枚举值

### 5.1 回显样式 (listClass)

| 值 | 标签 | Element Plus Tag 类型 |
|----|------|----------------------|
| default | 默认 | primary |
| primary | 主要 | primary |
| success | 成功 | success |
| info | 信息 | info |
| warning | 警告 | warning |
| danger | 危险 | danger |

### 5.2 是否默认 (is_default)

| 值 | 含义 |
|----|------|
| 'Y' | 是 |
| 'N' | 否 |

---

## 6. 数据流转

### 6.1 新增字典类型

```
前端 DictTypeForm
  → POST /system/dict/type
    → Service 校验 dictType 唯一性
    → 插入 sys_dict_type
  → 返回 R.ok()
```

### 6.2 新增字典数据

```
前端 DictDataForm
  → POST /system/dict/data
    → Service 插入 sys_dict_data
    → 清除 Redis 字典缓存
  → 返回 R.ok()
```

### 6.3 删除字典类型

```
前端 dictIds
  → DELETE /system/dict/type/{dictIds}
    → Service 级联删除 sys_dict_data
    → Service 删除 sys_dict_type
    → 清除 Redis 字典缓存
  → 返回 R.ok()
```

### 6.4 刷新缓存

```
前端 refreshCache()
  → DELETE /system/dict/type/refreshCache
    → Service 清除 Redis 中所有字典缓存
  → 返回 R.ok()
  → 前端 useDictStore().cleanDict()
```

---

## 7. 缓存机制

- 字典数据缓存在 Redis 中
- Key 格式: `dict:{dictType}`
- Value: 该字典类型下的所有字典数据列表（JSON 序列化）
- 新增/修改/删除字典数据时自动清除对应缓存
- 刷新缓存按钮可清除全部字典缓存
