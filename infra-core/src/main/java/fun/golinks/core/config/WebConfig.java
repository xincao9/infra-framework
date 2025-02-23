package fun.golinks.core.config;

import fun.golinks.core.interceptor.WebHandlerInterceptor;
import fun.golinks.core.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * web配置类
 */
@EnableConfigurationProperties(JwtProperties.class)
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 添加HandlerInterceptor拦截器
     *
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WebHandlerInterceptor(jwtProperties));
    }
}
