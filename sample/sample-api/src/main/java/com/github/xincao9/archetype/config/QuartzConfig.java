package com.github.xincao9.archetype.config;

import com.github.xincao9.archetype.job.MyJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail myJobDetail() {
        return JobBuilder.newJob(MyJob.class).withIdentity("myJob", "default").storeDurably().build();
    }

    @Bean
    public Trigger myTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0/5 * * * * ?");
        return TriggerBuilder.newTrigger().forJob(myJobDetail()).withIdentity("myTrigger", "default")
                .withSchedule(scheduleBuilder).build();
    }
}
