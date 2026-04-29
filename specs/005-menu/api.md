# 菜单管理API接口文档 (api.md)

> magic-ruoyi 菜单管理模块的API接口定义。
>
> 版本: 1.0.0 | 最后更新: 2026-04-29

---

## 1. 通用规范

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 模块路径 | `/system/menu` |
| 认证方式 | Sa-Token + JWT |
| 请求头 | `Authorization: Bearer {token}` |
| 租户头 | `Tenant-Id: {tenantId}` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 2. 菜单CRUD接口

### 2.1 查询菜单列表

```
GET /system/menu/list
```

**权限标识**: `system:menu:list`

**请求参数**:

| 参数 | 类型 | 必填 | 位置 | 描述 |
|------|------|------|------|------|
| menuName | String | 否 | Query | 菜单名称（模糊匹配） |
| status | String | 否 | Query | 状态（0=正常，1=停用） |
| keywords | String | 否 | Query | 关键字搜索 |

**响应示例**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "menuId": 1,
      "menuName": "系统管理",
      "parentId": 0,
      "parentName": "",
      "orderNum": 1,
      "path": "system",
      "component": null,
      "queryParam": null,
      "isFrame": 1,
      "isCache": 0,
      "menuType": "M",
      "visible": "0",
      "status": "0",
      "perms": null,
      "icon": "system",
      "remark": "系统管理目录",
      "createTime": "2026-04-28 10:00:00",
      "children": []
    }
  ]
}
```

### 2.2 获取菜单详情

```
GET /system/menu/{menuId}
```

**权限标识**: `system:menu:query`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| menuId | Long | 是 | 菜单ID |

**响应**: 返回 `MenuVO` 对象（同列表中的单条数据结构）

### 2.3 查询菜单下拉树结构

```
GET /system/menu/treeselect
```

**权限标识**: 无（内部调用）

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 1,
      "label": "系统管理",
      "parentId": 0,
      "weight": 1,
      "children": [
        {
          "id": 100,
          "label": "用户管理",
          "parentId": 1,
          "weight": 1
        }
      ]
    }
  ]
}
```

### 2.4 根据角色ID查询菜单树

```
GET /system/menu/roleMenuTreeselect/{roleId}
```

**权限标识**: 无（内部调用）

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| roleId | Long | 是 | 角色ID |

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "menus": [
      {
        "id": 1,
        "label": "系统管理",
        "parentId": 0,
        "weight": 1,
        "children": []
      }
    ],
    "checkedKeys": ["1", "100", "101", "102"]
  }
}
```

### 2.5 根据租户套餐ID查询菜单树

```
GET /system/menu/tenantPackageMenuTreeselect/{packageId}
```

**权限标识**: 无（内部调用）

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| packageId | Long | 是 | 租户套餐ID |

**响应**: 同角色菜单树结构

### 2.6 新增菜单

```
POST /system/menu
```

**权限标识**: `system:menu:add`

**请求体**:

```json
{
  "menuName": "测试菜单",
  "parentId": 0,
  "orderNum": 1,
  "path": "test",
  "component": null,
  "queryParam": null,
  "isFrame": "1",
  "isCache": "0",
  "menuType": "M",
  "visible": "0",
  "status": "0",
  "perms": null,
  "icon": "star",
  "remark": "测试菜单"
}
```

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| menuName | String | 是 | 菜单名称 |
| parentId | Long | 是 | 父菜单ID，0表示顶级 |
| orderNum | Integer | 是 | 显示顺序 |
| path | String | 条件 | 路由地址（目录/菜单类型必填） |
| component | String | 条件 | 组件路径（菜单类型必填） |
| queryParam | String | 否 | 路由参数 |
| isFrame | String | 否 | 是否外链（0=是，1=否），默认1 |
| isCache | String | 否 | 是否缓存（0=缓存，1=不缓存），默认0 |
| menuType | String | 是 | 菜单类型（M=目录，C=菜单，F=按钮） |
| visible | String | 否 | 显示状态（0=显示，1=隐藏），默认0 |
| status | String | 否 | 菜单状态（0=正常，1=停用），默认0 |
| perms | String | 条件 | 权限标识（菜单/按钮类型建议填写） |
| icon | String | 否 | 菜单图标 |
| remark | String | 否 | 备注 |

**响应**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### 2.7 修改菜单

```
PUT /system/menu
```

**权限标识**: `system:menu:edit`

**请求体**: 同新增，额外包含 `menuId` 字段

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| menuId | Long | 是 | 菜单ID |

### 2.8 删除菜单

```
DELETE /system/menu/{menuId}
```

**权限标识**: `system:menu:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| menuId | Long | 是 | 菜单ID |

**约束**: 存在子菜单时拒绝删除

### 2.9 级联删除菜单

