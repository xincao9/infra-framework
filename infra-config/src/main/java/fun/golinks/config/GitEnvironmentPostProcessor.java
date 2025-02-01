package fun.golinks.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GitEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<Object, Object> configMap = FileUtils.readYamlFile(new ClassPathResource(ConfigConsts.CONFIG_FILE));
        if (configMap.isEmpty()) {
            return;
        }
        Map<String, Object> configEnv = new HashMap<>(configMap.size());
        for (Map.Entry<Object, Object> entry : configMap.entrySet()) {
            configEnv.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        boolean enabled = Boolean.parseBoolean(String.valueOf(configEnv.get(ConfigConsts.INFRA_CONFIG_ENABLED)));
        if (!enabled) {
            return;
        }
        String type = String.valueOf(configEnv.get(ConfigConsts.INFRA_CONFIG_TYPE));
        if (!Objects.equals(type, ConfigConsts.GIT)) {
            return;
        }
        String uri = String.valueOf(configEnv.get(ConfigConsts.INFRA_CONFIG_GIT_URI));
        String dir = String.valueOf(configEnv.get(ConfigConsts.INFRA_CONFIG_GIT_DIR));
        String appName = String.valueOf(configEnv.get(ConfigConsts.INFRA_CONFIG_APP_NAME));
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
