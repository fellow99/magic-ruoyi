# 代码生成模块规格文档（014-codegen/spec.md）

> magic-ruoyi 代码生成模块。定义数据库表代码自动生成、CRUD 模板配置与多数据源支持。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 模块概述

代码生成模块负责将数据库表结构自动转换为完整的 CRUD 代码，包括后端 Java 实体类、Mapper、Service、Controller，以及前端 Vue 页面、API 封装和路由配置。模块支持多数据源、树形结构生成、主子表关联等场景。

### 1.1 技术栈

| 组件 | 技术选型 |
|------|----------|
| 模板引擎 | Velocity（.vm 模板文件） |
| ORM 框架 | MyBatis-Plus |
| 前端框架 | Vue 3 + TypeScript + Element Plus |
| 数据源 | 多数据源支持（通过 dataName 区分） |

### 1.2 核心能力

- **表导入**: 从数据库读取表结构，导入到代码生成配置表
- **字段配置**: 编辑每个字段的 Java 类型、HTML 组件、查询方式、字典绑定等
- **生成配置**: 设置包名、模块名、业务名、作者、生成路径等
- **代码预览**: 在线预览生成的全部代码文件
- **代码下载**: 打包为 ZIP 文件下载
- **代码生成到路径**: 直接生成到服务器指定目录
- **数据库同步**: 表结构变更后同步更新生成配置
- **树形表支持**: 支持树形结构（treeCode、treeParentCode、treeName）
- **主子表支持**: 支持主表与子表的关联生成

---

## 2. 生成流程

```
数据库表 ──(导入)──▶ gen_table / gen_table_column
       │
       ▼
  编辑配置（基本信息 + 字段信息 + 生成信息）
       │
       ▼
  Velocity 模板渲染
       │
       ├── domain.java.vm        → Java 实体类
       ├── mapper.java.vm        → Mapper 接口
       ├── service.java.vm       → Service 接口
       ├── serviceImpl.java.vm   → Service 实现
       ├── controller.java.vm    → Controller 控制器
       ├── mapper.xml.vm         → MyBatis XML 映射
       ├── index.vue.vm          → 前端列表页面
       ├── index.ts.vm           → 前端 API 封装
       ├── types.ts.vm           → 前端类型定义
       ├── sql.vm                → 菜单 SQL 脚本
       └── ...
       │
       ▼
  预览 / 下载 / 生成到路径
```

---

## 3. 模板分类（tplCategory）

| 值 | 说明 | 生成内容 |
|----|------|----------|
| `crud` | 单表 CRUD | 标准增删改查代码 |
| `tree` | 树形表 | 带树形结构的增删改查代码 |
| `sub` | 主子表 | 主表 + 子表关联代码 |

---

## 4. 字段配置能力

### 4.1 Java 类型映射

| 数据库类型 | 默认 Java 类型 |
|-----------|---------------|
| BIGINT | Long |
| VARCHAR/CHAR/TEXT | String |
| INT | Integer |
| DOUBLE/FLOAT | Double |
| DECIMAL | BigDecimal |
| DATETIME/TIMESTAMP | Date |
| TINYINT(1) | Boolean |

### 4.2 HTML 组件类型

| 值 | 说明 |
|----|------|
| `input` | 文本框 |
| `textarea` | 文本域 |
| `select` | 下拉框 |
| `radio` | 单选框 |
| `checkbox` | 复选框 |
| `datetime` | 日期控件 |
| `imageUpload` | 图片上传 |
| `fileUpload` | 文件上传 |
| `editor` | 富文本控件 |

### 4.3 查询方式

| 值 | 说明 |
|----|------|
| `EQ` | 等于 |
| `NE` | 不等于 |
| `GT` | 大于 |
| `GE` | 大于等于 |
| `LT` | 小于 |
| `LE` | 小于等于 |
| `LIKE` | 模糊匹配 |
| `BETWEEN` | 范围查询 |

### 4.4 字段操作标志

| 标志 | 说明 |
|------|------|
| `isInsert` | 是否在新增表单中显示 |
| `isEdit` | 是否在编辑表单中显示 |
| `isList` | 是否在列表表格中显示 |
| `isQuery` | 是否作为查询条件 |
| `isRequired` | 是否必填 |

---

## 5. 生成配置（genInfo）

