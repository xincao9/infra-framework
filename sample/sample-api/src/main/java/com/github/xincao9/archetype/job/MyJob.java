package com.github.xincao9.archetype.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 */
@Slf4j
@Component
public class MyJob implements Job {

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("{}: MyJob execute", applicationName);
    }
}
