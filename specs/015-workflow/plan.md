# 工作流模块技术实现方案（015-workflow/plan.md）

> magic-ruoyi 工作流模块技术实现方案。基于 Warm-Flow 引擎实现流程定义、流程实例、任务审批等功能。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. Technical Context

### 1.1 模块定位

工作流模块基于 Warm-Flow 轻量级工作流引擎，提供完整的 BPM 能力，包括流程分类管理、流程定义设计与发布、流程实例运行、任务审批处理、SpEL 表达式管理等。模块完全复用 RuoYi-Vue-Plus 上游实现，magic-ruoyi 项目零自定义代码。

### 1.2 上游依赖

| 上游模块 | 包前缀 | 说明 |
|----------|--------|------|
| `ruoyi-workflow` | `org.dromara.workflow` | Warm-Flow 集成与业务封装 |
| `ruoyi-common-web` | `org.dromara.common.web` | 全局异常处理、响应封装 |
| `ruoyi-common-satoken` | `org.dromara.common.satoken` | Sa-Token 权限校验 |
| `ruoyi-common-mybatis` | `org.dromara.common.mybatis` | MyBatis-Plus 集成 |
| `ruoyi-common-tenant` | `org.dromara.common.tenant` | 多租户支持 |
| `warm-flow-core` | `org.dromara.warm` | Warm-Flow 工作流引擎核心 |

### 1.3 技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 工作流引擎 | Warm-Flow | 1.8.4 | 轻量级国产工作流引擎 |
| 表达式引擎 | Spring SpEL | - | 流程条件表达式 |
| 后端框架 | Spring Boot + MyBatis-Plus | - | 业务层实现 |
| 前端框架 | Vue 3.5 + Element Plus | - | 流程管理界面 |
| 设计器 | Warm-Flow UI | - | 流程模型可视化设计 |

### 1.4 核心约束

- 本模块完全复用 RuoYi-Vue-Plus 上游实现，不编写自定义 Controller/Service/Mapper
- Warm-Flow 引擎表（`flow_*` 前缀）不受多租户过滤，`flow_spel` 显式排除
- 流程定义通过 XML 格式存储，支持经典模式和仿钉钉模式两种设计器
- 流程实例与业务数据通过 `business_id` 关联

---

## 2. Constitution Compliance

| 宪法原则 | 合规状态 | 说明 |
|----------|----------|------|
| 简单优于复杂 | 合规 | 使用轻量级 Warm-Flow 引擎，不引入 Activiti/Camunda 等重型框架 |
| 约定优于配置 | 合规 | 遵循 Warm-Flow 的 XML 格式、节点类型、流转规则 |
| 实用优于完美 | 合规 | 工作流模块默认开启但可按需关闭（`warm-flow.enabled`） |
| 安全优于便利 | 合规 | 所有流程操作需权限校验，审批操作记录审计日志 |
| 零样板代码 | 合规 | Warm-Flow 提供完整 API，无需手写流程引擎代码 |
| 依赖整合，非复制 | 合规 | 通过 Maven 依赖引入 `ruoyi-workflow` 和 `warm-flow-core` |
| 清晰模块边界 | 合规 | 工作流功能归属 `org.dromara.workflow` 包 |
| 多租户优先 | 合规 | `flow_spel` 在租户排除列表中，流程实例通过业务数据间接实现租户隔离 |

---

## 3. Research Findings

### 3.1 Warm-Flow 引擎架构

Warm-Flow 是一个轻量级工作流引擎，核心概念包括:

| 概念 | 说明 |
|------|------|
| 流程定义（Definition） | 流程模型，包含节点、连线、条件等 |
| 流程实例（Instance） | 流程定义的一次运行实例 |
| 任务（Task） | 流程实例中的待办/已办任务 |
| 节点（Node） | 流程中的步骤（开始、审批、结束等） |
| 连线（Skip） | 节点之间的流转路径 |
| 变量（Variable） | 流程运行时的上下文数据 |

### 3.2 流程定义生命周期

```
新建 → 编辑（设计器） → 发布 → 激活 → 运行实例
                          ↓
                      取消发布 → 失效
                          ↓
                      挂起 → 不可发起新实例
```

