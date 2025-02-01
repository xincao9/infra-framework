package fun.golinks.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;

@Slf4j
public class EnvironmentChangeListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final ConfigurationPropertiesBindingPostProcessor postProcessor;

    public EnvironmentChangeListener(ConfigurationPropertiesBindingPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        log.info("ApplicationEnvironmentPreparedEvent = {}", event);
    }
}
