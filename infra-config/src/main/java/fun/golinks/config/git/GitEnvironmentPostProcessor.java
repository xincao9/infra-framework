package fun.golinks.config.git;

import com.google.gson.Gson;
import fun.golinks.config.ConfigConsts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GitEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final Gson gson = new Gson();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        GitSyncRunner gitSyncRunner;
        try {
            // 创建git 远程同步runner
            gitSyncRunner = GitSyncRunner.start();
        } catch (Throwable e) {
            log.error("GitSyncRunner.new", e);
            System.exit(1);
            return;
        }
        Map<String, String> configEnv = FileUtils.readConfig();
        if (configEnv.isEmpty()) {
            return;
        }
        boolean enabled = Boolean.parseBoolean(configEnv.get(ConfigConsts.INFRA_CONFIG_ENABLED));
        if (!enabled) {
            return;
        }
        String type = configEnv.get(ConfigConsts.INFRA_CONFIG_TYPE);
        if (!Objects.equals(type, GitConsts.GIT)) {
            return;
        }
        String uri = configEnv.get(GitConsts.INFRA_CONFIG_GIT_URI);
        String appName = configEnv.get(ConfigConsts.INFRA_CONFIG_APP_NAME);
        if (StringUtils.isAnyBlank(uri, appName)) {
            return;
        }
        String home = System.getenv("HOME");
        String dir = configEnv.getOrDefault(GitConsts.INFRA_CONFIG_GIT_DIR, Paths.get(home, ".config").toString());
        String repo = StringUtils.substringAfterLast(uri, "/");
        repo = StringUtils.substringBefore(repo, ".git");
        Path path = Paths.get(Objects.requireNonNull(dir), appName, repo);
        Map<String, Object> configItems = FileUtils.readConfig(path.toString());
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        if (!configItems.isEmpty()) {
            configItems.putAll(configEnv);
            log.info("加载本地配置 {}", gson.toJson(configItems));
            mutablePropertySources.addFirst(new MapPropertySource(GitConsts.GIT_CONFIG, configItems));
        }
        gitSyncRunner.add(() -> {
            Map<String, Object> configItems1 = FileUtils.readConfig(path.toString());
            mutablePropertySources.remove(GitConsts.GIT_CONFIG);
            if (!configItems1.isEmpty()) {
                configItems1.putAll(configEnv);
                log.info("同步本地配置 {}", gson.toJson(configItems1));
                mutablePropertySources.addFirst(new MapPropertySource(GitConsts.GIT_CONFIG, configItems1));
            }
        });

    }
}