**发布状态**:
- `0`: 未发布（可编辑、可删除）
- `1`: 已发布（可发起新实例）
- `2`: 失效（被新版本替代）

**激活状态**:
- `0`: 挂起（不可发起新实例，已有实例继续运行）
- `1`: 激活（正常状态）

### 3.3 流程设计器模式

| 模式 | 值 | 说明 |
|------|-----|------|
| 经典模式 | `CLASSICS` | 传统 BPMN 风格设计器，适合复杂流程 |
| 仿钉钉模式 | `MIMIC` | 类钉钉审批流设计器，适合简单审批场景 |

### 3.4 任务操作类型

| 操作 | API 方法 | 说明 |
|------|----------|------|
| 审批通过 | `completeTask` | 完成任务，流转到下一节点 |
| 驳回 | `backProcess` | 驳回到指定历史节点 |
| 转办 | `transferTask` | 将任务转交给其他人办理 |
| 委派 | `delegateTask` | 将任务委派给其他人，完成后回到原办理人 |
| 加签 | `addSignature` | 在当前节点增加审批人 |
| 减签 | `reductionSignature` | 移除已加签的审批人 |
| 终止 | `terminationTask` | 终止流程实例 |
| 催办 | `urgeTask` | 提醒当前任务办理人 |

### 3.5 SpEL 表达式

SpEL 表达式用于流程中的条件判断（如网关分支条件）。

**存储**: `flow_spel` 表，不受租户过滤。

**使用场景**:
- 网关分支条件
- 节点办理人动态计算
- 流程变量赋值

### 3.6 请假流程示例

项目包含完整的请假流程演示:
- 前端页面: `views/workflow/leave/index.vue`、`leaveEdit.vue`
- 后端 API: `WorkflowLeaveController`
- 数据模型: 请假申请业务表 + Warm-Flow 流程实例

---

## 4. Data Model

### 4.1 flow_category（流程分类表）

| 字段 | 类型 | 说明 |
|------|------|------|
| category_id | BIGINT | 分类 ID（主键） |
| category_name | VARCHAR(100) | 分类名称 |
| parent_id | BIGINT | 父级 ID |
| order_num | INT | 显示顺序 |
| create_dept | BIGINT | 创建部门 |
| create_by | BIGINT | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | BIGINT | 更新者 |
| update_time | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

### 4.2 flow_spel（SpEL 表达式表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| component_name | VARCHAR(100) | 组件名称 |
| method_name | VARCHAR(100) | 方法名 |
| params | VARCHAR(500) | 参数 |
| status | CHAR(1) | 状态（0=正常, 1=停用） |
| create_dept | BIGINT | 创建部门 |
| create_by | BIGINT | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | BIGINT | 更新者 |
| update_time | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

### 4.3 Warm-Flow 引擎表（上游管理）

| 表名 | 说明 |
|------|------|
| `flow_definition` | 流程定义表 |
| `flow_instance` | 流程实例表 |
| `flow_task` | 任务表 |
| `flow_node` | 节点表 |
| `flow_skip` | 连线表 |
| `flow_condition` | 条件表 |
| `flow_his_task` | 历史任务表 |
| `flow_variable` | 流程变量表 |

### 4.4 流程定义核心字段（Warm-Flow 引擎）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 流程定义 ID |
| flow_name | VARCHAR | 流程名称 |
| flow_code | VARCHAR | 流程编码（唯一标识） |
| category | String | 所属分类 ID |
| version | String | 版本号 |
| is_publish | INT | 发布状态（0=未发布, 1=已发布, 2=失效） |
| activity_status | INT | 激活状态（0=挂起, 1=激活） |
| form_custom | CHAR | 是否动态表单（Y/N） |
| form_path | VARCHAR | 表单组件路径 |
| model_value | VARCHAR | 设计器模式（CLASSICS/MIMIC） |
| ext | VARCHAR | 扩展配置（JSON），含 autoPass 等 |

### 4.5 任务核心字段（Warm-Flow 引擎）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 任务 ID |
| instance_id | String | 所属流程实例 ID |
| flow_name | String | 流程名称 |
| flow_code | String | 流程编码 |
| node_code | String | 节点编码 |
| node_name | String | 节点名称 |
| node_type | INT | 节点类型 |
| business_id | String | 业务 ID |
| business_code | String | 业务编码 |
| business_title | String | 业务标题 |
| form_custom | String | 是否动态表单 |
| form_path | String | 表单组件路径 |
| button_list | Array | 可用操作按钮列表 |
| copy_list | Array | 抄送人列表 |
| var_list | Map | 流程变量 |

