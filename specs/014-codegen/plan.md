# 代码生成模块技术实现方案（014-codegen/plan.md）

> magic-ruoyi 代码生成模块技术实现方案。描述数据库表代码自动生成、CRUD 模板配置与多数据源支持的技术实现细节。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. Technical Context

### 1.1 模块定位

代码生成模块负责将数据库表结构自动转换为完整的 CRUD 代码，包括后端 Java 实体类、Mapper、Service、Controller，以及前端 Vue 页面、API 封装和路由配置。模块完全复用 RuoYi-Vue-Plus 上游实现，magic-ruoyi 项目零自定义代码。

### 1.2 上游依赖

| 上游模块 | 包前缀 | 说明 |
|----------|--------|------|
| `ruoyi-generator` | `org.dromara.generator` | 代码生成核心实现 |
| `ruoyi-common-web` | `org.dromara.common.web` | 全局异常处理、响应封装 |
| `ruoyi-common-satoken` | `org.dromara.common.satoken` | Sa-Token 权限校验 |
| `ruoyi-common-mybatis` | `org.dromara.common.mybatis` | MyBatis-Plus 集成 |

### 1.3 技术栈

| 组件 | 技术 | 说明 |
|------|------|------|
| 模板引擎 | Velocity | `.vm` 模板文件渲染 |
| ORM 框架 | MyBatis-Plus | 生成的 Mapper 继承 `BaseMapperPlus` |
| 前端框架 | Vue 3 + TypeScript + Element Plus | 生成的前端代码 |
| 数据源 | 多数据源支持 | 通过 `dataName` 参数区分 |

### 1.4 核心约束

- 本模块完全复用 RuoYi-Vue-Plus 上游实现，不编写自定义 Controller/Service/Mapper
- Velocity 模板文件位于 `ruoyi-generator` 模块的 `resources/vm/` 目录
- 生成的代码使用上游包名 `org.dromara`，如需自定义包名需在生成配置中修改
- 代码生成模块自身的两张配置表（`gen_table`、`gen_table_column`）不受多租户过滤

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 直接复用上游代码生成器，Velocity 模板开箱即用 |
| 约定优于配置 | 合规 | 遵循 RuoYi-Vue-Plus 的模板命名、目录结构、代码风格 |
| 实用优于完美 | 合规 | 先使用默认模板生成，再按需修改模板 |
| 安全优于便利 | 合规 | 代码生成接口需权限校验，生成路径需合法校验 |
| 零样板代码 | 合规 | 代码生成器的核心价值就是消除样板代码 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `ruoyi-generator` |
| 清晰模块边界 | 合规 | 代码生成功能归属 `org.dromara.generator` 包 |
| 多租户优先 | 合规 | `gen_table` 和 `gen_table_column` 在租户排除列表中 |

---

## 3. Research Findings

### 3.1 代码生成流程

```
数据库表 ──(导入)──▶ gen_table / gen_table_column
       │
       ▼
  编辑配置（基本信息 + 字段信息 + 生成信息）
       │
       ▼
  Velocity 模板渲染
       │
       ├── java/domain/       → Java 实体类（Bo/Vo）
       ├── java/mapper/       → Mapper 接口
       ├── java/service/      → Service 接口 + 实现
       ├── java/controller/   → Controller 控制器
       ├── java/domain/       → MapStruct 转换器
       ├── resources/mapper/  → MyBatis XML 映射
       ├── vue/api/           → 前端 API 封装（.ts）
       ├── vue/views/         → 前端页面（.vue）
       ├── vue/types/         → 前端类型定义（.ts）
       ├── sql/               → 菜单 SQL 脚本
       └── ...
       │
       ▼
  预览 / 下载 ZIP / 生成到服务器路径
```

### 3.2 Velocity 模板机制

Velocity 模板使用 `.vm` 后缀，位于 `ruoyi-generator` 模块的 `resources/vm/` 目录。

**模板变量来源**:

