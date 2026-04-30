package org.fellow99.magic.ruoyi.config;

import org.dromara.common.tenant.properties.TenantProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 无条件注册 {@link TenantProperties} bean。
 *
 * <p>RuoYi 上游 {@code TenantConfig} 通过
 * {@code @ConditionalOnProperty(name = "tenant.enable", havingValue = "true")}
 * 守护 {@link TenantProperties} 的注册，但其内部类
 * {@code TenantConfig$MybatisPlusConfiguration#tenantLineInnerInterceptor}
 * 仅由 {@code @ConditionalOnClass(TenantLineInnerInterceptor.class)} 守护，
 * 无条件依赖 {@link TenantProperties} bean。
 *
 * <p>当 {@code tenant.enable=false} 时上游配置存在 bean 缺失 bug：
 * 拦截器方法仍尝试注入 {@link TenantProperties}，但该 bean 未被注册，
 * 导致应用启动失败。
 *
 * <p>本类无条件注册 {@link TenantProperties}（默认 enable=null，excludes=null），
 * 让上游拦截器能拿到一个空 properties，从而绕过启动失败。
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class TenantPropertiesRegistration {
}
