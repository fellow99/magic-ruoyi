# 字典管理模块技术实现方案 (plan.md)

> magic-ruoyi 字典管理模块。负责字典类型和字典数据的增删改查、缓存刷新功能。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. Technical Context

### 1.1 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot 3.x + MyBatis-Plus |
| 权限框架 | Sa-Token |
| 缓存 | Redis (字典缓存) |
| 前端框架 | Vue 3.5 + TypeScript + Element Plus |
| 状态管理 | Pinia (dictStore) |
| 数据校验 | Element Plus Form Validation |
| 权限控制 | v-hasPermi 指令 |
| Excel 导出 | EasyExcel |

### 1.2 现有代码分析

**前端已实现** (`magic-ruoyi-web/src/views/system/dict/`):
- 字典类型管理 (`index.vue`): 列表分页、新增/修改/删除、刷新缓存、导出
- 字典数据管理 (`data.vue`): 按字典类型管理数据、新增/修改/删除、导出
- 字典类型点击跳转到字典数据页 (`router-link`)
- 字典数据标签带样式展示 (`el-tag` + `cssClass`)
- 回显样式选择 (default/primary/success/info/warning/danger)

**前端 API 已定义**:
- 字典类型: `listType`, `getType`, `addType`, `updateType`, `delType`, `refreshCache`, `optionselect`
- 字典数据: `getDicts` (按类型查询), `listData`, `getData`, `addData`, `updateData`, `delData`

**字典缓存机制** (`magic-ruoyi-web/src/store/modules/dict.ts`):
- `useDictStore` 使用 Map 存储字典数据
- Key 为字典类型 (如 `sys_normal_disable`)
- Value 为 `DictDataOption[]` 数组
- 提供 `getDict`, `setDict`, `removeDict`, `cleanDict` 方法

**DictTag 组件** (`magic-ruoyi-web/src/components/DictTag/index.vue`):
- 接收 `options` (字典数据数组) 和 `value` (当前值)
- 自动匹配 value 与字典数据的 dictValue
- 根据 `elTagType` (listClass) 显示不同颜色的 Tag
- 支持 `elTagClass` (cssClass) 自定义样式
- 支持多值分隔显示 (默认逗号分隔)

### 1.3 待实现后端

后端 Controller/Service/Mapper 层尚未创建，需要从零实现。包含两个子模块:
- 字典类型管理 (`SysDictTypeController`)
- 字典数据管理 (`SysDictDataController`)

---

## 2. Constitution Compliance

### 2.1 架构约束

- 遵循 RuoYi-Vue-Plus 分层架构: Controller -> Service -> Mapper
- 使用 MyBatis-Plus 的 `BaseMapper<T>` 和 `IService<T>` / `ServiceImpl` 模式
- BO / VO 分离，通过 MapStruct 转换
- 多租户: 字典数据为全局共享 (部分字典可能按租户隔离)

### 2.2 安全约束

- 所有写操作需要 `@SaCheckPermission` 权限校验
- 删除字典类型时级联删除关联的字典数据
- 删除/修改字典数据后需清除 Redis 缓存

### 2.3 编码规范

- 权限标识格式: `system:dict:{operation}` (list/query/add/edit/remove/export)
- RESTful API 设计
- 字典类型和字典数据分别使用独立的 Controller

---

## 3. Research Findings

### 3.1 数据库表结构 (sys_dict_type)

| 字段 | 类型 | 说明 |
|------|------|------|
| dict_id | bigint(20) | 主键 |
| tenant_id | varchar(20) | 租户编号 |
| dict_name | varchar(100) | 字典名称 |
| dict_type | varchar(100) | 字典类型 (唯一标识) |
| status | char(1) | 状态 (0=正常, 1=停用) |
| create_dept | bigint(20) | 创建部门 |
| create_by | bigint(20) | 创建者 |
| create_time | datetime | 创建时间 |
| update_by | bigint(20) | 更新者 |
| update_time | datetime | 更新时间 |
| remark | varchar(500) | 备注 |

### 3.2 数据库表结构 (sys_dict_data)

