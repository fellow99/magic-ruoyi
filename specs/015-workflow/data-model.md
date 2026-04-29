# 015-Workflow 工作流模块 - 数据模型

> 本文档定义工作流模块的数据库表结构、实体对象和前端类型。
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 数据库表

工作流模块基于 Warm-Flow 引擎，核心表由引擎管理。业务层主要使用以下表：

### 1.1 flow_category - 流程分类表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| category_id | bigint(20) | NOT NULL | - | 分类 ID（主键） |
| category_name | varchar(64) | YES | '' | 分类名称 |
| parent_id | bigint(20) | YES | 0 | 父级 ID |
| order_num | int(11) | YES | 0 | 显示顺序 |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `category_id`

---

### 1.2 flow_spel - SpEL 表达式表

| 列名 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | bigint(20) | NOT NULL | - | 主键 ID |
| component_name | varchar(100) | YES | '' | 组件名称 |
| method_name | varchar(100) | YES | '' | 方法名 |
| method_params | varchar(500) | YES | '' | 方法参数 |
| view_spel | varchar(500) | YES | '' | 预览 SpEL 值 |
| status | char(1) | YES | '0' | 状态（0=正常, 1=停用） |
| create_dept | bigint(20) | YES | NULL | 创建部门 |
| create_by | bigint(20) | YES | NULL | 创建者 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_by | bigint(20) | YES | NULL | 更新者 |
| update_time | datetime | YES | NULL | 更新时间 |
| remark | varchar(500) | YES | NULL | 备注 |

**主键**: `id`

---

### 1.3 Warm-Flow 引擎核心表（由引擎管理）

| 表名 | 说明 |
|------|------|
| flow_definition | 流程定义表 |
| flow_instance | 流程实例表 |
| flow_task | 流程任务表 |
| flow_node | 流程节点表 |
| flow_skip | 流程跳转表 |
| flow_his_task | 历史任务表 |
| flow_his_instance | 历史实例表 |
| flow_copy | 抄送表 |

---

## 2. 实体关系图

```
+------------------+       +------------------+
|  flow_category   |       |    flow_spel     |
+------------------+       +------------------+
| PK category_id   |       | PK id            |
|    category_name |       |    component_name|
|    parent_id     |       |    method_name   |
|    order_num     |       |    method_params |
+------------------+       |    view_spel     |
                           |    status        |
                           +------------------+

Warm-Flow 引擎表（内部关联）:
flow_definition 1-N flow_instance 1-N flow_task
```

---

## 3. 后端对象

### 3.1 CategoryVO - 流程分类视图对象

```java
@Data
public class CategoryVO {
    private Long categoryId;
    private String categoryName;
    private Long parentId;
    private Integer orderNum;
    private Date createTime;
    private List<CategoryVO> children;
}
```

### 3.2 SpelVO - SpEL 表达式视图对象

```java
@Data
public class SpelVO {
    private Long id;
    private String componentName;
    private String methodName;
    private String methodParams;
    private String viewSpel;
    private String status;
    private String remark;
}
```

### 3.3 FlowDefinitionVo - 流程定义视图对象

```java
@Data
public class FlowDefinitionVo {
    private String id;
    private String flowName;
    private String flowCode;
    private String formPath;
    private String version;
    private Integer isPublish;
    private Integer activityStatus;
    private Date createTime;
    private Date updateTime;
}
```

### 3.4 FlowInstanceVO - 流程实例视图对象

```java
@Data
public class FlowInstanceVO {
    private Long id;
    private String definitionId;
    private String flowName;
    private String flowCode;
    private String version;
    private String businessId;
    private Integer activityStatus;
    private String tenantId;
    private Date createTime;
    private String createBy;
    private String flowStatus;
    private String flowStatusName;
    private List<FlowTaskVO> flowTaskList;
    private String businessCode;
    private String businessTitle;
}
```

### 3.5 FlowTaskVO - 流程任务视图对象

```java
@Data
public class FlowTaskVO {
    private Long id;
    private Date createTime;
    private Date updateTime;
    private String tenantId;
    private String definitionId;
    private String instanceId;
    private String flowName;
    private String businessId;
    private String nodeCode;
    private String nodeName;
    private String flowCode;
    private String flowStatus;
    private String formCustom;
    private String formPath;
    private Integer nodeType;
    private Object nodeRatio;
    private String version;
    private Boolean applyNode;
    private List<ButtonList> buttonList;
    private List<FlowCopyVo> copyList;
    private Map<String, String> varList;
    private String businessCode;
    private String businessTitle;
}
```

---

## 4. 前端类型

### 4.1 流程分类

