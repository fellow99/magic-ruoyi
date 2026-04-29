# magic-ruoyi REST API 清单

> 本文档列出 magic-ruoyi 项目的所有 REST API 接口。项目继承自 RuoYi-Vue-Plus，并融合 magic-api 动态接口能力。

---

## 统一响应格式

所有接口返回统一的 `R<T>` 结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 表示成功，500 表示失败 |
| msg | string | 提示信息 |
| data | T | 业务数据，类型随接口变化 |

---

## 认证机制

magic-ruoyi 使用 **Sa-Token** 作为认证框架。

### 认证方式

- 登录后返回 `access_token`，后续请求通过请求头携带
- 请求头格式：`Authorization: Bearer <token>`

### 特殊请求头

| 请求头 | 类型 | 说明 |
|--------|------|------|
| `isToken` | boolean | 是否需要 token 校验，默认 true。登录、注册、验证码等接口设为 false |
| `isEncrypt` | boolean | 是否启用请求体加密，默认 false。登录、注册等敏感接口设为 true |
| `repeatSubmit` | boolean | 是否防重复提交，默认 true |

### 免认证接口

以下接口标注 `@SaIgnore`，无需 token 即可访问：
- 所有 `/auth/*` 登录相关接口
- `/auth/code` 验证码
- `/resource/sms/code` 短信验证码
- `/resource/email/code` 邮箱验证码
- `/` 首页

---

## API 清单

### 1. 认证授权模块

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/auth/login` | POST | 用户登录（支持密码、短信、邮箱、小程序、第三方等多种认证方式） | `AuthController.java` |
| `/auth/logout` | POST | 退出登录 | `AuthController.java` |
| `/auth/register` | POST | 用户注册 | `AuthController.java` |
| `/auth/binding/{source}` | GET | 获取第三方授权跳转URL | `AuthController.java` |
| `/auth/social/callback` | POST | 第三方登录回调绑定 | `AuthController.java` |
| `/auth/unlock/{socialId}` | DELETE | 取消第三方授权 | `AuthController.java` |
| `/auth/tenant/list` | GET | 登录页租户下拉列表 | `AuthController.java` |
| `/auth/code` | GET | 生成图形验证码 | `CaptchaController.java` |

### 2. 验证码模块

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/resource/sms/code` | GET | 发送短信验证码（限流：60秒/1次） | `CaptchaController.java` |
| `/resource/email/code` | GET | 发送邮箱验证码（限流：60秒/1次） | `CaptchaController.java` |

### 3. 系统管理 - 用户管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/user/list` | GET | 分页查询用户列表 | `SysUserController` |
| `/system/user/{userId}` | GET | 获取用户详情 | `SysUserController` |
| `/system/user/getInfo` | GET | 获取当前登录用户信息 | `SysUserController` |
| `/system/user` | POST | 新增用户 | `SysUserController` |
| `/system/user` | PUT | 修改用户 | `SysUserController` |
| `/system/user/{userIds}` | DELETE | 删除用户 | `SysUserController` |
| `/system/user/resetPwd` | PUT | 重置用户密码 | `SysUserController` |
| `/system/user/changeStatus` | PUT | 修改用户状态 | `SysUserController` |
| `/system/user/export` | POST | 导出用户数据 | `SysUserController` |
| `/system/user/importData` | POST | 导入用户数据 | `SysUserController` |
| `/system/user/optionselect` | GET | 根据用户IDs查询用户（下拉选择） | `SysUserController` |
| `/system/user/profile` | GET | 查询当前用户个人信息 | `SysUserController` |
| `/system/user/profile` | PUT | 修改当前用户个人信息 | `SysUserController` |
| `/system/user/profile/updatePwd` | PUT | 修改当前用户密码 | `SysUserController` |
| `/system/user/profile/avatar` | POST | 上传用户头像 | `SysUserController` |
| `/system/user/authRole/{userId}` | GET | 查询用户已授权角色 | `SysUserController` |
| `/system/user/authRole` | PUT | 保存用户角色授权 | `SysUserController` |
| `/system/user/list/dept/{deptId}` | GET | 查询指定部门下的所有用户 | `SysUserController` |
| `/system/user/deptTree` | GET | 查询部门下拉树结构 | `SysUserController` |

