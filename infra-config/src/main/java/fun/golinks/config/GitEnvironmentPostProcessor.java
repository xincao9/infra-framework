package fun.golinks.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class GitEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, String> configEnv = FileUtils.readConfig();
        if (configEnv.isEmpty()) {
            return;
        }
        boolean enabled = Boolean.parseBoolean(configEnv.get(ConfigConsts.INFRA_CONFIG_ENABLED));
        if (!enabled) {
            return;
        }
        String type = configEnv.get(ConfigConsts.INFRA_CONFIG_TYPE);
        if (!Objects.equals(type, ConfigConsts.GIT)) {
            return;
        }
        String uri = configEnv.get(ConfigConsts.INFRA_CONFIG_GIT_URI);
        String appName = configEnv.get(ConfigConsts.INFRA_CONFIG_APP_NAME);
        if (StringUtils.isAnyBlank(uri, appName)) {
            return;
        }
        String home = System.getenv("HOME");
        String dir = configEnv.getOrDefault(ConfigConsts.INFRA_CONFIG_GIT_DIR, Paths.get(home, "./config").toString());
        String repo = StringUtils.substringAfterLast(uri, "/");
        repo = StringUtils.substringBefore(repo, ".git");
        Path path = Paths.get(Objects.requireNonNull(dir), appName, repo);
        Map<String, Object> configItems = FileUtils.readConfig(path.toString());
        if (!configItems.isEmpty()) {
            configItems.putAll(configEnv);
            environment.getPropertySources().addFirst(new MapPropertySource(ConfigConsts.GIT_CONFIG, configItems));
        }

    }
}
