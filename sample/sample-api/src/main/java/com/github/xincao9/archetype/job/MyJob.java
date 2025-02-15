package com.github.xincao9.archetype.job;

import fun.golinks.scheduler.JobCron;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 定时任务
 */
@Slf4j
@JobCron(cron = "0/1 * * * * ?")
public class MyJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("MyJob execute");
    }
}
