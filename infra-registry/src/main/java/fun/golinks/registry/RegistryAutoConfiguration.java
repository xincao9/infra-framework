package fun.golinks.registry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(RegistryProperties.class)
public class RegistryAutoConfiguration {

    @ConditionalOnProperty(prefix = "infra.registry.discovery", name = "type", havingValue = "nacos")
    @Bean
    public NacosSmartLifecycle nacosSmartLifecycle(RegistryProperties registryProperties) throws Throwable {
        return new NacosSmartLifecycle(registryProperties);
    }
}