---

## 5. Interface Contracts

### 5.1 流程分类 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/category/list` | `workflow:category:list` | 查询流程分类列表 |
| POST | `/workflow/category` | `workflow:category:add` | 新增流程分类 |
| PUT | `/workflow/category` | `workflow:category:edit` | 修改流程分类 |
| DELETE | `/workflow/category/{ids}` | `workflow:category:remove` | 删除流程分类 |

### 5.2 流程定义 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/definition/list` | `workflow:definition:list` | 查询流程定义列表 |
| GET | `/workflow/definition/unPublishList` | `workflow:definition:list` | 查询未发布流程定义 |
| GET | `/workflow/definition/{id}` | `workflow:definition:edit` | 查询流程定义详情 |
| POST | `/workflow/definition` | `workflow:definition:add` | 新增流程定义 |
| PUT | `/workflow/definition` | `workflow:definition:edit` | 修改流程定义 |
| DELETE | `/workflow/definition/{ids}` | `workflow:definition:remove` | 删除流程定义 |
| PUT | `/workflow/definition/publish/{id}` | `workflow:definition:publish` | 发布流程定义 |
| PUT | `/workflow/definition/unPublish/{id}` | `workflow:definition:publish` | 取消发布 |
| PUT | `/workflow/definition/active/{id}` | `workflow:definition:edit` | 激活/挂起 |
| POST | `/workflow/definition/importDef` | `workflow:definition:importDef` | 导入流程定义 |
| POST | `/workflow/definition/copy/{id}` | `workflow:definition:add` | 复制流程定义 |
| GET | `/workflow/definition/definitionXml/{id}` | `workflow:definition:list` | 获取流程 XML |
| GET | `/workflow/definition/xmlString/{id}` | `workflow:definition:list` | 获取流程 XML 字符串 |

### 5.3 流程实例 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/instance/list` | `workflow:instance:list` | 查询流程实例列表 |
| GET | `/workflow/instance/finishList` | `workflow:instance:list` | 查询已完成实例列表 |
| GET | `/workflow/instance/myDocument` | `workflow:instance:list` | 查询我的单据 |
| DELETE | `/workflow/instance/{ids}` | `workflow:instance:list` | 删除流程实例 |
| PUT | `/workflow/instance/cancel/{id}` | `workflow:instance:cancel` | 撤销流程 |
| PUT | `/workflow/instance/invalid/{id}` | `workflow:instance:cancel` | 作废流程 |

### 5.4 任务管理 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/task/pageByTaskWait` | `workflow:task:list` | 查询待办任务列表 |
| GET | `/workflow/task/pageByTaskFinish` | `workflow:task:list` | 查询已办任务列表 |
| GET | `/workflow/task/pageByTaskCopy` | `workflow:task:list` | 查询抄送列表 |
| GET | `/workflow/task/pageByAllTaskWait` | `workflow:task:list` | 查询全部待办 |
| GET | `/workflow/task/pageByAllTaskFinish` | `workflow:task:list` | 查询全部已办 |
| POST | `/workflow/task/startWorkFlow` | `workflow:task:list` | 启动流程 |
| POST | `/workflow/task/completeTask` | `workflow:task:complete` | 审批任务 |
| POST | `/workflow/task/backProcess` | `workflow:task:back` | 驳回任务 |
| POST | `/workflow/task/taskOperation/{operation}` | `workflow:task:complete` | 任务操作（转办/委派/加签/减签） |
| POST | `/workflow/task/terminationTask` | `workflow:task:complete` | 终止任务 |
| POST | `/workflow/task/urgeTask` | `workflow:task:complete` | 催办任务 |
| GET | `/workflow/task/getTask/{taskId}` | `workflow:task:list` | 查询任务详情 |
| GET | `/workflow/task/getBackTaskNode/{taskId}/{nodeCode}` | `workflow:task:list` | 获取可驳回节点 |
| GET | `/workflow/task/currentTaskAllUser/{taskId}` | `workflow:task:list` | 获取当前任务办理人 |
| POST | `/workflow/task/getNextNodeList` | `workflow:task:list` | 获取下一节点 |
| PUT | `/workflow/task/updateAssignee/{userId}` | `workflow:task:complete` | 修改任务办理人 |

