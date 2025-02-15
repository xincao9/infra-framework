package fun.golinks.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ScheduleBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private final Scheduler scheduler;
    private Environment environment;

    public ScheduleBeanPostProcessor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = ClassUtils.getUserClass(bean.getClass());
        Method[] methods = clazz.getMethods();
        if (methods.length == 0) {
            return bean;
        }
        for (Method method : methods) {
            if (!method.isAnnotationPresent(Schedule.class)) {
                continue;
            }
            Schedule schedule = method.getAnnotation(Schedule.class);
            String cron = schedule.cron();
            if (StringUtils.isBlank(cron)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] != JobExecutionContext.class) {
                throw new SchedulerBeansException(
                        "The method annotated with @Schedule must have a parameter of type JobExecutionContext");
            }
            Job job = (Job) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class[] { Job.class },
                    (proxy, proxyMethod, args) -> {
                        if (parameterTypes.length == 0) {
                            return method.invoke(bean);
                        }
                        return method.invoke(bean, args);
                    });
            String name = schedule.name();
            if (StringUtils.isBlank(name)) {
                name = method.getName();
            }
            String group = schedule.group();
            if (StringUtils.isBlank(group)) {
                group = environment.getProperty("spring.application.name");
            }
            JobDetail jobDetail = JobBuilder.newJob(job.getClass()).withIdentity(name, group).build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name + "-trigger", group)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                throw new SchedulerBeansException("scheduler", e);
            }
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public static class SchedulerBeansException extends BeansException {

        public SchedulerBeansException(String msg) {
            super(msg);
        }

        public SchedulerBeansException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
