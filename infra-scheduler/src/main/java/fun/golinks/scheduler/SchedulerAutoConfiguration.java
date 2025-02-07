package fun.golinks.scheduler;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SchedulerProperties.class)
public class SchedulerAutoConfiguration {
}
