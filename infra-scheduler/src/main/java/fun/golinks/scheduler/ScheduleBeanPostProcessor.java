package fun.golinks.scheduler;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

@Slf4j
public class ScheduleBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private final Scheduler scheduler;
    private Environment environment;

    public ScheduleBeanPostProcessor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, @NonNull String beanName) throws BeansException {
        Class<?> clazz = ClassUtils.getUserClass(bean.getClass());
        if (clazz.isAnnotationPresent(JobCron.class) && Job.class.isAssignableFrom(clazz)) {
            JobCron jobCron = clazz.getAnnotation(JobCron.class);
            String cron = jobCron.cron();
            if (StringUtils.isBlank(cron)) {
                return bean;
            }
            String name = jobCron.name();
            if (StringUtils.isBlank(name)) {
                name = beanName;
            }
            String group = jobCron.group();
            if (StringUtils.isBlank(group)) {
                group = environment.getProperty("spring.application.name");
            }
            @SuppressWarnings("unchecked")
            Class<? extends Job> jobClass = (Class<? extends Job>) clazz;
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(name, group).build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name + "-trigger", group).forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
            try {
                scheduler.scheduleJob(trigger);
            } catch (SchedulerException e) {
                throw new SchedulerBeansException("scheduler", e);
            }
        }
        return bean;
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    public static class SchedulerBeansException extends BeansException {

        public SchedulerBeansException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