### 5.5 SpEL 表达式 API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/spel/list` | `workflow:spel:list` | 查询 SpEL 列表 |
| POST | `/workflow/spel` | `workflow:spel:add` | 新增 SpEL |
| PUT | `/workflow/spel` | `workflow:spel:edit` | 修改 SpEL |
| DELETE | `/workflow/spel/{ids}` | `workflow:spel:remove` | 删除 SpEL |

---

## 6. Implementation Strategy

### 6.1 后端实现

本模块完全复用 RuoYi-Vue-Plus 上游实现。

**入口 Controller（上游）**:

| Controller | 包路径 | 职责 |
|------------|--------|------|
| `FlwCategoryController` | `org.dromara.workflow.controller` | 流程分类管理 |
| `FlwDefinitionController` | `org.dromara.workflow.controller` | 流程定义管理 |
| `FlwInstanceController` | `org.dromara.workflow.controller` | 流程实例管理 |
| `FlwTaskController` | `org.dromara.workflow.controller` | 任务管理 |
| `FlwSpelController` | `org.dromara.workflow.controller` | SpEL 表达式管理 |
| `WorkflowLeaveController` | `org.dromara.workflow.controller` | 请假流程示例 |

**关键 Service（上游）**:

| Service | 包路径 | 职责 |
|---------|--------|------|
| `FlwCategoryService` | `org.dromara.workflow.service` | 流程分类 Service |
| `FlwDefinitionService` | `org.dromara.workflow.service` | 流程定义 Service |
| `FlwInstanceService` | `org.dromara.workflow.service` | 流程实例 Service |
| `FlwTaskService` | `org.dromara.workflow.service` | 任务 Service |
| `FlwSpelService` | `org.dromara.workflow.service` | SpEL 表达式 Service |

### 6.2 前端实现

前端页面位于 `magic-ruoyi-web/src/views/workflow/` 目录。

**页面清单**:

| 页面 | 路径 | 组件 |
|------|------|------|
| 流程分类 | `views/workflow/category/index.vue` | 树形分类管理 |
| 流程定义 | `views/workflow/processDefinition/index.vue` | 列表 + 设计器 + 发布管理 |
| 流程设计器 | `views/workflow/processDefinition/design.vue` | Warm-Flow 可视化设计器 |
| 流程实例 | `views/workflow/processInstance/index.vue` | 运行中/已完成实例管理 |
| 待办任务 | `views/workflow/task/taskWaiting.vue` | 当前用户待办列表 |
| 已办任务 | `views/workflow/task/taskFinish.vue` | 当前用户已办列表 |
| 抄送列表 | `views/workflow/task/taskCopyList.vue` | 抄送我的流程 |
| 我的单据 | `views/workflow/task/myDocument.vue` | 我发起的单据 |
| 全部待办 | `views/workflow/task/allTaskWaiting.vue` | 租户维度全部待办 |
| SpEL 表达式 | `views/workflow/spel/index.vue` | SpEL 表达式管理 |
| 请假流程 | `views/workflow/leave/index.vue` | 请假列表 |
| 请假编辑 | `views/workflow/leave/leaveEdit.vue` | 请假申请表单 |

**API 封装**:

| API 模块 | 路径 | 说明 |
|----------|------|------|
| 流程分类 | `src/api/workflow/category/` | list, add, edit, remove |
| 流程定义 | `src/api/workflow/definition/` | listDefinition, publish, unPublish, importDef, xmlString, copy |
| 流程实例 | `src/api/workflow/instance/` | list, finishList, myDocument, cancel, invalid |
| 任务管理 | `src/api/workflow/task/` | pageByTaskWait, pageByTaskFinish, completeTask, backProcess, taskOperation |
| SpEL 表达式 | `src/api/workflow/spel/` | list, add, edit, remove |
| 请假流程 | `src/api/workflow/leave/` | 请假业务 API |
| 工作流通用 | `src/api/workflow/workflowCommon/` | 通用工具方法 |

