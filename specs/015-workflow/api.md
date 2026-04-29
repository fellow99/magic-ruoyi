# 工作流模块 API 文档（015-workflow/api.md）

> magic-ruoyi 工作流模块全部 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 后端服务地址 | `http://{host}:8080` |
| API 模块前缀 | `/workflow` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 流程分类接口

### 2.1 查询流程分类列表

```
GET /workflow/category/list
```

**认证**: 需要 Token
**权限**: `workflow:category:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| categoryName | String | 否 | 分类名称（模糊匹配） |

---

### 2.2 查询流程分类详情

```
GET /workflow/category/{categoryId}
```

**认证**: 需要 Token
**权限**: `workflow:category:list`

---

### 2.3 新增流程分类

```
POST /workflow/category
```

**认证**: 需要 Token
**权限**: `workflow:category:add`

**请求体**:

```json
{
  "categoryName": "人事管理",
  "parentId": 0,
  "orderNum": 1
}
```

---

### 2.4 修改流程分类

```
PUT /workflow/category
```

**认证**: 需要 Token
**权限**: `workflow:category:edit`

---

### 2.5 删除流程分类

```
DELETE /workflow/category/{categoryIds}
```

**认证**: 需要 Token
**权限**: `workflow:category:remove`

---

### 2.6 获取流程分类树

```
GET /workflow/category/categoryTree
```

**认证**: 需要 Token

---

## 3. 流程定义接口

### 3.1 查询流程定义列表

```
GET /workflow/definition/list
```

**认证**: 需要 Token
**权限**: `workflow:definition:list`

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| flowCode | String | 否 | 流程编码 |
| flowName | String | 否 | 流程名称 |
| category | String | 否 | 分类 ID |
| isPublish | Integer | 否 | 发布状态 |

---

### 3.2 查询未发布流程定义列表

```
GET /workflow/definition/unPublishList
```

**认证**: 需要 Token

---

### 3.3 新增流程定义

```
POST /workflow/definition
```

**认证**: 需要 Token
**权限**: `workflow:definition:add`

---

### 3.4 修改流程定义

```
PUT /workflow/definition
```

**认证**: 需要 Token
**权限**: `workflow:definition:edit`

---

### 3.5 查询流程定义详情

```
GET /workflow/definition/{id}
```

**认证**: 需要 Token

---

### 3.6 删除流程定义

```
DELETE /workflow/definition/{id}
```

**认证**: 需要 Token
**权限**: `workflow:definition:remove`

---

### 3.7 发布流程定义

```
PUT /workflow/definition/publish/{id}
```

**认证**: 需要 Token
**权限**: `workflow:definition:publish`

---

### 3.8 取消发布流程定义

```
PUT /workflow/definition/unPublish/{id}
```

**认证**: 需要 Token
**权限**: `workflow:definition:publish`

---

### 3.9 激活/挂起流程定义

```
PUT /workflow/definition/active/{definitionId}
```

**认证**: 需要 Token

**Query 参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| active | Boolean | 是 | true=激活, false=挂起 |

---

### 3.10 导入流程定义

```
POST /workflow/definition/importDef
```

**认证**: 需要 Token
**权限**: `workflow:definition:importDef`

**请求体**: 文件上传（ZIP/XML）

---

### 3.11 获取流程定义 XML

```
GET /workflow/definition/definitionXml/{definitionId}
```

**认证**: 需要 Token

---

### 3.12 获取流程定义 XML 字符串

```
GET /workflow/definition/xmlString/{id}
```

**认证**: 需要 Token

---

### 3.13 复制流程定义

```
POST /workflow/definition/copy/{id}
```

**认证**: 需要 Token

---

## 4. 流程实例接口

### 4.1 查询运行中实例列表

```
GET /workflow/instance/pageByRunning
```

**认证**: 需要 Token
**权限**: `workflow:instance:list`

---

### 4.2 查询已完成实例列表

```
GET /workflow/instance/pageByFinish
```

**认证**: 需要 Token
**权限**: `workflow:instance:list`

---

### 4.3 查询当前用户单据列表

```
GET /workflow/instance/pageByCurrent
```

**认证**: 需要 Token

---

### 4.4 获取历史流程图

```
GET /workflow/instance/flowHisTaskList/{businessId}
```

**认证**: 需要 Token

---

### 4.5 撤销流程

```
PUT /workflow/instance/cancelProcessApply
```

**认证**: 需要 Token

---

### 4.6 作废流程

```
POST /workflow/instance/invalid
```

**认证**: 需要 Token

---

### 4.7 删除流程实例

```
DELETE /workflow/instance/deleteByInstanceIds/{instanceIds}
```

**认证**: 需要 Token

---

### 4.8 删除历史流程实例

```
DELETE /workflow/instance/deleteHisByInstanceIds/{instanceIds}
```

**认证**: 需要 Token

---

### 4.9 获取流程变量

```
GET /workflow/instance/instanceVariable/{instanceId}
```

**认证**: 需要 Token

---

### 4.10 修改流程变量

```
PUT /workflow/instance/updateVariable
```

**认证**: 需要 Token

---

## 5. 任务接口

### 5.1 查询待办任务列表

```
GET /workflow/task/pageByTaskWait
```

**认证**: 需要 Token
**权限**: `workflow:task:list`

---

### 5.2 查询已办任务列表

```
GET /workflow/task/pageByTaskFinish
```

**认证**: 需要 Token
**权限**: `workflow:task:list`

---

### 5.3 查询抄送列表

```
GET /workflow/task/pageByTaskCopy
```

**认证**: 需要 Token

---

### 5.4 查询租户全部待办

```
GET /workflow/task/pageByAllTaskWait
```

**认证**: 需要 Token

---

### 5.5 查询租户全部已办

```
GET /workflow/task/pageByAllTaskFinish
```

**认证**: 需要 Token

---

### 5.6 启动流程

```
POST /workflow/task/startWorkFlow
```

**认证**: 需要 Token

---

### 5.7 审批任务

```
POST /workflow/task/completeTask
```

**认证**: 需要 Token
**权限**: `workflow:task:complete`

---

### 5.8 驳回任务

```
POST /workflow/task/backProcess
```

**认证**: 需要 Token
**权限**: `workflow:task:back`

---

### 5.9 获取当前任务

```
GET /workflow/task/getTask/{taskId}
```

**认证**: 需要 Token

---

### 5.10 修改任务办理人

```
PUT /workflow/task/updateAssignee/{userId}
```

**认证**: 需要 Token

---

### 5.11 终止任务

```
POST /workflow/task/terminationTask
```

**认证**: 需要 Token

---

### 5.12 获取可驳回的任务节点

```
GET /workflow/task/getBackTaskNode/{taskId}/{nodeCode}
```

**认证**: 需要 Token

---

### 5.13 任务操作（转办/委派/加签/减签）

```
POST /workflow/task/taskOperation/{operation}
```

**认证**: 需要 Token

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| operation | String | 是 | delegateTask/transferTask/addSignature/reductionSignature |

---

### 5.14 获取当前任务办理人

```
GET /workflow/task/currentTaskAllUser/{taskId}
```

**认证**: 需要 Token

---

### 5.15 获取下一节点

```
POST /workflow/task/getNextNodeList
```

**认证**: 需要 Token

---

### 5.16 催办任务

```
POST /workflow/task/urgeTask
```

**认证**: 需要 Token

---

## 6. SpEL 表达式接口

### 6.1 查询 SpEL 列表

```
GET /workflow/spel/list
```

**认证**: 需要 Token
**权限**: `workflow:spel:list`

---

### 6.2 查询 SpEL 详情

```
GET /workflow/spel/{id}
```

**认证**: 需要 Token

---

### 6.3 新增 SpEL

```
POST /workflow/spel
```

**认证**: 需要 Token
**权限**: `workflow:spel:add`

---

### 6.4 修改 SpEL

```
PUT /workflow/spel
```

**认证**: 需要 Token
**权限**: `workflow:spel:edit`

---

### 6.5 删除 SpEL

```
DELETE /workflow/spel/{ids}
```

**认证**: 需要 Token
**权限**: `workflow:spel:remove`

---

## 7. 前端 API 封装

### 7.1 流程分类（`@/api/workflow/category/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listCategory(query)` | GET | `/workflow/category/list` | 查询分类列表 |
| `getCategory(categoryId)` | GET | `/workflow/category/{categoryId}` | 查询分类详情 |
| `addCategory(data)` | POST | `/workflow/category` | 新增分类 |
| `updateCategory(data)` | PUT | `/workflow/category` | 修改分类 |
| `delCategory(categoryIds)` | DELETE | `/workflow/category/{categoryIds}` | 删除分类 |
| `categoryTree(query)` | GET | `/workflow/category/categoryTree` | 获取分类树 |

### 7.2 流程定义（`@/api/workflow/definition/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listDefinition(query)` | GET | `/workflow/definition/list` | 查询定义列表 |
| `unPublishList(query)` | GET | `/workflow/definition/unPublishList` | 查询未发布列表 |
| `add(data)` | POST | `/workflow/definition` | 新增定义 |
| `edit(data)` | PUT | `/workflow/definition` | 修改定义 |
| `getInfo(id)` | GET | `/workflow/definition/{id}` | 查询定义详情 |
| `deleteDefinition(id)` | DELETE | `/workflow/definition/{id}` | 删除定义 |
| `publish(id)` | PUT | `/workflow/definition/publish/{id}` | 发布定义 |
| `unPublish(id)` | PUT | `/workflow/definition/unPublish/{id}` | 取消发布 |
| `active(definitionId, active)` | PUT | `/workflow/definition/active/{definitionId}` | 激活/挂起 |
| `importDef(data)` | POST | `/workflow/definition/importDef` | 导入定义 |
| `definitionXml(definitionId)` | GET | `/workflow/definition/definitionXml/{definitionId}` | 获取 XML |
| `xmlString(id)` | GET | `/workflow/definition/xmlString/{id}` | 获取 XML 字符串 |
| `copy(id)` | POST | `/workflow/definition/copy/{id}` | 复制定义 |

### 7.3 流程实例（`@/api/workflow/instance/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `pageByRunning(query)` | GET | `/workflow/instance/pageByRunning` | 运行中实例 |
| `pageByFinish(query)` | GET | `/workflow/instance/pageByFinish` | 已完成实例 |
| `pageByCurrent(query)` | GET | `/workflow/instance/pageByCurrent` | 我的单据 |
| `flowHisTaskList(businessId)` | GET | `/workflow/instance/flowHisTaskList/{businessId}` | 历史流程图 |
| `cancelProcessApply(data)` | PUT | `/workflow/instance/cancelProcessApply` | 撤销流程 |
| `invalid(data)` | POST | `/workflow/instance/invalid` | 作废流程 |
| `deleteByInstanceIds(instanceIds)` | DELETE | `/workflow/instance/deleteByInstanceIds/{instanceIds}` | 删除实例 |
| `deleteHisByInstanceIds(instanceIds)` | DELETE | `/workflow/instance/deleteHisByInstanceIds/{instanceIds}` | 删除历史实例 |
| `instanceVariable(instanceId)` | GET | `/workflow/instance/instanceVariable/{instanceId}` | 获取流程变量 |
| `updateVariable(data)` | PUT | `/workflow/instance/updateVariable` | 修改流程变量 |

