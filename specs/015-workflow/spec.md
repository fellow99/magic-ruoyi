# 工作流模块规格文档（015-workflow/spec.md）

> magic-ruoyi 工作流模块。基于 Warm-Flow 引擎实现流程定义、流程实例、任务审批等功能。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 模块概述

工作流模块基于 Warm-Flow 轻量级工作流引擎，提供完整的 BPM 能力，包括流程分类管理、流程定义设计与发布、流程实例运行、任务审批处理、SpEL 表达式管理等。

### 1.1 技术栈

| 组件 | 技术选型 |
|------|----------|
| 工作流引擎 | Warm-Flow |
| 后端框架 | Spring Boot + MyBatis-Plus |
| 前端框架 | Vue 3.5 + Element Plus |
| 表达式引擎 | Spring SpEL |

### 1.2 核心功能

- **流程分类**: 树形分类管理
- **流程定义**: 流程模型设计、XML 导入、发布/取消发布、激活/挂起
- **流程实例**: 运行中/已完成实例管理、流程撤销、作废
- **任务管理**: 待办/已办/抄送列表、审批、驳回、转办、委派、加签、减签、终止、催办
- **SpEL 表达式**: 流程条件表达式管理
- **请假示例**: 完整的请假流程演示

### 1.3 模块结构

```
工作流
├── 流程分类 (category/index.vue)           → /workflow/category
├── 流程定义 (processDefinition/index.vue)   → /workflow/processDefinition
├── 流程实例 (processInstance/index.vue)     → /workflow/processInstance
├── 任务管理 (task/index.vue)               → /workflow/task
├── 请假流程 (leave/index.vue)              → /workflow/leave
└── SpEL 表达式 (spel/index.vue)            → /workflow/spel
```

---

## 2. 流程分类

### 2.1 功能

- 树形结构管理流程分类
- 支持新增、修改、删除分类节点
- 按分类筛选流程定义

### 2.2 数据模型

| 字段 | 类型 | 说明 |
|------|------|------|
| categoryId | Long | 分类 ID |
| categoryName | String | 分类名称 |
| parentId | Long | 父级 ID |
| orderNum | Integer | 显示顺序 |

---

## 3. 流程定义

### 3.1 功能

- **列表查询**: 按流程编码、流程名称、分类、发布状态筛选
- **新增定义**: 创建空白流程定义
- **导入定义**: 上传 ZIP/XML 文件导入流程模型
- **发布/取消发布**: 控制流程是否可用
- **激活/挂起**: 控制已发布流程是否可发起新实例
- **复制定义**: 基于已有定义创建新版本
- **删除定义**: 删除未发布的流程定义
- **XML 预览**: 查看流程模型 XML

### 3.2 流程状态

| 状态 | 说明 |
|------|------|
| isPublish=0 | 未发布 |
| isPublish=1 | 已发布 |
| activityStatus=0 | 挂起 |
| activityStatus=1 | 激活 |

---

## 4. 流程实例

### 4.1 功能

- **运行中实例**: 查看当前正在运行的流程实例
- **已完成实例**: 查看已完成的流程实例
- **我的单据**: 查看当前用户发起的单据
- **流程撤销**: 撤销尚未审批完成的流程
- **流程作废**: 作废已完成的流程实例
- **删除实例**: 删除运行中/历史流程实例
- **流程变量**: 查看和修改流程变量
- **流程图**: 查看历史流程流转轨迹

---

## 5. 任务管理

### 5.1 功能

- **待办任务**: 当前用户需要处理的任务
- **已办任务**: 当前用户已处理的任务
- **抄送列表**: 当前用户被抄送的流程
- **全部待办/已办**: 租户维度的任务列表
- **审批任务**: 同意/驳回
- **任务操作**: 转办、委派、加签、减签
- **终止任务**: 终止流程
- **催办任务**: 提醒当前处理人

### 5.2 任务操作类型

