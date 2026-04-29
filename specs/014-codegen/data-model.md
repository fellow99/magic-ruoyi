# 014-Codegen 代码生成模块 - 数据模型

> 本文档定义代码生成模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

### 1.1 gen_table - 代码生成业务表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| table_id | bigint(20) | NOT NULL | - | 表 ID（主键，雪花算法） |
| data_name | varchar(200) | YES | 'master' | 数据源名称 |
| table_name | varchar(200) | YES | '' | 数据库表名 |
| table_comment | varchar(500) | YES | '' | 表描述 |
| sub_table_name | varchar(64) | YES | NULL | 关联子表的表名 |
| sub_table_fk_name | varchar(64) | YES | NULL | 子表关联的外键名 |
| class_name | varchar(100) | YES | '' | Java 类名 |
| tpl_category | varchar(200) | YES | 'crud' | 模板分类（crud/tree/sub） |
| package_name | varchar(100) | YES | NULL | 生成包名 |
| module_name | varchar(30) | YES | NULL | 模块名 |
| business_name | varchar(30) | YES | NULL | 业务名 |
| function_name | varchar(50) | YES | NULL | 功能描述 |
| function_author | varchar(50) | YES | NULL | 作者 |
| gen_type | char(1) | YES | '0' | 生成方式（0=下载, 1=自定义路径） |
| gen_path | varchar(200) | YES | '/' | 自定义生成路径 |
| options | varchar(2000) | YES | NULL | JSON 扩展配置（树形/菜单参数） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `table_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_table_name | table_name | 唯一 | 表名唯一（按数据源） |

---

### 1.2 gen_table_column - 代码生成业务表字段

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| column_id | bigint(20) | NOT NULL | - | 字段 ID（主键，雪花算法） |
| table_id | bigint(20) | YES | NULL | 所属表 ID（外键） |
| column_name | varchar(200) | YES | NULL | 数据库列名 |
| column_comment | varchar(500) | YES | NULL | 列描述 |
| column_type | varchar(100) | YES | NULL | 数据库列类型 |
| java_type | varchar(500) | YES | NULL | Java 类型 |
| java_field | varchar(200) | YES | NULL | Java 属性名 |
| is_pk | char(1) | YES | NULL | 是否主键（1=是） |
| is_increment | char(1) | YES | NULL | 是否自增（1=是） |
| is_required | char(1) | YES | NULL | 是否必填（1=是） |
| is_insert | char(1) | YES | NULL | 是否插入字段（1=是） |
| is_edit | char(1) | YES | NULL | 是否编辑字段（1=是） |
| is_list | char(1) | YES | NULL | 是否列表字段（1=是） |
| is_query | char(1) | YES | NULL | 是否查询字段（1=是） |
| query_type | varchar(200) | YES | 'EQ' | 查询方式（EQ/NE/GT/GE/LT/LE/LIKE/BETWEEN） |
| html_type | varchar(200) | YES | NULL | HTML 组件类型 |
| dict_type | varchar(200) | YES | '' | 字典类型 |
| sort | int(11) | YES | NULL | 排序号 |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |

**主键**: `column_id`

**外键**: `table_id` → `gen_table.table_id`

**索引建议**:

| 索引名 | 列 | 类型 | 说明 |
|--------|-----|------|------|
| idx_table_id | table_id | 普通 | 按表 ID 查询字段 |

---

## 2. 实体关系图

```
+------------------+       +----------------------+
|    gen_table     |       |  gen_table_column    |
+------------------+       +----------------------+
| PK table_id      |<----+ | PK column_id         |
|    data_name     |      | |    table_id (FK)     |
|    table_name    |      | |    column_name       |
|    table_comment |      | |    column_type       |
|    class_name    |      | |    java_type         |
|    tpl_category  |      | |    java_field        |
|    package_name  |      | |    is_pk             |
|    module_name   |      | |    is_insert         |
|    business_name |      | |    is_edit           |
|    function_name |      | |    is_list           |
|    function_author|     | |    is_query          |
|    gen_type      |      | |    query_type        |
|    gen_path      |      | |    html_type         |
|    options       |      | |    dict_type         |
+------------------+      | |    sort              |
                          | +----------------------+
                          |
                          + 1:N 关系
```

---

## 3. 后端对象

### 3.1 GenTable - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gen_table")
public class GenTable extends BaseEntity {
    private Long tableId;
    private String dataName;
    private String tableName;
    private String tableComment;
    private String subTableName;
    private String subTableFkName;
    private String className;
    private String tplCategory;
    private String packageName;
    private String moduleName;
    private String businessName;
    private String functionName;
    private String functionAuthor;
    private String genType;
    private String genPath;
    private String options;
    private List<GenTableColumn> columns;
}
```