### 4. 系统管理 - 角色管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/role/list` | GET | 分页查询角色列表 | `SysRoleController` |
| `/system/role/{roleId}` | GET | 获取角色详情 | `SysRoleController` |
| `/system/role` | POST | 新增角色 | `SysRoleController` |
| `/system/role` | PUT | 修改角色 | `SysRoleController` |
| `/system/role/{roleIds}` | DELETE | 删除角色 | `SysRoleController` |
| `/system/role/dataScope` | PUT | 修改角色数据权限 | `SysRoleController` |
| `/system/role/changeStatus` | PUT | 修改角色状态 | `SysRoleController` |
| `/system/role/optionselect` | GET | 根据角色IDs查询角色（下拉选择） | `SysRoleController` |
| `/system/role/authUser/allocatedList` | GET | 查询角色已授权用户列表 | `SysRoleController` |
| `/system/role/authUser/unallocatedList` | GET | 查询角色未授权用户列表 | `SysRoleController` |
| `/system/role/authUser/cancel` | PUT | 取消用户角色授权 | `SysRoleController` |
| `/system/role/authUser/cancelAll` | PUT | 批量取消用户角色授权 | `SysRoleController` |
| `/system/role/authUser/selectAll` | PUT | 批量授权用户选择 | `SysRoleController` |
| `/system/role/deptTree/{roleId}` | GET | 根据角色ID查询部门树 | `SysRoleController` |

### 5. 系统管理 - 菜单管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/menu/list` | GET | 查询菜单列表 | `SysMenuController` |
| `/system/menu/{menuId}` | GET | 获取菜单详情 | `SysMenuController` |
| `/system/menu` | POST | 新增菜单 | `SysMenuController` |
| `/system/menu` | PUT | 修改菜单 | `SysMenuController` |
| `/system/menu/{menuId}` | DELETE | 删除菜单 | `SysMenuController` |
| `/system/menu/cascade/{menuIds}` | DELETE | 级联删除菜单 | `SysMenuController` |
| `/system/menu/getRouters` | GET | 获取前端路由信息 | `SysMenuController` |
| `/system/menu/treeselect` | GET | 查询菜单下拉树结构 | `SysMenuController` |
| `/system/menu/roleMenuTreeselect/{roleId}` | GET | 根据角色ID查询菜单树 | `SysMenuController` |
| `/system/menu/tenantPackageMenuTreeselect/{packageId}` | GET | 根据租户套餐ID查询菜单树 | `SysMenuController` |

### 6. 系统管理 - 部门管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/dept/list` | GET | 查询部门列表 | `SysDeptController` |
| `/system/dept/{deptId}` | GET | 获取部门详情 | `SysDeptController` |
| `/system/dept` | POST | 新增部门 | `SysDeptController` |
| `/system/dept` | PUT | 修改部门 | `SysDeptController` |
| `/system/dept/{deptId}` | DELETE | 删除部门 | `SysDeptController` |
| `/system/dept/list/exclude/{deptId}` | GET | 查询部门列表（排除指定节点） | `SysDeptController` |
| `/system/dept/optionselect` | GET | 根据部门IDs查询部门（下拉选择） | `SysDeptController` |

### 7. 系统管理 - 岗位管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/post/list` | GET | 查询岗位列表 | `SysPostController` |
| `/system/post/{postId}` | GET | 获取岗位详情 | `SysPostController` |
| `/system/post` | POST | 新增岗位 | `SysPostController` |
| `/system/post` | PUT | 修改岗位 | `SysPostController` |
| `/system/post/{postId}` | DELETE | 删除岗位 | `SysPostController` |
| `/system/post/optionselect` | GET | 查询岗位下拉选择列表 | `SysPostController` |
| `/system/post/deptTree` | GET | 查询部门下拉树结构 | `SysPostController` |

### 8. 系统管理 - 字典管理

