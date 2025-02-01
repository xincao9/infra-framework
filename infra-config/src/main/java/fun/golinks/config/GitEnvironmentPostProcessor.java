package fun.golinks.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GitEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = Boolean.TRUE
                .equals(environment.getProperty(ConfigConsts.INFRA_CONFIG_ENABLED, Boolean.class));
        if (!enabled) {
            return;
        }
        String type = environment.getProperty(ConfigConsts.INFRA_CONFIG_TYPE, String.class);
        if (!Objects.equals(type, ConfigConsts.GIT)) {
            return;
        }
        String uri = environment.getProperty(ConfigConsts.INFRA_CONFIG_GIT_URI, String.class);
        String dir = environment.getProperty(ConfigConsts.INFRA_CONFIG_GIT_DIR, String.class);
        String appName = environment.getProperty(ConfigConsts.SPRING_APPLICATION_NAME, String.class);
        if (StringUtils.isAnyBlank(uri, dir, appName)) {
            return;
        }
        String repo = StringUtils.substringAfterLast(uri, "/");
        repo = StringUtils.substringBefore(repo, ".git");
        Path path = Paths.get(Objects.requireNonNull(dir), appName, repo);
        Map<Object, Object> map = FileUtils.readConfigFiles(path.toString());
        if (!map.isEmpty()) {
            Map<String, Object> env = new HashMap<>();
            map.forEach((k, v) -> env.put(String.valueOf(k), v));
            environment.getPropertySources().addFirst(new MapPropertySource(ConfigConsts.GIT_CONFIG, env));
        }

    }
}