| 变量 | 来源 | 说明 |
|------|------|------|
| `$tableName` | `gen_table.table_name` | 数据库表名 |
| `$tableComment` | `gen_table.table_comment` | 表描述 |
| `$className` | `gen_table.class_name` | Java 类名 |
| `$packageName` | `gen_table.package_name` | 生成包名 |
| `$moduleName` | `gen_table.module_name` | 模块名 |
| `$businessName` | `gen_table.business_name` | 业务名 |
| `$functionName` | `gen_table.function_name` | 功能描述 |
| `$functionAuthor` | `gen_table.function_author` | 作者 |
| `$columns` | `gen_table_column` 列表 | 字段信息 |
| `$tplCategory` | `gen_table.tpl_category` | 模板类型（crud/tree/sub） |

**模板文件清单**:

| 模板文件 | 生成目标 | 说明 |
|----------|----------|------|
| `java/domain/bo.java.vm` | `*Bo.java` | 业务对象（Business Object） |
| `java/domain/vo.java.vm` | `*Vo.java` | 视图对象（View Object） |
| `java/domain/entity.java.vm` | `*.java` | 数据库实体类 |
| `java/mapper.java.vm` | `*Mapper.java` | Mapper 接口 |
| `java/service.java.vm` | `I*Service.java` | Service 接口 |
| `java/serviceImpl.java.vm` | `*ServiceImpl.java` | Service 实现 |
| `java/controller.java.vm` | `*Controller.java` | Controller 控制器 |
| `java/converter.java.vm` | `*Converter.java` | MapStruct 转换器 |
| `java/domain.java.vm` | 兼容旧版实体 | 旧版实体类模板 |
| `xml/mapper.xml.vm` | `*Mapper.xml` | MyBatis XML 映射 |
| `vue/api/api.ts.vm` | `index.ts` | 前端 API 封装 |
| `vue/views/index.vue.vm` | `index.vue` | 前端列表页面 |
| `vue/views/index-tree.vue.vm` | `index-tree.vue` | 树形列表页面 |
| `vue/types/types.ts.vm` | `types.ts` | 前端类型定义 |
| `sql/sql.vm` | `*.sql` | 菜单 SQL 脚本 |

### 3.3 多数据源支持

代码生成模块支持从多个数据源读取表结构。

**实现机制**:
- `GenTableServiceImpl` 通过 `DynamicDataSourceContextHolder` 切换数据源
- `getDataNames()` 返回所有配置的数据源名称
- 导入表时通过 `dataName` 参数指定目标数据源
- 查询数据库表列表时，根据数据源连接对应的数据库

### 3.4 树形表生成

当 `tplCategory = tree` 时，生成的代码包含树形结构支持。

**关键参数**:
- `treeCode`: 树编码字段（如 `menu_id`）
- `treeParentCode`: 树父编码字段（如 `parent_id`）
- `treeName`: 树名称字段（如 `menu_name`）

**生成差异**:
- 前端页面使用 `index-tree.vue.vm` 模板（带树形展开）
- Service 层包含 `buildTree()` 方法
- 查询返回树形结构而非扁平列表

### 3.5 主子表生成

当 `tplCategory = sub` 时，生成的代码包含主子表关联。

**关键参数**:
- `subTableName`: 子表名
- `subTableFkName`: 子表外键字段名

**生成差异**:
- 前端页面包含子表编辑区域
- Service 层包含子表的级联保存逻辑
- 实体类包含子表集合属性

---

## 4. Data Model

### 4.1 gen_table（代码生成业务表）

