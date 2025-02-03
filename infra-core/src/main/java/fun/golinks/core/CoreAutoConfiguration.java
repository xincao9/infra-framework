package fun.golinks.core;

import fun.golinks.core.interceptor.WebHandlerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreAutoConfiguration {

    @ConditionalOnWebApplication
    @Bean
    public WebHandlerInterceptor webHandlerInterceptor() {
        return new WebHandlerInterceptor();
    }
}
