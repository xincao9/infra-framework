package fun.golinks.scheduler;

import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnBean(Scheduler.class)
@Configuration
public class SchedulerAutoConfiguration {

    @Bean
    public ScheduleBeanPostProcessor scheduleBeanPostProcessor(Scheduler scheduler) {
        return new ScheduleBeanPostProcessor(scheduler);
    }
}
