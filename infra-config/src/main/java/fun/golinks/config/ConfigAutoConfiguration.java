package fun.golinks.config;

import fun.golinks.config.git.GitAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 配置中心自动配置类
 */
@ImportAutoConfiguration(GitAutoConfiguration.class)
@ConditionalOnProperty(prefix = "infra.config", name = "enabled", havingValue = "true")
public class ConfigAutoConfiguration {

    @Bean
    public ContextUtils contextUtils() {
        return new ContextUtils();
    }
}