#### 字典类型

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/dict/type/list` | GET | 查询字典类型列表 | `SysDictTypeController` |
| `/system/dict/type/{dictId}` | GET | 获取字典类型详情 | `SysDictTypeController` |
| `/system/dict/type` | POST | 新增字典类型 | `SysDictTypeController` |
| `/system/dict/type` | PUT | 修改字典类型 | `SysDictTypeController` |
| `/system/dict/type/{dictId}` | DELETE | 删除字典类型 | `SysDictTypeController` |
| `/system/dict/type/refreshCache` | DELETE | 刷新字典缓存 | `SysDictTypeController` |
| `/system/dict/type/optionselect` | GET | 查询字典类型下拉列表 | `SysDictTypeController` |

#### 字典数据

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/dict/data/list` | GET | 查询字典数据列表 | `SysDictDataController` |
| `/system/dict/data/{dictCode}` | GET | 获取字典数据详情 | `SysDictDataController` |
| `/system/dict/data` | POST | 新增字典数据 | `SysDictDataController` |
| `/system/dict/data` | PUT | 修改字典数据 | `SysDictDataController` |
| `/system/dict/data/{dictCode}` | DELETE | 删除字典数据 | `SysDictDataController` |
| `/system/dict/data/type/{dictType}` | GET | 根据字典类型查询字典数据 | `SysDictDataController` |

### 9. 系统管理 - 参数配置

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/config/list` | GET | 查询参数列表 | `SysConfigController` |
| `/system/config/{configId}` | GET | 获取参数详情 | `SysConfigController` |
| `/system/config` | POST | 新增参数 | `SysConfigController` |
| `/system/config` | PUT | 修改参数 | `SysConfigController` |
| `/system/config/{configId}` | DELETE | 删除参数 | `SysConfigController` |
| `/system/config/configKey/{configKey}` | GET | 根据参数键名查询参数值 | `SysConfigController` |
| `/system/config/updateByKey` | PUT | 根据键名修改参数值 | `SysConfigController` |
| `/system/config/refreshCache` | DELETE | 刷新参数缓存 | `SysConfigController` |

### 10. 系统管理 - 通知公告

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/notice/list` | GET | 查询公告列表 | `SysNoticeController` |
| `/system/notice/{noticeId}` | GET | 获取公告详情 | `SysNoticeController` |
| `/system/notice` | POST | 新增公告 | `SysNoticeController` |
| `/system/notice` | PUT | 修改公告 | `SysNoticeController` |
| `/system/notice/{noticeId}` | DELETE | 删除公告 | `SysNoticeController` |

### 11. 系统管理 - 客户端管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/client/list` | GET | 查询客户端列表 | `SysClientController` |
| `/system/client/{id}` | GET | 获取客户端详情 | `SysClientController` |
| `/system/client` | POST | 新增客户端 | `SysClientController` |
| `/system/client` | PUT | 修改客户端 | `SysClientController` |
| `/system/client/{id}` | DELETE | 删除客户端 | `SysClientController` |
| `/system/client/changeStatus` | PUT | 修改客户端状态 | `SysClientController` |

### 12. 系统管理 - 社交账号

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/social/list` | GET | 查询社交账号授权列表 | `SysSocialController` |

### 13. 租户管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/tenant/list` | GET | 分页查询租户列表 | `SysTenantController` |
| `/system/tenant/{id}` | GET | 获取租户详情 | `SysTenantController` |
| `/system/tenant` | POST | 新增租户 | `SysTenantController` |
| `/system/tenant` | PUT | 修改租户 | `SysTenantController` |
| `/system/tenant/{ids}` | DELETE | 删除租户 | `SysTenantController` |
| `/system/tenant/changeStatus` | PUT | 修改租户状态 | `SysTenantController` |
| `/system/tenant/dynamic/{tenantId}` | GET | 动态切换租户 | `SysTenantController` |
| `/system/tenant/dynamic/clear` | GET | 清除动态租户切换 | `SysTenantController` |
| `/system/tenant/syncTenantPackage` | GET | 同步租户套餐 | `SysTenantController` |
| `/system/tenant/syncTenantDict` | GET | 同步租户字典 | `SysTenantController` |
| `/system/tenant/syncTenantConfig` | GET | 同步租户配置 | `SysTenantController` |