```
DELETE /system/menu/cascade/{menuIds}
```

**权限标识**: `system:menu:remove`

**路径参数**:

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| menuIds | String | 是 | 菜单ID列表，逗号分隔，如 "100,101" |

**行为**: 删除选中菜单及其所有子菜单，同时清除 sys_role_menu 关联记录

---

## 3. 动态路由接口

### 3.1 获取前端路由信息

```
GET /system/menu/getRouters
```

**权限标识**: 无（登录后自动调用）

**请求参数**: 无（从当前登录用户上下文获取权限）

**响应**:

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "name": "System",
      "path": "/system",
      "hidden": false,
      "redirect": "noRedirect",
      "component": "Layout",
      "alwaysShow": true,
      "meta": {
        "title": "系统管理",
        "icon": "system",
        "noCache": false,
        "link": null
      },
      "children": [
        {
          "name": "User",
          "path": "user",
          "hidden": false,
          "component": "system/user/index",
          "meta": {
            "title": "用户管理",
            "icon": "user",
            "noCache": false,
            "link": null
          }
        }
      ]
    }
  ]
}
```

**路由字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 路由名称，取自菜单名称 |
| path | String | 路由路径 |
| hidden | Boolean | 是否隐藏（visible=1 时为 true） |
| redirect | String | 重定向地址，目录类型默认为 noRedirect |
| component | String | 组件路径，目录类型为 Layout |
| alwaysShow | Boolean | 是否始终显示根菜单，目录类型为 true |
| meta.title | String | 菜单标题 |
| meta.icon | String | 菜单图标 |
| meta.noCache | Boolean | 是否不缓存（isCache=1 时为 true） |
| meta.link | String | 外链地址（isFrame=0 时为完整 URL） |

---

## 4. 前端API客户端

### 4.1 模块路径

```
src/api/system/menu/index.ts
```

### 4.2 导出函数清单

| 函数名 | 对应接口 | 说明 |
|--------|----------|------|
| `listMenu` | GET /system/menu/list | 查询菜单列表 |
| `getMenu` | GET /system/menu/{menuId} | 查询菜单详情 |
| `treeselect` | GET /system/menu/treeselect | 查询菜单下拉树结构 |
| `roleMenuTreeselect` | GET /system/menu/roleMenuTreeselect/{roleId} | 根据角色ID查询菜单树 |
| `tenantPackageMenuTreeselect` | GET /system/menu/tenantPackageMenuTreeselect/{packageId} | 根据租户套餐ID查询菜单树 |
| `addMenu` | POST /system/menu | 新增菜单 |
| `updateMenu` | PUT /system/menu | 修改菜单 |
| `delMenu` | DELETE /system/menu/{menuId} | 删除菜单 |
| `cascadeDelMenu` | DELETE /system/menu/cascade/{menuIds} | 级联删除菜单 |

### 4.3 路由API

```
src/api/menu.ts
```

| 函数名 | 对应接口 | 说明 |
|--------|----------|------|
| `getRouters` | GET /system/menu/getRouters | 获取前端路由信息 |

### 4.4 类型定义

```typescript
// src/api/system/menu/types.ts

import { MenuTypeEnum } from '@/enums/MenuTypeEnum';

// 菜单树形结构类型
export interface MenuTreeOption {
  id: string | number;
  label: string;
  parentId: string | number;
  weight: number;
  children?: MenuTreeOption[];
}

// 角色菜单树响应类型
export interface RoleMenuTree {
  menus: MenuTreeOption[];
  checkedKeys: string[];
}

// 菜单查询参数
export interface MenuQuery {
  keywords?: string;
  menuName?: string;
  status?: string;
}

// 菜单视图对象
export interface MenuVO extends BaseEntity {
  parentName: string;
  parentId: string | number;
  children: MenuVO[];
  menuId: string | number;
  menuName: string;
  orderNum: number;
  path: string;
  component: string;
  queryParam: string;
  isFrame: string;
  isCache: string;
  menuType: MenuTypeEnum;
  visible: string;
  status: string;
  icon: string;
  remark: string;
}

// 菜单表单对象
export interface MenuForm {
  parentName?: string;
  parentId?: string | number;
  children?: MenuForm[];
  menuId?: string | number;
  menuName: string;
  orderNum: number;
  path: string;
  component?: string;
  queryParam?: string;
  isFrame?: string;
  isCache?: string;
  menuType?: MenuTypeEnum;
  visible?: string;
  status?: string;
  icon?: string;
  remark?: string;
  query?: string;
  perms?: string;
}
```

### 4.5 菜单类型枚举

```typescript
// src/enums/MenuTypeEnum.ts

export enum MenuTypeEnum {
  M = 'M',  // 目录
  C = 'C',  // 菜单
  F = 'F'   // 按钮
}
```

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，菜单管理API接口定义 |