### 5.1 基本参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `packageName` | 生成包名 | `org.fellow99.magic.ruoyi` |
| `moduleName` | 模块名 | `system` |
| `businessName` | 业务名 | `user` |
| `functionName` | 功能描述 | `用户管理` |
| `functionAuthor` | 作者 | `fellow99` |
| `genType` | 生成方式 | `0`=下载ZIP, `1`=自定义路径 |
| `genPath` | 自定义生成路径 | `/opt/project/` |

### 5.2 树形表参数

| 参数 | 说明 |
|------|------|
| `treeCode` | 树编码字段 |
| `treeParentCode` | 树父编码字段 |
| `treeName` | 树名称字段 |

### 5.3 菜单参数

| 参数 | 说明 |
|------|------|
| `parentMenuId` | 上级菜单 ID |
| `menuIds` | 生成的菜单 ID 列表 |

---

## 6. 多数据源支持

模块支持从多个数据源读取表结构。通过 `dataName` 参数区分不同数据源。

- `getDataNames` 接口返回所有可用数据源名称列表
- 导入表时需选择目标数据源
- 列表页可按数据源筛选

---

## 7. 数据模型

代码生成模块依赖以下两张核心表:

| 表名 | 实体类 | 主键 | 描述 |
|------|--------|------|------|
| `gen_table` | GenTable | `table_id` BIGINT | 代码生成业务表 |
| `gen_table_column` | GenTableColumn | `column_id` BIGINT | 代码生成业务表字段 |

### 7.1 gen_table 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `table_id` | BIGINT | 表 ID |
| `data_name` | VARCHAR | 数据源名称 |
| `table_name` | VARCHAR | 数据库表名 |
| `table_comment` | VARCHAR | 表描述 |
| `class_name` | VARCHAR | Java 类名 |
| `tpl_category` | VARCHAR | 模板分类（crud/tree/sub） |
| `package_name` | VARCHAR | 生成包名 |
| `module_name` | VARCHAR | 模块名 |
| `business_name` | VARCHAR | 业务名 |
| `function_name` | VARCHAR | 功能描述 |
| `function_author` | VARCHAR | 作者 |
| `gen_type` | CHAR | 生成方式（0=下载, 1=路径） |
| `gen_path` | VARCHAR | 自定义生成路径 |
| `options` | VARCHAR | JSON 扩展配置 |
| `sub_table_name` | VARCHAR | 子表名（主子表场景） |
| `sub_table_fk_name` | VARCHAR | 子表外键名 |

### 7.2 gen_table_column 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `column_id` | BIGINT | 字段 ID |
| `table_id` | BIGINT | 所属表 ID |
| `column_name` | VARCHAR | 数据库列名 |
| `column_comment` | VARCHAR | 列描述 |
| `column_type` | VARCHAR | 数据库列类型 |
| `java_type` | VARCHAR | Java 类型 |
| `java_field` | VARCHAR | Java 属性名 |
| `is_pk` | CHAR | 是否主键（1=是） |
| `is_increment` | CHAR | 是否自增（1=是） |
| `is_required` | CHAR | 是否必填（1=是） |
| `is_insert` | CHAR | 是否插入字段（1=是） |
| `is_edit` | CHAR | 是否编辑字段（1=是） |
| `is_list` | CHAR | 是否列表字段（1=是） |
| `is_query` | CHAR | 是否查询字段（1=是） |
| `query_type` | VARCHAR | 查询方式（EQ/LIKE 等） |
| `html_type` | VARCHAR | HTML 组件类型 |
| `dict_type` | VARCHAR | 字典类型 |
| `sort` | INT | 排序号 |

---

## 8. 权限标识

| 权限标识 | 说明 |
|----------|------|
| `tool:gen:list` | 查询已导入表列表 |
| `tool:gen:import` | 导入表 |
| `tool:gen:edit` | 编辑生成配置 |
| `tool:gen:remove` | 删除生成配置 |
| `tool:gen:preview` | 预览生成代码 |
| `tool:gen:code` | 生成代码 |

---

## 9. 错误场景

| 场景 | 说明 |
|------|------|
| 未选择导入表 | 导入表时未勾选任何表 |
| 表已存在 | 重复导入同一张表 |
| 字段校验失败 | 编辑时必填字段为空 |
| 生成路径无效 | genType=1 时路径不存在或无权限 |
| 数据源不可用 | 指定的数据源连接失败 |

---

## 10. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
