package fun.golinks.config;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 配置中心自动配置类
 */
@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class ConfigAutoConfiguration {

    /**
     * 创建git 远程同步runner
     *
     * @return git 远程同步runner
     * 
     * @throws GitAPIException
     *             git api 异常
     * @throws IOException
     *             io 异常
     */
    @Bean
    public GitSyncRunner gitSyncRunner() throws Throwable {
        return new GitSyncRunner();
    }
}
