package org.fellow99.magic.ruoyi.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.context.MagicUser;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.core.exception.MagicLoginException;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Magic-API 与 Sa-Token 集成配置：用 Sa-Token 接管 magic-api 编辑器登录，
 * 并将响应包装为 RuoYi 的 R&lt;T&gt;（code/msg/data）结构。
 * magic-editor jar 已在 pom 中排除，前端通过 npm @fellow99/magic-editor 嵌入 SPA。
 */
@Configuration
public class MagicApiConfig {

    private static final String ADMIN_ROLE = "admin";
    private static final String PERM_EDIT = "tool:magic-api:edit";
    private static final String PERM_DEBUG = "tool:magic-api:debug";

    @Bean
    public AuthorizationInterceptor magicApiAuthorizationInterceptor() {
        return new AuthorizationInterceptor() {

            @Override
            public boolean requireLogin() {
                return true;
            }

            @Override
            public MagicUser getUserByToken(String token) throws MagicLoginException {
                try {
                    if (!StpUtil.isLogin()) {
                        throw new MagicLoginException("未登录");
                    }
                    Object loginId = StpUtil.getLoginId();
                    String username = String.valueOf(loginId);
                    return new MagicUser(username, username, StpUtil.getTokenValue());
                } catch (NotLoginException e) {
                    throw new MagicLoginException("登录已失效，请重新登录");
                }
            }

            @Override
            public MagicUser login(String username, String password) throws MagicLoginException {
                throw new MagicLoginException("请通过主系统登录后再使用 Magic-API 编辑器");
            }

            @Override
            public void logout(String token) {
            }

            @Override
            public boolean allowVisit(MagicUser magicUser, MagicHttpServletRequest request, Authorization authorization) {
                if (!StpUtil.isLogin()) {
                    return false;
                }
                if (authorization == null) {
                    return true;
                }
                switch (authorization) {
                    case SAVE:
                    case DELETE:
                        return StpUtil.hasRole(ADMIN_ROLE) || StpUtil.hasPermission(PERM_EDIT);
                    case DOWNLOAD:
                    case UPLOAD:
                    case PUSH:
                    case LOCK:
                    case UNLOCK:
                    case RELOAD:
                    case VIEW:
                    case NONE:
                    default:
                        return true;
                }
            }

            @Override
            public boolean allowVisit(MagicUser magicUser, MagicHttpServletRequest request, Authorization authorization, MagicEntity entity) {
                return allowVisit(magicUser, request, authorization);
            }
        };
    }

    @Bean
    public RequestInterceptor magicApiRequestInterceptor() {
        return new RequestInterceptor() {
        };
    }

    /**
     * 将 magic-api 内部 code（1=成功）映射为 RuoYi R&lt;T&gt; 的 code=200。
     */
    @Bean
    public ResultProvider magicApiResultProvider() {
        return (RequestEntity requestEntity, int code, String message, Object data) -> {
            Map<String, Object> result = new LinkedHashMap<>(4);
            result.put("code", code == 1 ? 200 : code);
            result.put("msg", message);
            result.put("data", data);
            return result;
        };
    }
}