| 字段 | 类型 | 说明 |
|------|------|------|
| dict_code | bigint(20) | 主键 |
| tenant_id | varchar(20) | 租户编号 |
| dict_sort | int(4) | 字典排序 |
| dict_label | varchar(100) | 字典标签 (显示文本) |
| dict_value | varchar(100) | 字典键值 (存储值) |
| dict_type | varchar(100) | 字典类型 (关联 sys_dict_type) |
| css_class | varchar(100) | 样式属性 (CSS 类名) |
| list_class | varchar(100) | 回显样式 (Tag 类型) |
| is_default | char(1) | 是否默认 (Y=是, N=否) |
| status | char(1) | 状态 (0=正常, 1=停用) |
| create_dept | bigint(20) | 创建部门 |
| create_by | bigint(20) | 创建者 |
| create_time | datetime | 创建时间 |
| update_by | bigint(20) | 更新者 |
| update_time | datetime | 更新时间 |
| remark | varchar(500) | 备注 |

### 3.3 缓存机制

- 字典数据缓存在 Redis 中
- Key 格式: `dict:{dictType}` (如 `dict:sys_normal_disable`)
- Value: 字典数据列表 (JSON 序列化)
- 刷新缓存: 清除所有字典缓存 (`DELETE /system/dict/type/refreshCache`)
- 前端 dictStore 同步管理本地字典缓存

### 3.4 DictTag 组件映射

DictTag 组件期望的数据格式 (`DictDataOption`):
```typescript
interface DictDataOption {
  label: string;    // dictLabel
  value: string;    // dictValue
  elTagType: string; // listClass (primary/success/warning/danger/info/default)
  elTagClass: string; // cssClass
}
```

后端返回字典数据时，字段名需与前端 `DictDataOption` 匹配，或前端进行字段映射。

---

## 4. Data Model

### 4.1 字典类型实体设计

**SysDictType (Entity)**
```
dictId: Long
tenantId: String
dictName: String
dictType: String
status: String
createDept: Long
createBy: Long
createTime: Date
updateBy: Long
updateTime: Date
remark: String
```

**SysDictTypeBo (Business Object)**
```
dictId: Long (修改时必填)
dictName: String (必填)
dictType: String (必填)
remark: String
```

**SysDictTypeVo (View Object)**
```
dictId: Long
dictName: String
dictType: String
remark: String
createTime: Date
```

### 4.2 字典数据实体设计

**SysDictData (Entity)**
```
dictCode: Long
tenantId: String
dictSort: Integer
dictLabel: String
dictValue: String
dictType: String
cssClass: String
listClass: String
isDefault: String
status: String
createDept: Long
createBy: Long
createTime: Date
updateBy: Long
updateTime: Date
remark: String
```

**SysDictDataBo (Business Object)**
```
dictCode: Long (修改时必填)
dictType: String (必填)
dictLabel: String (必填)
dictValue: String (必填)
cssClass: String
listClass: String
dictSort: Integer (必填)
remark: String
```

**SysDictDataVo (View Object)**
```
dictCode: Long
dictType: String
dictLabel: String
dictValue: String
cssClass: String
listClass: String
dictSort: Integer
remark: String
createTime: Date
```

---

## 5. Interface Contracts

### 5.1 字典类型 API 端点

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/dict/type/list` | system:dict:list | 查询字典类型列表 (分页) |
| GET | `/system/dict/type/{dictId}` | system:dict:query | 查询字典类型详情 |
| GET | `/system/dict/type/optionselect` | - | 字典类型下拉选择 |
| POST | `/system/dict/type` | system:dict:add | 新增字典类型 |
| PUT | `/system/dict/type` | system:dict:edit | 修改字典类型 |
| DELETE | `/system/dict/type/{dictIds}` | system:dict:remove | 删除字典类型 (支持批量) |
| DELETE | `/system/dict/type/refreshCache` | system:dict:remove | 刷新字典缓存 |
| POST | `/system/dict/type/export` | system:dict:export | 导出字典类型 Excel |

### 5.2 字典数据 API 端点

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/dict/data/list` | system:dict:list | 查询字典数据列表 (分页) |
| GET | `/system/dict/data/{dictCode}` | system:dict:query | 查询字典数据详情 |
| GET | `/system/dict/data/type/{dictType}` | - | 按字典类型查询字典数据 |
| POST | `/system/dict/data` | system:dict:add | 新增字典数据 |
| PUT | `/system/dict/data` | system:dict:edit | 修改字典数据 |
| DELETE | `/system/dict/data/{dictCodes}` | system:dict:remove | 删除字典数据 (支持批量) |
| POST | `/system/dict/data/export` | system:dict:export | 导出字典数据 Excel |