### 6.3 权限配置

| 权限标识 | 功能 | 页面 |
|----------|------|------|
| `workflow:category:list` | 查看流程分类 | 流程分类 |
| `workflow:category:add` | 新增流程分类 | 流程分类 |
| `workflow:category:edit` | 修改流程分类 | 流程分类 |
| `workflow:category:remove` | 删除流程分类 | 流程分类 |
| `workflow:definition:list` | 查看流程定义 | 流程定义 |
| `workflow:definition:add` | 新增流程定义 | 流程定义 |
| `workflow:definition:edit` | 修改流程定义 | 流程定义 |
| `workflow:definition:remove` | 删除流程定义 | 流程定义 |
| `workflow:definition:publish` | 发布/取消发布 | 流程定义 |
| `workflow:definition:importDef` | 导入流程定义 | 流程定义 |
| `workflow:instance:list` | 查看流程实例 | 流程实例 |
| `workflow:instance:cancel` | 撤销流程 | 流程实例 |
| `workflow:task:list` | 查看任务列表 | 任务管理 |
| `workflow:task:complete` | 审批任务 | 任务管理 |
| `workflow:task:back` | 驳回任务 | 任务管理 |
| `workflow:spel:list` | 查看 SpEL 列表 | SpEL 表达式 |
| `workflow:spel:add` | 新增 SpEL | SpEL 表达式 |
| `workflow:spel:edit` | 修改 SpEL | SpEL 表达式 |
| `workflow:spel:remove` | 删除 SpEL | SpEL 表达式 |

### 6.4 配置项

```yaml
# application.yml 相关配置
warm-flow:
  enabled: true    # 是否开启工作流引擎
  ui: true         # 是否开启工作流设计器

# 租户排除配置
tenant:
  excludes:
    - flow_spel
```

---

## 7. Testing Considerations

### 7.1 后端测试

| 测试场景 | 测试方法 |
|----------|----------|
| 流程定义发布 | 验证发布后状态变为已发布，可发起新实例 |
| 流程实例启动 | 验证实例创建，首个任务生成 |
| 任务审批 | 验证任务完成，流转到下一节点 |
| 任务驳回 | 验证驳回到指定节点，生成新任务 |
| 流程撤销 | 验证撤销后实例状态变更 |
| SpEL 表达式执行 | 验证表达式计算结果正确 |

### 7.2 前端测试

| 测试场景 | 测试方法 |
|----------|----------|
| 流程设计器加载 | 验证设计器正确渲染流程模型 |
| 流程定义发布操作 | 验证点击发布后状态更新 |
| 待办任务列表 | 验证任务数据正确展示 |
| 审批弹窗 | 验证审批表单正确加载 |
| 流程图查看 | 验证流程图显示流转轨迹 |

### 7.3 集成测试

| 测试场景 | 验证点 |
|----------|--------|
| 完整审批流程 | 发起.审批.完成，验证流程正确流转 |
| 驳回重审 | 发起.驳回.重新提交.审批，验证流程正确 |
| 转办/委派 | 验证任务办理人正确变更 |
| 加签/减签 | 验证审批人动态增减 |
| 请假流程示例 | 验证示例流程可正常运行 |

---

## 8. File Inventory

### 8.1 后端文件（上游 RuoYi-Vue-Plus）