### 14. 租户套餐管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/system/tenant/package/list` | GET | 查询租户套餐列表 | `SysTenantPackageController` |
| `/system/tenant/package/{packageId}` | GET | 获取租户套餐详情 | `SysTenantPackageController` |
| `/system/tenant/package` | POST | 新增租户套餐 | `SysTenantPackageController` |
| `/system/tenant/package` | PUT | 修改租户套餐 | `SysTenantPackageController` |
| `/system/tenant/package/{packageId}` | DELETE | 删除租户套餐 | `SysTenantPackageController` |
| `/system/tenant/package/changeStatus` | PUT | 修改租户套餐状态 | `SysTenantPackageController` |
| `/system/tenant/package/selectList` | GET | 查询租户套餐下拉列表 | `SysTenantPackageController` |

### 15. OSS 文件存储

#### OSS 对象管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/resource/oss/list` | GET | 分页查询 OSS 对象列表 | `SysOssController` |
| `/resource/oss/listByIds/{ossId}` | GET | 根据IDs查询 OSS 对象 | `SysOssController` |
| `/resource/oss/{ossIds}` | DELETE | 删除 OSS 对象 | `SysOssController` |

> 注：文件上传通过 `/resource/oss/upload` 接口，具体实现取决于 OSS 配置（本地、MinIO、阿里云、腾讯云、七牛云等）。

#### OSS 配置管理

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/resource/oss/config/list` | GET | 查询 OSS 配置列表 | `SysOssConfigController` |
| `/resource/oss/config/{ossConfigId}` | GET | 获取 OSS 配置详情 | `SysOssConfigController` |
| `/resource/oss/config` | POST | 新增 OSS 配置 | `SysOssConfigController` |
| `/resource/oss/config` | PUT | 修改 OSS 配置 | `SysOssConfigController` |
| `/resource/oss/config/{ossConfigId}` | DELETE | 删除 OSS 配置 | `SysOssConfigController` |
| `/resource/oss/config/changeStatus` | PUT | 修改 OSS 配置状态 | `SysOssConfigController` |

### 16. 系统监控 - 在线用户

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/monitor/online/list` | GET | 查询在线用户列表 | `SysUserOnlineController` |
| `/monitor/online/{tokenId}` | DELETE | 强退在线用户 | `SysUserOnlineController` |
| `/monitor/online` | GET | 获取当前用户登录设备列表 | `SysUserOnlineController` |
| `/monitor/online/myself/{tokenId}` | DELETE | 删除当前用户的登录设备 | `SysUserOnlineController` |

### 17. 系统监控 - 操作日志

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/monitor/operlog/list` | GET | 查询操作日志列表 | `SysOperLogController` |
| `/monitor/operlog/{operId}` | DELETE | 删除操作日志 | `SysOperLogController` |
| `/monitor/operlog/clean` | DELETE | 清空操作日志 | `SysOperLogController` |

### 18. 系统监控 - 登录日志

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/monitor/logininfor/list` | GET | 查询登录日志列表 | `SysLogininforController` |
| `/monitor/logininfor/{infoId}` | DELETE | 删除登录日志 | `SysLogininforController` |
| `/monitor/logininfor/unlock/{userName}` | GET | 解锁用户登录状态 | `SysLogininforController` |
| `/monitor/logininfor/clean` | DELETE | 清空登录日志 | `SysLogininforController` |

