package fun.golinks.core;

import fun.golinks.core.config.HandlerMethodReturnValueHandlerConfig;
import fun.golinks.core.config.WebConfig;
import fun.golinks.core.feign.FeginResourceBeanPostProcessor;
import fun.golinks.core.feign.FeignProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@ImportAutoConfiguration(CoreAutoConfiguration.WebConfigImporter.class)
public class CoreAutoConfiguration {

    @Bean
    public FeignProxy feignProxy() {
        return new FeignProxy();
    }

    @Bean
    public FeginResourceBeanPostProcessor feginResourceBeanPostProcessor(FeignProxy feignProxy) {
        return new FeginResourceBeanPostProcessor(feignProxy);
    }

    @ConditionalOnWebApplication
    @ImportAutoConfiguration({ WebConfig.class, HandlerMethodReturnValueHandlerConfig.class })
    public static class WebConfigImporter {
    }
}