| 文件 | 包路径 | 说明 |
|------|--------|------|
| `FlwCategoryController.java` | `org.dromara.workflow.controller` | 流程分类 Controller |
| `FlwDefinitionController.java` | `org.dromara.workflow.controller` | 流程定义 Controller |
| `FlwInstanceController.java` | `org.dromara.workflow.controller` | 流程实例 Controller |
| `FlwTaskController.java` | `org.dromara.workflow.controller` | 任务 Controller |
| `FlwSpelController.java` | `org.dromara.workflow.controller` | SpEL Controller |
| `WorkflowLeaveController.java` | `org.dromara.workflow.controller` | 请假流程 Controller |
| `FlwCategoryService.java` | `org.dromara.workflow.service` | 流程分类 Service |
| `FlwDefinitionService.java` | `org.dromara.workflow.service` | 流程定义 Service |
| `FlwInstanceService.java` | `org.dromara.workflow.service` | 流程实例 Service |
| `FlwTaskService.java` | `org.dromara.workflow.service` | 任务 Service |
| `FlwSpelService.java` | `org.dromara.workflow.service` | SpEL Service |
| `FlwCategory.java` | `org.dromara.workflow.domain` | 流程分类实体 |
| `FlwSpel.java` | `org.dromara.workflow.domain` | SpEL 实体 |
| `FlwCategoryBo.java` | `org.dromara.workflow.domain.bo` | 流程分类 Bo |
| `FlwCategoryVo.java` | `org.dromara.workflow.domain.vo` | 流程分类 Vo |
| `FlwSpelBo.java` | `org.dromara.workflow.domain.bo` | SpEL Bo |
| `FlwSpelVo.java` | `org.dromara.workflow.domain.vo` | SpEL Vo |
| `FlwCategoryMapper.java` | `org.dromara.workflow.mapper` | 流程分类 Mapper |
| `FlwSpelMapper.java` | `org.dromara.workflow.mapper` | SpEL Mapper |

### 8.2 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| `index.vue` | `src/views/workflow/category/` | 流程分类页面 |
| `index.vue` | `src/views/workflow/processDefinition/` | 流程定义页面 |
| `design.vue` | `src/views/workflow/processDefinition/` | 流程设计器页面 |
| `index.vue` | `src/views/workflow/processInstance/` | 流程实例页面 |
| `taskWaiting.vue` | `src/views/workflow/task/` | 待办任务页面 |
| `taskFinish.vue` | `src/views/workflow/task/` | 已办任务页面 |
| `taskCopyList.vue` | `src/views/workflow/task/` | 抄送列表页面 |
| `myDocument.vue` | `src/views/workflow/task/` | 我的单据页面 |
| `allTaskWaiting.vue` | `src/views/workflow/task/` | 全部待办页面 |
| `index.vue` | `src/views/workflow/spel/` | SpEL 表达式页面 |
| `index.vue` | `src/views/workflow/leave/` | 请假流程页面 |
| `leaveEdit.vue` | `src/views/workflow/leave/` | 请假编辑页面 |
| `index.ts` | `src/api/workflow/category/` | 流程分类 API |
| `types.ts` | `src/api/workflow/category/` | 流程分类类型 |
| `index.ts` | `src/api/workflow/definition/` | 流程定义 API |
| `types.ts` | `src/api/workflow/definition/` | 流程定义类型 |
| `index.ts` | `src/api/workflow/instance/` | 流程实例 API |
| `types.ts` | `src/api/workflow/instance/` | 流程实例类型 |
| `index.ts` | `src/api/workflow/task/` | 任务管理 API |
| `types.ts` | `src/api/workflow/task/` | 任务管理类型 |
| `index.ts` | `src/api/workflow/spel/` | SpEL API |
| `types.ts` | `src/api/workflow/spel/` | SpEL 类型 |
| `index.ts` | `src/api/workflow/leave/` | 请假 API |
| `types.ts` | `src/api/workflow/leave/` | 请假类型 |
| `index.ts` | `src/api/workflow/workflowCommon/` | 工作流通用 API |
| `types.ts` | `src/api/workflow/workflowCommon/` | 工作流通用类型 |

### 8.3 数据库表

| 表名 | 说明 | SQL 文件 |
|------|------|----------|
| `flow_category` | 流程分类表 | `sql/magic-ruoyi.sql` |
| `flow_spel` | SpEL 表达式表 | `sql/magic-ruoyi.sql` |
| `flow_definition` | 流程定义表 | Warm-Flow 引擎自动创建 |
| `flow_instance` | 流程实例表 | Warm-Flow 引擎自动创建 |
| `flow_task` | 任务表 | Warm-Flow 引擎自动创建 |
| `flow_node` | 节点表 | Warm-Flow 引擎自动创建 |
| `flow_skip` | 连线表 | Warm-Flow 引擎自动创建 |
| `flow_condition` | 条件表 | Warm-Flow 引擎自动创建 |
| `flow_his_task` | 历史任务表 | Warm-Flow 引擎自动创建 |
| `flow_variable` | 流程变量表 | Warm-Flow 引擎自动创建 |

---

## 9. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
