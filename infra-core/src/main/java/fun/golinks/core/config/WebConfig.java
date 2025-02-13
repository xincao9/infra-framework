package fun.golinks.core.config;

import fun.golinks.core.interceptor.WebHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web配置类
 */
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加HandlerInterceptor拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WebHandlerInterceptor());
    }
}
