package com.github.xincao9.archetype.config;

import com.github.xincao9.archetype.job.MyJob;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class QuartzConfig implements BeanClassLoaderAware {

    private ClassLoader classLoader;

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

    /**
     * 初始化quartz使用
     *
     */
    @QuartzDataSource
    @Bean
    public DataSource myDataSource(QuartzProperties quartzProperties) {
        Map<String, String> properties = quartzProperties.getProperties();
        String prefix = "org.quartz.dataSource";
        String ds = properties.get("org.quartz.jobStore.dataSource");
        return DataSourceBuilder.create(classLoader).type(HikariDataSource.class)
                .driverClassName(properties.get(String.format("%s.%s.driver", prefix, ds)))
                .url(properties.get(String.format("%s.%s.URL", prefix, ds)))
                .username(properties.get(String.format("%s.%s.user", prefix, ds)))
                .password(properties.get(String.format("%s.%s.password", prefix, ds))).build();
    }

    @Override
    public void setBeanClassLoader(@NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
