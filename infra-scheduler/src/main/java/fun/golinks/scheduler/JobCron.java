package fun.golinks.scheduler;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Component
public @interface JobCron {

    /**
     * cron 表达式
     *
     * @return
     */
    String cron();

    /**
     * 名字
     *
     * @return
     */
    String name() default "";

    /**
     * 组名
     *
     * @return
     */
    String group() default "";
}