### 5.3 请求/响应示例

**GET /system/dict/type/list**
- Query: `pageNum`, `pageSize`, `dictName?`, `dictType?`, `createTimeBegin?`, `createTimeEnd?`
- Response: `R<TableDataInfo<SysDictTypeVo>>`

**GET /system/dict/data/type/{dictType}**
- Path: `dictType` (字典类型标识)
- Response: `R<List<SysDictDataVo>>` (按 dictSort 排序)

**DELETE /system/dict/type/refreshCache**
- Response: `R<Void>`

### 5.4 前端 API 契约

前端已定义完整 API 接口，后端需匹配:
- 字典类型: `@/api/system/dict/type/index.ts`
- 字典数据: `@/api/system/dict/data/index.ts`
- URL 路径、HTTP 方法、参数名称需与前端类型定义一致

---

## 6. Implementation Strategy

### 6.1 后端实现步骤

**Phase 1: 字典类型 CRUD**
1. 创建 `SysDictType` Entity
2. 创建 `SysDictTypeMapper` 继承 `BaseMapper<SysDictType>`
3. 创建 `ISysDictTypeService` 接口
4. 创建 `SysDictTypeServiceImpl` 实现业务逻辑
5. 创建 `SysDictTypeController` 暴露 REST API

**Phase 2: 字典数据 CRUD**
1. 创建 `SysDictData` Entity
2. 创建 `SysDictDataMapper` 继承 `BaseMapper<SysDictData>`
3. 创建 `ISysDictDataService` 接口
4. 创建 `SysDictDataServiceImpl` 实现业务逻辑
5. 创建 `SysDictDataController` 暴露 REST API

**Phase 3: Redis 缓存**
1. 创建字典缓存 Service (`SysDictCacheService`):
   - `loadDictCache(dictType)`: 加载单个字典类型到 Redis
   - `clearDictCache()`: 清除所有字典缓存
   - `removeDictCache(dictType)`: 移除指定字典缓存
2. 字典数据增删改后自动清除对应字典类型的缓存
3. 刷新缓存接口清除所有缓存并重新加载

**Phase 4: 级联删除**
1. 删除字典类型时:
   - 查询关联的字典数据 (dict_type = ?)
   - 批量删除字典数据
   - 清除 Redis 缓存
2. 使用 `@Transactional` 保证事务一致性

**Phase 5: 导出功能**
1. 字典类型导出: 按查询条件导出 Excel
2. 字典数据导出: 按查询条件导出 Excel
3. 使用 EasyExcel 实现

### 6.2 关键业务逻辑

**字典类型唯一性**:
- `dict_type` 在同一租户下唯一
- 新增/修改时校验唯一性

**字典数据排序**:
- 按 `dict_sort` 升序排列
- 新增时默认 `dict_sort = 0`

**缓存刷新策略**:
```
refreshCache():
  1. 清除 Redis 中所有 dict:* key
  2. 查询所有字典类型 (status='0')
  3. 对每个字典类型:
     a. 查询字典数据 (status='0', order by dict_sort)
     b. 序列化后存入 Redis (key: dict:{dictType})
```

**前端 dictStore 同步**:
- 前端通过 `useDict()` composable 加载字典
- `useDict()` 内部调用 `getDicts(dictType)` API
- API 返回数据存入 dictStore
- 刷新缓存后调用 `useDictStore().cleanDict()` 清空本地缓存

### 6.3 前端已有实现 (无需修改)

- 字典类型列表分页已实现
- 字典数据列表分页已实现
- 字典类型点击跳转已实现 (router-link)
- 字典数据标签样式展示已实现 (el-tag + cssClass)
- 回显样式选择已实现 (6种颜色选项)
- 刷新缓存功能已实现 (后端 + 前端 dictStore)
- DictTag 组件已实现 (自动映射字典值)
- 导出功能已实现

---

## 7. Testing Considerations

### 7.1 单元测试

