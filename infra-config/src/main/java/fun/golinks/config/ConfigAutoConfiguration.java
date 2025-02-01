package fun.golinks.config;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 配置中心自动配置类
 */
@ConditionalOnProperty(prefix = "infra.config", name = "enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class ConfigAutoConfiguration {

    /**
     * 创建git 远程同步runner
     *
     * @param configProperties
     *            配置属性类
     *
     * @return git 远程同步runner
     *
     * @throws GitAPIException
     *             git api 异常
     * @throws IOException
     *             io 异常
     */
    @ConditionalOnProperty(prefix = "infra.config", name = "type", havingValue = "git", matchIfMissing = true)
    @Bean
    public GitSyncRunner gitSyncRunner(ConfigProperties configProperties,
            @Value("${spring.application.name}") String application) throws Throwable {
        return new GitSyncRunner(configProperties.getGit(), application);
    }
}