### 19. 系统监控 - 缓存监控

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/monitor/cache` | GET | 获取 Redis 缓存监控信息 | `CacheController` |
| `/monitor/cache/getNames` | GET | 查询缓存名称列表 | `CacheController` |
| `/monitor/cache/getKeys/{cacheName}` | GET | 查询缓存键名列表 | `CacheController` |
| `/monitor/cache/getValue/{cacheName}/{cacheKey}` | GET | 查询缓存内容 | `CacheController` |
| `/monitor/cache/clearCacheName/{cacheName}` | DELETE | 清理指定名称缓存 | `CacheController` |
| `/monitor/cache/clearCacheKey/{cacheName}/{cacheKey}` | DELETE | 清理指定键名缓存 | `CacheController` |
| `/monitor/cache/clearCacheAll` | DELETE | 清理全部缓存 | `CacheController` |

### 20. 代码生成

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/tool/gen/list` | GET | 查询已导入的代码生成表列表 | `GenController` |
| `/tool/gen/db/list` | GET | 查询数据库表列表 | `GenController` |
| `/tool/gen/{tableId}` | GET | 获取表详情 | `GenController` |
| `/tool/gen` | PUT | 修改代码生成配置 | `GenController` |
| `/tool/gen/{tableId}` | DELETE | 删除代码生成表 | `GenController` |
| `/tool/gen/importTable` | POST | 导入表结构 | `GenController` |
| `/tool/gen/preview/{tableId}` | GET | 预览生成代码 | `GenController` |
| `/tool/gen/genCode/{tableId}` | GET | 生成代码（自定义路径） | `GenController` |
| `/tool/gen/synchDb/{tableId}` | GET | 同步数据库表结构 | `GenController` |
| `/tool/gen/getDataNames` | GET | 获取数据源名称列表 | `GenController` |

### 21. Magic API 动态接口

magic-api 提供可视化 API 编辑器，支持通过 Web 界面动态创建、编辑、管理 REST API。

| 路径 | HTTP 方法 | 用途 |
|------|-----------|------|
| `/magic/web` | GET | magic-api Web 编辑器入口 |
| `/magic-web/**` | 多种 | magic-api 编辑器资源文件 |
| `/api/**` | 可配置 | 动态 API 接口（路径前缀可配置） |

#### 特性

- **可视化编辑**：通过浏览器直接编写 SQL、脚本、HTTP 调用等
- **热部署**：保存即生效，无需重启服务
- **权限控制**：可配置接口访问权限
- **数据源**：支持多数据源切换
- **脚本语言**：支持 magic-script 脚本语法

#### 配置项（application.yml）

```yaml
magic-api:
  web: /magic/web          # Web编辑器路径
  resource:                # 接口存储配置
    type: database         # 存储到数据库
  prefix: /api             # 动态接口URL前缀
```

### 22. 其他接口

| API 路径 | HTTP 方法 | 用途 | 入口文件 |
|----------|-----------|------|----------|
| `/` | GET | 访问首页（返回欢迎信息） | `IndexController.java` |
| `/resource/sse/close` | GET | 关闭 SSE 推送连接 | - |

---

## 接口统计

| 模块 | 接口数量 |
|------|----------|
| 认证授权 | 8 |
| 验证码 | 2 |
| 用户管理 | 18 |
| 角色管理 | 14 |
| 菜单管理 | 10 |
| 部门管理 | 7 |
| 岗位管理 | 7 |
| 字典类型 | 7 |
| 字典数据 | 6 |
| 参数配置 | 8 |
| 通知公告 | 5 |
| 客户端管理 | 6 |
| 社交账号 | 1 |
| 租户管理 | 11 |
| 租户套餐 | 7 |
| OSS 对象管理 | 3 |
| OSS 配置管理 | 6 |
| 在线用户 | 4 |
| 操作日志 | 3 |
| 登录日志 | 4 |
| 缓存监控 | 7 |
| 代码生成 | 10 |
| Magic API | 动态 |
| 其他 | 2 |
| **合计** | **约 150+**（不含 Magic API 动态接口） |

---

## 注意事项

1. **分页参数**：列表接口统一使用 `pageNum`（页码）和 `pageSize`（每页条数）作为分页参数
2. **排序参数**：支持 `orderByColumn`（排序字段）和 `isAsc`（排序方向 asc/desc）
3. **日期格式**：请求和响应中的日期统一使用 `yyyy-MM-dd HH:mm:ss` 格式
4. **加密传输**：登录、注册、密码修改等敏感接口启用 `@ApiEncrypt`，请求体需加密传输
5. **限流保护**：验证码、租户列表等接口启用 `@RateLimiter` 限流注解
6. **租户隔离**：启用多租户后，大部分业务接口自动按 `tenantId` 进行数据隔离
7. **数据权限**：角色可配置数据权限（全部、本部门、本部门及以下、仅本人、自定义）
