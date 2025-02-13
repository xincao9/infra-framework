package fun.golinks.config.git;

import fun.golinks.config.ConfigConsts;
import fun.golinks.config.ContextUtils;
import fun.golinks.config.FileUtils;
import fun.golinks.config.Pair;
import fun.golinks.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GitEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final ConversionService conversionService = new DefaultConversionService();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, String> configEnv = FileUtils.readConfig();
        Path path = tryGetPath(configEnv);
        if (path == null) {
            return;
        }
        GitSyncRunner gitSyncRunner;
        try {
            // 创建git 远程同步runner
            gitSyncRunner = GitSyncRunner.start();
        } catch (Throwable e) {
            log.error("GitSyncRunner.new", e);
            System.exit(1);
            return;
        }
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        update(path, configEnv, mutablePropertySources);
        gitSyncRunner.add(() -> update(path, configEnv, mutablePropertySources));
    }

    private void update(Path path, Map<String, String> configEnv, MutablePropertySources mutablePropertySources) {
        Map<String, Object> configItems = FileUtils.readConfig(path.toString());
        mutablePropertySources.remove(GitConsts.GIT_CONFIG);
        configItems.putAll(configEnv);
        log.info("Load local configuration {}", JsonUtils.toJsonString(configItems));
        mutablePropertySources.addFirst(new MapPropertySource(GitConsts.GIT_CONFIG, configItems));
        refresh(configItems);
    }

    public void refresh(Map<String, Object> configItems) {
        if (ContextUtils.AC == null) { // 加载时AC为NULL，之后修改时非NULL
            return;
        }
        /*
         * 刷新@Value标记的属性
         */
        List<Pair<String, Pair<Object, Field>>> valueAnnotationRuntime = ContextUtils.AC
                .getBean(GitBeanPostProcessor.class).getValueAnnotationRuntime();
        for (Pair<String, Pair<Object, Field>> pair : valueAnnotationRuntime) {
            Object value = configItems.get(pair.getO1());
            if (value == null) {
                continue;
            }
            Object bean = pair.getO2().getO1();
            Field field = pair.getO2().getO2();
            field.setAccessible(true);
            try {
                value = conversionService.convert(value, field.getType());
                field.set(bean, value);
            } catch (IllegalAccessException e) {
                log.error("refresh @Value({})", pair.getO1(), e);
            }
        }
        /*
         * 刷新@ConfigurationProperties标记的配置属性类
         */
        ConfigurationPropertiesBindingPostProcessor postProcessor = ContextUtils.AC
                .getBean(ConfigurationPropertiesBindingPostProcessor.class);
        Map<String, Object> beans = ContextUtils.AC.getBeansWithAnnotation(ConfigurationProperties.class);
        beans.forEach((beanName, bean) -> {
            Class<?> clazz = ClassUtils.getUserClass(bean);
            ConfigurationProperties configurationProperties = clazz.getAnnotation(ConfigurationProperties.class);
            if (configurationProperties == null) {
                return;
            }
            String prefix = configurationProperties.prefix();
            if (StringUtils.isBlank(prefix)) {
                prefix = configurationProperties.value();
            }
            if (StringUtils.isBlank(prefix)) {
                postProcessor.postProcessBeforeInitialization(bean, beanName);
                return;
            }
            for (Map.Entry<String, Object> entry : configItems.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.startsWith(key, prefix)) {
                    postProcessor.postProcessBeforeInitialization(bean, beanName);
                    return;
                }
            }
        });
        /*
         * git配置同步事件触发
         */
        try {
            ApplicationEventPublisher applicationEventPublisher = ContextUtils.AC
                    .getBean(ApplicationEventPublisher.class);
            applicationEventPublisher.publishEvent(new GitSyncApplicationEvent(this, configItems));
        } catch (NoSuchBeanDefinitionException e) {
            log.debug("applicationEventPublisher.publishEvent(GitSyncApplicationEvent)", e);
        }
    }

    private Path tryGetPath(Map<String, String> configEnv) {
        if (configEnv.isEmpty()) {
            return null;
        }
        // enabled
        boolean enabled = Boolean.parseBoolean(configEnv.get(ConfigConsts.INFRA_CONFIG_ENABLED));
        if (!enabled) {
            return null;
        }
        // type
        String type = configEnv.get(ConfigConsts.INFRA_CONFIG_TYPE);
        if (!Objects.equals(type, GitConsts.GIT)) {
            return null;
        }
        // uri and appName
        String uri = configEnv.get(GitConsts.INFRA_CONFIG_GIT_URI);
        String appName = configEnv.get(ConfigConsts.INFRA_CONFIG_APP_NAME);
        if (StringUtils.isAnyBlank(uri, appName)) {
            return null;
        }
        // dir
        String home = System.getenv("HOME");
        String dir = configEnv.getOrDefault(GitConsts.INFRA_CONFIG_GIT_DIR, Paths.get(home, ".config").toString());

        // repo
        String repo = StringUtils.substringAfterLast(uri, "/");
        repo = StringUtils.substringBefore(repo, ".git");
        return Paths.get(Objects.requireNonNull(dir), appName, repo);
    }
}
