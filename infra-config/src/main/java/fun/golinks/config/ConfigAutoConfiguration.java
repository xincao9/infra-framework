package fun.golinks.config;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "infra.config", name = "enable", havingValue = "true")
@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class ConfigAutoConfiguration {

    @ConditionalOnProperty(prefix = "infra.config", name = "type", havingValue = "git", matchIfMissing = true)
    @Bean
    public GitRunner gitRunner(ConfigProperties configProperties) throws GitAPIException {
        return new GitRunner(configProperties.getGit());
    }
}
