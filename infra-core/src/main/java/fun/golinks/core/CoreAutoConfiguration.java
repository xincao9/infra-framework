package fun.golinks.core;

import fun.golinks.core.config.WebConfig;
import fun.golinks.core.interceptor.WebHandlerInterceptor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration(CoreAutoConfiguration.WebConfigImporter.class)
public class CoreAutoConfiguration {

    @Bean
    public WebHandlerInterceptor webHandlerInterceptor() {
        return new WebHandlerInterceptor();
    }

    @ConditionalOnWebApplication
    @ImportAutoConfiguration(WebConfig.class)
    public static class WebConfigImporter {
    }
}
