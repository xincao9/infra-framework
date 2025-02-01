package com.github.xincao9.archetype.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 信息
 */
@Data
@Component
@ConfigurationProperties("info")
public class InfoProperties {

    /**
     * 应用名
     */
    private String applicationName;
    /**
     * 版本
     */
    private String version;
    /**
     * 作者
     */
    private String creater;

}
