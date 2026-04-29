# Magic-API 动态接口引擎模块 API 文档（016-magic-api/api.md）

> magic-ruoyi Magic-API 模块 API 接口定义。
>
> 版本: 1.0.0 | 日期: 2026-04-29

---

## 1. 模块概述

Magic-API 模块不提供传统的 REST API 列表，而是通过 `/magic/web` 编辑器界面进行在线接口开发。编辑器本身提供以下内置 API：

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 编辑器地址 | `http://{host}:8080/magic/web` |
| 接口执行前缀 | `/magic/api` |
| API 文档 | 编辑器内置 |

### 1.2 编辑器内置 API

Magic-API 编辑器提供以下内置管理接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/magic/api/debug` | POST | 调试接口 |
| `/magic/api/save` | POST | 保存接口配置 |
| `/magic/api/delete` | POST | 删除接口配置 |
| `/magic/api/publish` | POST | 发布接口 |
| `/magic/api/offline` | POST | 下线接口 |
| `/magic/api/export` | GET | 导出接口配置 |
| `/magic/api/import` | POST | 导入接口配置 |
| `/magic/function/save` | POST | 保存函数 |
| `/magic/function/delete` | POST | 删除函数 |
| `/magic/datasource/save` | POST | 保存数据源 |
| `/magic/datasource/delete` | POST | 删除数据源 |
| `/magic/datasource/test` | POST | 测试数据源连接 |

---

## 2. 动态接口执行

### 2.1 执行动态接口

```
{请求方法} /magic/api/{接口路径}
```

**认证**: 取决于接口配置的权限

**说明**: 动态接口的请求方法、路径、参数均由编辑器中配置的接口定义决定。

---

## 3. MagicScript 脚本 API

### 3.1 数据库操作

| 方法 | 说明 | 示例 |
|------|------|------|
| `db.select(sql, params)` | 查询 | `db.select("SELECT * FROM user WHERE id = ?", id)` |
| `db.insert(sql, params)` | 插入 | `db.insert("INSERT INTO user(name) VALUES (?)", name)` |
| `db.update(sql, params)` | 更新 | `db.update("UPDATE user SET name = ? WHERE id = ?", name, id)` |
| `db.delete(sql, params)` | 删除 | `db.delete("DELETE FROM user WHERE id = ?", id)` |
| `db.page(sql, params, pageNum, pageSize)` | 分页查询 | `db.page("SELECT * FROM user", null, 1, 10)` |

### 3.2 HTTP 请求

| 方法 | 说明 | 示例 |
|------|------|------|
| `http.get(url, params)` | GET 请求 | `http.get("https://api.example.com/users", {id: 1})` |
| `http.post(url, body)` | POST 请求 | `http.post("https://api.example.com/users", {name: "test"})` |
| `http.put(url, body)` | PUT 请求 | `http.put("https://api.example.com/users/1", {name: "updated"})` |
| `http.delete(url)` | DELETE 请求 | `http.delete("https://api.example.com/users/1")` |

### 3.3 工具函数

| 方法 | 说明 | 示例 |
|------|------|------|
| `log.info(msg)` | 信息日志 | `log.info("用户登录: " + username)` |
| `log.error(msg)` | 错误日志 | `log.error("操作失败")` |
| `json.parse(str)` | JSON 解析 | `json.parse('{"name": "test"}')` |
| `json.stringify(obj)` | JSON 序列化 | `json.stringify({name: "test"})` |
| `date.format(date, pattern)` | 日期格式化 | `date.format(new Date(), "yyyy-MM-dd")` |

---

## 4. 编辑器访问控制

### 4.1 访问权限

编辑器 `/magic/web` 的访问通过 Sa-Token 进行权限控制，仅授权用户可访问。

### 4.2 配置方式

在 `application.yml` 中配置 Magic-API：

```yaml
magic-api:
  web: /magic/web
  prefix: /magic/api
  resource:
    datasource: magic-api
  auth:
    enabled: true
```

---

## 5. 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-04-29 | 初始版本，基于现有代码逆向生成 |
