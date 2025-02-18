package fun.golinks.registry;

import fun.golinks.registry.nacos.NacosConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * infra-registry配置属性
 */
@Data
@ConfigurationProperties(prefix = "infra.registry")
public class RegistryProperties {

    /**
     * 开关
     */
    private Boolean enabled = false;

    /**
     * 类型 (默认值：nacos)
     */
    private static final String DEFAULT_TYPE = "nacos";

    /**
     * 应用名
     */
    private String appName;

    /**
     * 监听端口
     */
    private Integer port;

    /**
     * 类型
     */
    private String type = DEFAULT_TYPE;

    /**
     * Nacos 配置
     */
    private NacosConfig nacos = new NacosConfig();

}