| 操作 | 说明 |
|------|------|
| completeTask | 审批通过 |
| backProcess | 驳回 |
| transferTask | 转办 |
| delegateTask | 委派 |
| addSignature | 加签 |
| reductionSignature | 减签 |
| terminationTask | 终止 |
| urgeTask | 催办 |

---

## 6. SpEL 表达式

### 6.1 功能

- 管理流程中使用的 SpEL 表达式
- 支持组件名称、方法名、参数配置
- 预览表达式计算结果
- 状态管理（正常/停用）

---

## 7. 权限控制

| 权限标识 | 功能 | 按钮/操作 |
|----------|------|-----------|
| `workflow:category:list` | 查看流程分类 | 页面访问 |
| `workflow:category:add` | 新增流程分类 | 新增按钮 |
| `workflow:category:edit` | 修改流程分类 | 修改按钮 |
| `workflow:category:remove` | 删除流程分类 | 删除按钮 |
| `workflow:definition:list` | 查看流程定义 | 页面访问 |
| `workflow:definition:add` | 新增流程定义 | 新增按钮 |
| `workflow:definition:edit` | 修改流程定义 | 修改按钮 |
| `workflow:definition:remove` | 删除流程定义 | 删除按钮 |
| `workflow:definition:publish` | 发布流程定义 | 发布按钮 |
| `workflow:definition:importDef` | 导入流程定义 | 导入按钮 |
| `workflow:instance:list` | 查看流程实例 | 页面访问 |
| `workflow:instance:cancel` | 撤销流程 | 撤销按钮 |
| `workflow:task:list` | 查看任务列表 | 页面访问 |
| `workflow:task:complete` | 审批任务 | 审批按钮 |
| `workflow:task:back` | 驳回任务 | 驳回按钮 |
| `workflow:spel:list` | 查看 SpEL 列表 | 页面访问 |
| `workflow:spel:add` | 新增 SpEL | 新增按钮 |
| `workflow:spel:edit` | 修改 SpEL | 修改按钮 |
| `workflow:spel:remove` | 删除 SpEL | 删除按钮 |

---

## 8. 流程定义详细配置

### 8.1 设计器模式

| 模式 | 值 | 说明 |
|------|-----|------|
| 经典模式 | `CLASSICS` | 传统 BPMN 风格设计器 |
| 仿钉钉模式 | `MIMIC` | 类钉钉审批流设计器 |

### 8.2 发布状态

| 值 | 说明 |
|----|------|
| `0` | 未发布 |
| `1` | 已发布 |
| `2` | 失效（被新版本替代） |

### 8.3 流程定义字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 流程定义 ID |
| `flow_name` | VARCHAR | 流程名称 |
| `flow_code` | VARCHAR | 流程编码（唯一标识） |
| `category` | String | 所属分类 ID |
| `version` | String | 版本号 |
| `is_publish` | INT | 发布状态 |
| `activity_status` | INT | 激活状态 |
| `form_custom` | CHAR | 是否动态表单（Y/N） |
| `form_path` | VARCHAR | 表单组件路径 |
| `model_value` | VARCHAR | 设计器模式 |
| `ext` | VARCHAR | 扩展配置（JSON），含 autoPass 等 |

### 8.4 任务数据结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 任务 ID |
| `instance_id` | String | 所属流程实例 ID |
| `flow_name` | String | 流程名称 |
| `flow_code` | String | 流程编码 |
| `node_code` | String | 节点编码 |
| `node_name` | String | 节点名称 |
| `node_type` | INT | 节点类型 |
| `business_id` | String | 业务 ID |
| `business_code` | String | 业务编码 |
| `business_title` | String | 业务标题 |
| `form_custom` | String | 是否动态表单 |
| `form_path` | String | 表单组件路径 |
| `button_list` | Array | 可用操作按钮列表 |
| `copy_list` | Array | 抄送人列表 |
| `var_list` | Map | 流程变量 |

---

## 9. 配置项

### 9.1 application.yml

```yaml
warm-flow:
  enabled: true    # 是否开启工作流引擎
  ui: true         # 是否开启工作流设计器
```

---

## 10. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
