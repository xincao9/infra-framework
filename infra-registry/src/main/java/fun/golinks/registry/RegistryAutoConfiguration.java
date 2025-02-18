package fun.golinks.registry;

import com.alibaba.nacos.api.exception.NacosException;
import fun.golinks.registry.nacos.NacosConfig;
import fun.golinks.registry.nacos.NacosNamingService;
import fun.golinks.registry.nacos.NacosSmartLifecycle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "infra.registry", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RegistryProperties.class)
@ImportAutoConfiguration(RegistryAutoConfiguration.NacosConfiguration.class)
public class RegistryAutoConfiguration {

    @ConditionalOnProperty(prefix = "infra.registry", name = "type", havingValue = "nacos")
    public static class NacosConfiguration {

        private final String applicationName;
        private final Integer port;

        public NacosConfiguration(@Value("${spring.application.name}") String applicationName,
                @Value("${server.port}") Integer port) {
            this.applicationName = applicationName;
            this.port = port;
        }

        @Bean
        public NacosNamingService registryNacosNamingService(RegistryProperties registryProperties)
                throws NacosException {
            NacosConfig nacosConfig = registryProperties.getNacos();
            return NacosNamingService.newBuilder().setServerAddress(nacosConfig.getAddress())
                    .setUsername(nacosConfig.getUsername()).setPassword(nacosConfig.getPassword())
                    .setNamespace(nacosConfig.getNamespace()).build();
        }

        @Bean
        public NacosSmartLifecycle nacosSmartLifecycle(RegistryProperties registryProperties,
                NacosNamingService registryNacosNamingService) {
            String appName = registryProperties.getAppName();
            if (StringUtils.isBlank(appName)) {
                appName = applicationName;
            }
            Integer p = registryProperties.getPort();
            if (p == null || p <= 0) {
                p = port;
            }
            return new NacosSmartLifecycle(appName, p, registryNacosNamingService);
        }
    }
}
