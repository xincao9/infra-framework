package fun.golinks.config.git;

import com.google.gson.Gson;
import fun.golinks.config.ConfigConsts;
import fun.golinks.config.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.ClassUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GitEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final Gson gson = new Gson();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, String> configEnv = FileUtils.readConfig();
        Path path = getPath(configEnv);
        if (path == null)
            return;
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
        if (!configItems.isEmpty()) {
            configItems.putAll(configEnv);
            log.info("加载本地配置 {}", gson.toJson(configItems));
            mutablePropertySources.addFirst(new MapPropertySource(GitConsts.GIT_CONFIG, configItems));
            refresh(configItems);
        }
    }

    /**
     * TODO 配置变更可以刷新，ConfigurationProperties类，目前，重新加载所有的Bean，需要修改为只刷新匹配前缀的Bean
     */
    public void refresh(Map<String, Object> configItems) {
        if (ContextUtils.AC == null) {
            return;
        }
        ConfigurationPropertiesBindingPostProcessor postProcessor = ContextUtils.AC
                .getBean(ConfigurationPropertiesBindingPostProcessor.class);
        Map<String, Object> beans = ContextUtils.AC.getBeansWithAnnotation(ConfigurationProperties.class);
        beans.forEach((beanName, bean) -> {
            Class<?> clazz = ClassUtils.getUserClass(bean);
            ConfigurationProperties configurationProperties = clazz.getAnnotation(ConfigurationProperties.class);
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
                    break;
                }
            }
        });
    }

    private Path getPath(Map<String, String> configEnv) {
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