- 字典类型唯一性校验测试
- 级联删除测试: 删除字典类型后关联数据正确清理
- 缓存加载测试: 验证字典数据正确存入 Redis
- 缓存清除测试: 验证增删改后缓存正确清除

### 7.2 集成测试

- 字典类型 CRUD 完整流程测试
- 字典数据 CRUD 完整流程测试
- 缓存刷新测试: 验证 refreshCache 后 Redis 数据正确
- 按类型查询字典数据测试: 验证返回数据按 dictSort 排序

### 7.3 前端测试

- 字典类型列表分页功能
- 字典数据列表分页功能
- 字典类型点击跳转功能
- 字典数据标签样式展示
- 回显样式选择功能
- 刷新缓存功能 (后端 + 前端 dictStore)
- DictTag 组件字典值映射

### 7.4 性能测试

- 字典类型列表查询响应时间 (目标: < 300ms P95)
- 按类型查询字典数据响应时间 (目标: < 200ms P95, 含缓存)
- Redis 缓存加载性能 (目标: 100个字典类型 < 2s)
- 缓存命中率测试 (目标: > 95%)

---

## 8. File Inventory

### 8.1 后端文件 (待创建)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-admin/.../domain/SysDictType.java` | 字典类型实体 |
| `magic-ruoyi-admin/.../domain/bo/SysDictTypeBo.java` | 字典类型业务对象 |
| `magic-ruoyi-admin/.../domain/vo/SysDictTypeVo.java` | 字典类型视图对象 |
| `magic-ruoyi-admin/.../domain/SysDictData.java` | 字典数据实体 |
| `magic-ruoyi-admin/.../domain/bo/SysDictDataBo.java` | 字典数据业务对象 |
| `magic-ruoyi-admin/.../domain/vo/SysDictDataVo.java` | 字典数据视图对象 |
| `magic-ruoyi-admin/.../mapper/SysDictTypeMapper.java` | 字典类型 Mapper |
| `magic-ruoyi-admin/.../mapper/SysDictDataMapper.java` | 字典数据 Mapper |
| `magic-ruoyi-admin/.../service/ISysDictTypeService.java` | 字典类型 Service 接口 |
| `magic-ruoyi-admin/.../service/ISysDictDataService.java` | 字典数据 Service 接口 |
| `magic-ruoyi-admin/.../service/impl/SysDictTypeServiceImpl.java` | 字典类型 Service 实现 |
| `magic-ruoyi-admin/.../service/impl/SysDictDataServiceImpl.java` | 字典数据 Service 实现 |
| `magic-ruoyi-admin/.../controller/system/SysDictTypeController.java` | 字典类型 Controller |
| `magic-ruoyi-admin/.../controller/system/SysDictDataController.java` | 字典数据 Controller |
| `magic-ruoyi-admin/.../resources/mapper/system/SysDictTypeMapper.xml` | MyBatis XML (如需自定义 SQL) |
| `magic-ruoyi-admin/.../resources/mapper/system/SysDictDataMapper.xml` | MyBatis XML (如需自定义 SQL) |

### 8.2 前端文件 (已存在)

| 文件路径 | 说明 |
|----------|------|
| `magic-ruoyi-web/src/views/system/dict/index.vue` | 字典类型管理页面 |
| `magic-ruoyi-web/src/views/system/dict/data.vue` | 字典数据管理页面 |
| `magic-ruoyi-web/src/api/system/dict/type/index.ts` | 字典类型 API 接口 |
| `magic-ruoyi-web/src/api/system/dict/type/types.ts` | 字典类型类型定义 |
| `magic-ruoyi-web/src/api/system/dict/data/index.ts` | 字典数据 API 接口 |
| `magic-ruoyi-web/src/api/system/dict/data/types.ts` | 字典数据类型定义 |
| `magic-ruoyi-web/src/store/modules/dict.ts` | 字典 Store |
| `magic-ruoyi-web/src/components/DictTag/index.vue` | 字典标签组件 |

### 8.3 数据库文件

| 文件路径 | 说明 |
|----------|------|
| `sql/magic-ruoyi.sql` | 包含 sys_dict_type 和 sys_dict_data 表定义和初始数据 |

### 8.4 关联表

| 表名 | 说明 |
|------|------|
| sys_dict_type | 字典类型表 |
| sys_dict_data | 字典数据表 (通过 dict_type 关联) |