```typescript
export interface CategoryVO {
  categoryId: string | number;
  categoryName: string;
  parentId: string | number;
  orderNum: number;
  createTime: string;
  children: CategoryVO[];
}

export interface CategoryForm extends BaseEntity {
  categoryId?: string | number;
  categoryName?: string;
  parentId?: string | number;
  orderNum?: number;
}

export interface CategoryQuery {
  categoryName?: string;
}

export interface CategoryTreeVO {
  id: number | string;
  label: string;
  parentId: number | string;
  weight: number;
  children: CategoryTreeVO[];
}
```

### 4.2 流程定义

```typescript
export interface FlowDefinitionQuery extends PageQuery {
  flowCode?: string;
  flowName?: string;
  category: string | number;
  isPublish?: number;
}

export interface FlowDefinitionVo {
  id: string;
  flowName: string;
  flowCode: string;
  formPath: string;
  version: string;
  isPublish: number;
  activityStatus: number;
  createTime: Date;
  updateTime: Date;
}

export interface FlowDefinitionForm {
  id: string;
  flowName: string;
  flowCode: string;
  category: string;
  ext: string;
  formPath: string;
  formCustom: string;
  modelValue: string;
}
```

### 4.3 流程实例

```typescript
export interface FlowInstanceQuery extends PageQuery {
  category?: string | number;
  nodeName?: string;
  flowCode?: string;
  flowName?: string;
  createByIds?: string[] | number[];
  businessId?: string;
}

export interface FlowInstanceVO extends BaseEntity {
  id: string | number;
  definitionId: string;
  flowName: string;
  flowCode: string;
  version: string;
  businessId: string;
  activityStatus: number;
  tenantId: string;
  createTime: string;
  createBy: string;
  flowStatus: string;
  flowStatusName: string;
  flowTaskList: FlowTaskVO[];
  businessCode: string;
  businessTitle: string;
}
```

### 4.4 任务

```typescript
export interface TaskQuery extends PageQuery {
  nodeName?: string;
  flowCode?: string;
  flowName?: string;
  createByIds?: string[] | number[];
}

export interface FlowTaskVO {
  id: string | number;
  createTime?: Date;
  updateTime?: Date;
  tenantId?: string;
  definitionId?: string;
  instanceId: string;
  flowName: string;
  businessId: string;
  nodeCode: string;
  nodeName: string;
  flowCode: string;
  flowStatus: string;
  formCustom: string;
  formPath: string;
  nodeType: number;
  nodeRatio: string | number;
  version?: string;
  applyNode?: boolean;
  buttonList?: ButtonList[];
  copyList?: FlowCopyVo[];
  varList?: Map<string, string>;
  businessCode: string;
  businessTitle: string;
}

export interface TaskOperationBo {
  userId?: string;
  userIds?: string[];
  taskId: string | number;
  message?: string;
}
```

### 4.5 SpEL

```typescript
export interface SpelVO {
  id: string | number;
  componentName: string;
  methodName: string;
  methodParams: string;
  viewSpel: string;
  status: string;
  remark?: string;
}

export interface SpelForm extends BaseEntity {
  id?: string | number;
  componentName?: string;
  methodName?: string;
  methodParams?: string;
  viewSpel?: string;
  status?: string;
  remark?: string;
}

export interface SpelQuery extends PageQuery {
  componentName?: string;
  methodName?: string;
  methodParams?: string;
  viewSpel?: string;
  status?: string;
  params?: any;
}
```

---

## 5. 字段枚举值

### 5.1 流程发布状态 (isPublish)

| 值 | 含义 |
|------|------|
| 0 | 未发布 |
| 1 | 已发布 |

### 5.2 流程激活状态 (activityStatus)

| 值 | 含义 |
|------|------|
| 0 | 挂起 |
| 1 | 激活 |

### 5.3 SpEL 状态 (status)

| 值 | 含义 |
|------|------|
| '0' | 正常 |
| '1' | 停用 |

### 5.4 任务操作类型

| 值 | 说明 |
|------|------|
| delegateTask | 委派 |
| transferTask | 转办 |
| addSignature | 加签 |
| reductionSignature | 减签 |

---

## 6. 数据流转

### 6.1 启动流程

```
前端 { flowCode, businessId, ... }
  → POST /workflow/task/startWorkFlow
    → Warm-Flow 创建流程实例
    → 创建首个任务
  → 返回流程实例信息
```

### 6.2 审批任务

```
前端 { taskId, message, ... }
  → POST /workflow/task/completeTask
    → Warm-Flow 完成当前任务
    → 流转到下一节点
    → 创建新任务或结束流程
  → 返回审批结果
```

---

## 7. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