| 字段 | 类型 | 说明 |
|------|------|------|
| table_id | BIGINT | 表 ID（主键） |
| data_name | VARCHAR(200) | 数据源名称 |
| table_name | VARCHAR(200) | 数据库表名 |
| table_comment | VARCHAR(500) | 表描述 |
| sub_table_name | VARCHAR(64) | 子表名（主子表场景） |
| sub_table_fk_name | VARCHAR(64) | 子表外键名 |
| class_name | VARCHAR(100) | Java 类名 |
| tpl_category | VARCHAR(200) | 模板分类（crud/tree/sub） |
| package_name | VARCHAR(100) | 生成包名 |
| module_name | VARCHAR(30) | 模块名 |
| business_name | VARCHAR(30) | 业务名 |
| function_name | VARCHAR(50) | 功能描述 |
| function_author | VARCHAR(50) | 作者 |
| gen_type | CHAR(1) | 生成方式（0=下载ZIP, 1=自定义路径） |
| gen_path | VARCHAR(200) | 自定义生成路径 |
| options | VARCHAR(2000) | JSON 扩展配置（树形参数等） |
| create_dept | BIGINT | 创建部门 |
| create_by | BIGINT | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | BIGINT | 更新者 |
| update_time | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

### 4.2 gen_table_column（代码生成业务表字段）

| 字段 | 类型 | 说明 |
|------|------|------|
| column_id | BIGINT | 字段 ID（主键） |
| table_id | BIGINT | 所属表 ID |
| column_name | VARCHAR(200) | 数据库列名 |
| column_comment | VARCHAR(500) | 列描述 |
| column_type | VARCHAR(100) | 数据库列类型 |
| java_type | VARCHAR(500) | Java 类型 |
| java_field | VARCHAR(200) | Java 属性名 |
| is_pk | CHAR(1) | 是否主键（1=是） |
| is_increment | CHAR(1) | 是否自增（1=是） |
| is_required | CHAR(1) | 是否必填（1=是） |
| is_insert | CHAR(1) | 是否插入字段（1=是） |
| is_edit | CHAR(1) | 是否编辑字段（1=是） |
| is_list | CHAR(1) | 是否列表字段（1=是） |
| is_query | CHAR(1) | 是否查询字段（1=是） |
| query_type | VARCHAR(200) | 查询方式（EQ/NE/GT/GE/LT/LE/LIKE/BETWEEN） |
| html_type | VARCHAR(200) | HTML 组件类型 |
| dict_type | VARCHAR(200) | 字典类型 |
| sort | INT | 排序号 |
| create_dept | BIGINT | 创建部门 |
| create_by | BIGINT | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | BIGINT | 更新者 |
| update_time | DATETIME | 更新时间 |

---

## 5. Interface Contracts

### 5.1 代码生成 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/tool/gen/list` | `tool:gen:list` | 查询已导入表列表 |
| GET | `/tool/gen/db/list` | `tool:gen:list` | 查询数据库表列表（未导入） |
| GET | `/tool/gen/{tableId}` | `tool:gen:edit` | 查询表详细信息 |
| PUT | `/tool/gen` | `tool:gen:edit` | 修改生成配置 |
| DELETE | `/tool/gen/{tableIds}` | `tool:gen:remove` | 删除生成配置 |
| POST | `/tool/gen/importTable` | `tool:gen:import` | 导入表 |
| GET | `/tool/gen/preview/{tableId}` | `tool:gen:preview` | 预览生成代码 |
| GET | `/tool/gen/download/{tableId}` | `tool:gen:code` | 下载生成代码（ZIP） |
| GET | `/tool/gen/genCode/{tableId}` | `tool:gen:code` | 生成代码到自定义路径 |
| GET | `/tool/gen/synchDb/{tableId}` | `tool:gen:edit` | 同步数据库表结构 |
| GET | `/tool/gen/getDataNames` | `tool:gen:list` | 获取数据源名称列表 |

---

## 6. Implementation Strategy

### 6.1 后端实现

本模块完全复用 RuoYi-Vue-Plus 上游实现。

**入口 Controller（上游）**:

| Controller | 包路径 | 职责 |
|------------|--------|------|
| `GenController` | `org.dromara.generator.controller` | 代码生成 Controller |

**关键 Service（上游）**:

| Service | 包路径 | 职责 |
|---------|--------|------|
| `IGenTableService` | `org.dromara.generator.service` | 代码生成业务表 Service |
| `IGenTableColumnService` | `org.dromara.generator.service` | 代码生成字段 Service |
| `GenTableServiceImpl` | `org.dromara.generator.service.impl` | 核心实现（导入、编辑、预览、生成） |

**关键 Mapper（上游）**:

| Mapper | 包路径 | 职责 |
|--------|--------|------|
| `GenTableMapper` | `org.dromara.generator.mapper` | 代码生成业务表 Mapper |
| `GenTableColumnMapper` | `org.dromara.generator.mapper` | 代码生成字段 Mapper |

### 6.2 前端实现

前端页面位于 `magic-ruoyi-web/src/views/tool/gen/` 目录。

**页面清单**:

| 页面 | 路径 | 组件 |
|------|------|------|
| 代码生成列表 | `views/tool/gen/index.vue` | 列表 + 导入 + 编辑 + 预览 + 下载 |
| 导入表对话框 | `views/tool/gen/importTable.vue` | 数据库表选择 + 导入 |
| 编辑表对话框 | `views/tool/gen/editTable.vue` | 基本信息 + 字段信息 + 生成信息 |
| 基本信息表单 | `views/tool/gen/basicInfoForm.vue` | 表基本信息编辑 |
| 生成信息表单 | `views/tool/gen/genInfoForm.vue` | 生成配置编辑 |

**API 封装**:

| API 模块 | 路径 | 说明 |
|----------|------|------|
| 代码生成 | `src/api/tool/gen/index.ts` | listTable, listDbTable, getGenTable, updateGenTable, importTable, previewTable, delTable, genCode, synchDb, getDataNames |
| 类型定义 | `src/api/tool/gen/types.ts` | TableQuery, TableVO, GenTableVO, DbTableQuery, DbTableVO, DbTableForm |

### 6.3 权限配置

| 权限标识 | 功能 | 页面 |
|----------|------|------|
| `tool:gen:list` | 查询已导入表列表 | 代码生成 |
| `tool:gen:import` | 导入表 | 代码生成 |
| `tool:gen:edit` | 编辑生成配置 | 代码生成 |
| `tool:gen:remove` | 删除生成配置 | 代码生成 |
| `tool:gen:preview` | 预览生成代码 | 代码生成 |
| `tool:gen:code` | 生成代码 | 代码生成 |

### 6.4 配置项

```yaml
# application.yml 相关配置
mybatis-plus:
  # 代码生成模块的表不受租户过滤
  tenant:
    excludes:
      - gen_table
      - gen_table_column
```

---

## 7. Testing Considerations

### 7.1 后端测试

| 测试场景 | 测试方法 |
|----------|----------|
| 导入表 | 验证 `gen_table` 和 `gen_table_column` 正确插入 |
| 编辑配置 | 验证配置更新后，预览代码反映变更 |
| 预览代码 | 验证 Velocity 模板渲染输出正确 |
| 下载 ZIP | 验证 ZIP 文件包含所有预期文件 |
| 生成到路径 | 验证文件写入指定目录 |
| 同步数据库 | 验证表结构变更后配置同步更新 |
| 多数据源 | 验证切换数据源后读取正确的表列表 |

### 7.2 前端测试

| 测试场景 | 测试方法 |
|----------|----------|
| 导入表对话框 | 验证数据库表列表加载和勾选导入 |
| 编辑表表单 | 验证字段配置（查询方式、HTML 类型、字典绑定） |
| 代码预览 | 验证多 Tab 展示生成的代码文件 |
| 下载代码 | 验证 ZIP 下载触发 |
| 树形表配置 | 验证树形参数（treeCode/treeParentCode/treeName）设置 |

### 7.3 集成测试

| 测试场景 | 验证点 |
|----------|--------|
| 完整生成流程 | 导入表.编辑配置.预览.下载，验证生成代码可编译 |
| 生成的代码集成 | 将生成的代码放入项目，验证可正常启动和 CRUD |
| 树形表生成 | 验证生成的树形页面可正常展开/折叠 |
| 主子表生成 | 验证主子表级联保存逻辑正确 |