### 7.4 任务（`@/api/workflow/task/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `pageByTaskWait(query)` | GET | `/workflow/task/pageByTaskWait` | 待办任务 |
| `pageByTaskFinish(query)` | GET | `/workflow/task/pageByTaskFinish` | 已办任务 |
| `pageByTaskCopy(query)` | GET | `/workflow/task/pageByTaskCopy` | 抄送列表 |
| `pageByAllTaskWait(query)` | GET | `/workflow/task/pageByAllTaskWait` | 全部待办 |
| `pageByAllTaskFinish(query)` | GET | `/workflow/task/pageByAllTaskFinish` | 全部已办 |
| `startWorkFlow(data)` | POST | `/workflow/task/startWorkFlow` | 启动流程 |
| `completeTask(data)` | POST | `/workflow/task/completeTask` | 审批任务 |
| `backProcess(data)` | POST | `/workflow/task/backProcess` | 驳回任务 |
| `getTask(taskId)` | GET | `/workflow/task/getTask/{taskId}` | 获取当前任务 |
| `updateAssignee(taskIdList, userId)` | PUT | `/workflow/task/updateAssignee/{userId}` | 修改办理人 |
| `terminationTask(data)` | POST | `/workflow/task/terminationTask` | 终止任务 |
| `getBackTaskNode(taskId, nodeCode)` | GET | `/workflow/task/getBackTaskNode/{taskId}/{nodeCode}` | 获取可驳回节点 |
| `taskOperation(data, operation)` | POST | `/workflow/task/taskOperation/{operation}` | 任务操作 |
| `currentTaskAllUser(taskId)` | GET | `/workflow/task/currentTaskAllUser/{taskId}` | 获取当前办理人 |
| `getNextNodeList(data)` | POST | `/workflow/task/getNextNodeList` | 获取下一节点 |
| `urgeTask(data)` | POST | `/workflow/task/urgeTask` | 催办任务 |

### 7.5 SpEL（`@/api/workflow/spel/index.ts`）

| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `listSpel(query)` | GET | `/workflow/spel/list` | 查询 SpEL 列表 |
| `getSpel(id)` | GET | `/workflow/spel/{id}` | 查询 SpEL 详情 |
| `addSpel(data)` | POST | `/workflow/spel` | 新增 SpEL |
| `updateSpel(data)` | PUT | `/workflow/spel` | 修改 SpEL |
| `delSpel(ids)` | DELETE | `/workflow/spel/{ids}` | 删除 SpEL |

---

## 8. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