### 3.2 GenTableColumn - 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gen_table_column")
public class GenTableColumn extends BaseEntity {
    private Long columnId;
    private Long tableId;
    private String columnName;
    private String columnComment;
    private String columnType;
    private String javaType;
    private String javaField;
    private String isPk;
    private String isIncrement;
    private String isRequired;
    private String isInsert;
    private String isEdit;
    private String isList;
    private String isQuery;
    private String queryType;
    private String htmlType;
    private String dictType;
    private Integer sort;
}
```

---

## 4. 前端类型

### 4.1 表相关类型（`@/api/tool/gen/types.ts`）

```typescript
// 已导入表视图对象
export interface TableVO extends BaseEntity {
  tableId: number | string;
  dataName: string;
  tableName: string;
  tableComment: string;
  className: string;
  tplCategory: string;
  packageName: string;
  moduleName: string;
  businessName: string;
  functionName: string;
  functionAuthor: string;
  genType: string;
  genPath: string;
}

// 已导入表查询参数
export interface TableQuery extends PageQuery {
  tableName: string;
  tableComment: string;
  dataName: string;
}

// 数据库表视图对象
export interface DbTableVO {
  tableName: string;
  tableComment: string;
  createTime: string;
  updateTime: string;
}

// 数据库表查询参数
export interface DbTableQuery extends PageQuery {
  dataName: string;
  tableName: string;
  tableComment: string;
}

// 数据库字段视图对象
export interface DbColumnVO {
  columnId: number | string;
  tableId: number | string;
  columnName: string;
  columnComment: string;
  columnType: string;
  javaType: string;
  javaField: string;
  isPk: string;
  isIncrement: string;
  isRequired: string;
  isInsert: string;
  isEdit: string;
  isList: string;
  isQuery: string;
  queryType: string;
  htmlType: string;
  dictType: string;
  sort: number;
}

// 生成表详情响应
export interface GenTableVO {
  info: GenTable;
  rows: DbColumnVO[];
  tables: DbTableVO[];
}

// 表编辑表单
export interface DbTableForm {
  tableId: number | string;
  tableName: string;
  tableComment: string;
  className: string;
  tplCategory: string;
  packageName: string;
  moduleName: string;
  businessName: string;
  functionName: string;
  functionAuthor: string;
  genType: string;
  genPath: string;
  columns: DbColumnForm[];
  params: DbParamForm;
}

// 字段编辑表单
export interface DbColumnForm {
  columnId: number | string;
  columnName: string;
  columnComment: string;
  columnType: string;
  javaType: string;
  javaField: string;
  isPk: string;
  isIncrement: string;
  isRequired: string;
  isInsert: string;
  isEdit: string;
  isList: string;
  isQuery: string;
  queryType: string;
  htmlType: string;
  dictType: string;
  sort: number;
}

// 树形/菜单参数
export interface DbParamForm {
  treeCode?: string;
  treeParentCode?: string;
  treeName?: string;
  parentMenuId?: string;
}
```

---

## 5. 字段枚举值

### 5.1 模板分类 (tpl_category)

| 值 | 说明 |
|------|------|
| crud | 单表 CRUD |
| tree | 树形表 |
| sub | 主子表 |

### 5.2 生成方式 (gen_type)

| 值 | 说明 |
|------|------|
| '0' | 下载 ZIP |
| '1' | 自定义路径 |

### 5.3 查询方式 (query_type)

| 值 | 说明 |
|------|------|
| EQ | 等于 |
| NE | 不等于 |
| GT | 大于 |
| GE | 大于等于 |
| LT | 小于 |
| LE | 小于等于 |
| LIKE | 模糊匹配 |
| BETWEEN | 范围查询 |

### 5.4 HTML 组件类型 (html_type)

| 值 | 说明 |
|------|------|
| input | 文本框 |
| textarea | 文本域 |
| select | 下拉框 |
| radio | 单选框 |
| checkbox | 复选框 |
| datetime | 日期控件 |
| imageUpload | 图片上传 |
| fileUpload | 文件上传 |
| editor | 富文本控件 |

---

## 6. 数据流转

### 6.1 导入表

```
前端 { tables, dataName }
  → POST /tool/gen/importTable
    → Service 读取数据库表结构
    → 插入 gen_table
    → 插入 gen_table_column
  → 返回 R.ok()
```

### 6.2 编辑生成配置

```
前端 DbTableForm
  → PUT /tool/gen
    → Service 更新 gen_table
    → Service 批量更新 gen_table_column
  → 返回 R.ok()
```

### 6.3 预览生成代码

```
前端 tableId
  → GET /tool/gen/preview/{tableId}
    → Service 加载表和字段配置
    → Velocity 模板渲染
  → 返回 { "domain.java": "...", "mapper.java": "...", ... }
```

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
