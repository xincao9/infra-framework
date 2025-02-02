package fun.golinks.config.git;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "infra.config", name = "type", havingValue = "git", matchIfMissing = true)
public class GitAutoConfiguration {

    @Bean
    public GitBeanPostProcessor gitBeanPostProcessor() {
        return new GitBeanPostProcessor();
    }
}
