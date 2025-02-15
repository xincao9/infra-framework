package fun.golinks.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerAutoConfiguration {

    public ScheduleBeanPostProcessor scheduleBeanPostProcessor(Scheduler scheduler) {
        return new ScheduleBeanPostProcessor(scheduler);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }
}
