package fun.golinks.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;

public class SchedulerAutoConfiguration {

    @Bean
    public ScheduleBeanPostProcessor scheduleBeanPostProcessor(Scheduler scheduler) {
        return new ScheduleBeanPostProcessor(scheduler);
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }
}