---

## 8. File Inventory

### 8.1 后端文件（上游 RuoYi-Vue-Plus）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| `GenController.java` | `org.dromara.generator.controller` | 代码生成 Controller |
| `IGenTableService.java` | `org.dromara.generator.service` | 代码生成 Service 接口 |
| `IGenTableColumnService.java` | `org.dromara.generator.service` | 字段 Service 接口 |
| `GenTableServiceImpl.java` | `org.dromara.generator.service.impl` | 代码生成 Service 实现 |
| `GenTableColumnServiceImpl.java` | `org.dromara.generator.service.impl` | 字段 Service 实现 |
| `GenTableMapper.java` | `org.dromara.generator.mapper` | 代码生成表 Mapper |
| `GenTableColumnMapper.java` | `org.dromara.generator.mapper` | 字段 Mapper |
| `GenTable.java` | `org.dromara.generator.domain` | 代码生成表实体 |
| `GenTableColumn.java` | `org.dromara.generator.domain` | 字段实体 |
| `GenTableBo.java` | `org.dromara.generator.domain.bo` | 代码生成表 Bo |
| `GenTableVo.java` | `org.dromara.generator.domain.vo` | 代码生成表 Vo |
| `GenTableColumnBo.java` | `org.dromara.generator.domain.bo` | 字段 Bo |
| `GenTableColumnVo.java` | `org.dromara.generator.domain.vo` | 字段 Vo |
| `GenTableConverter.java` | `org.dromara.generator.domain` | MapStruct 转换器 |
| `GenTableColumnConverter.java` | `org.dromara.generator.domain` | 字段转换器 |
| `vm/java/domain/bo.java.vm` | `resources/vm/` | Bo 模板 |
| `vm/java/domain/vo.java.vm` | `resources/vm/` | Vo 模板 |
| `vm/java/domain/entity.java.vm` | `resources/vm/` | 实体模板 |
| `vm/java/mapper.java.vm` | `resources/vm/` | Mapper 模板 |
| `vm/java/service.java.vm` | `resources/vm/` | Service 接口模板 |
| `vm/java/serviceImpl.java.vm` | `resources/vm/` | Service 实现模板 |
| `vm/java/controller.java.vm` | `resources/vm/` | Controller 模板 |
| `vm/java/converter.java.vm` | `resources/vm/` | 转换器模板 |
| `vm/xml/mapper.xml.vm` | `resources/vm/` | MyBatis XML 模板 |
| `vm/vue/api/api.ts.vm` | `resources/vm/` | 前端 API 模板 |
| `vm/vue/views/index.vue.vm` | `resources/vm/` | 前端列表模板 |
| `vm/vue/views/index-tree.vue.vm` | `resources/vm/` | 树形列表模板 |
| `vm/vue/types/types.ts.vm` | `resources/vm/` | 前端类型模板 |
| `vm/sql/sql.vm` | `resources/vm/` | 菜单 SQL 模板 |

### 8.2 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| `index.vue` | `src/views/tool/gen/` | 代码生成列表页 |
| `importTable.vue` | `src/views/tool/gen/` | 导入表对话框 |
| `editTable.vue` | `src/views/tool/gen/` | 编辑表对话框 |
| `basicInfoForm.vue` | `src/views/tool/gen/` | 基本信息表单 |
| `genInfoForm.vue` | `src/views/tool/gen/` | 生成信息表单 |
| `index.ts` | `src/api/tool/gen/` | 代码生成 API |
| `types.ts` | `src/api/tool/gen/` | 代码生成类型定义 |

### 8.3 数据库表

| 表名 | 说明 | SQL 文件 |
|------|------|----------|
| `gen_table` | 代码生成业务表 | `sql/magic-ruoyi.sql` |
| `gen_table_column` | 代码生成业务表字段 | `sql/magic-ruoyi.sql` |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
