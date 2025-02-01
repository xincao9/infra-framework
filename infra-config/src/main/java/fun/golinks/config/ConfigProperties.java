package fun.golinks.config;

import fun.golinks.config.git.GitConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置属性类
 */
@Data
@ConfigurationProperties("infra.config")
public class ConfigProperties {

    /**
     * 功能开关
     */
    private Boolean enabled = false;

    /**
     * type is git or apollo
     */
    private String type = "git";

    /**
     * git 配置
     */
    private GitConfig git = new GitConfig();

}
