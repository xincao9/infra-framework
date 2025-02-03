package fun.golinks.core;

import fun.golinks.core.config.WebConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration(CoreAutoConfiguration.WebConfigImporter.class)
public class CoreAutoConfiguration {

    @ConditionalOnWebApplication
    @ImportAutoConfiguration(WebConfig.class)
    public static class WebConfigImporter {
    }
}
