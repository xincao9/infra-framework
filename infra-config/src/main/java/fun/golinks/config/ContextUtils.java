package fun.golinks.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ContextUtils implements ApplicationContextAware {

    public static ApplicationContext AC;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AC = applicationContext;
    }
}
