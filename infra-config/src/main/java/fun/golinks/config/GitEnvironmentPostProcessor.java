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

public class GitEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final GitConfig gitConfig;
    private final String applicationName;

    public GitEnvironmentPostProcessor(GitConfig gitConfig, String applicationName) {
        this.gitConfig = gitConfig;
        this.applicationName = applicationName;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String repo = StringUtils.substringAfterLast(gitConfig.getUri(), "/");
        repo = StringUtils.substringBefore(repo, ".git");
        Path path = Paths.get(gitConfig.getDir(), applicationName, repo);
        Map<Object, Object> map = FileUtils.readConfigFiles(path.toString());
        if (!map.isEmpty()) {
            Map<String, Object> env = new HashMap<>();
            map.forEach((k, v) -> env.put(String.valueOf(k), v));
            environment.getPropertySources().addFirst(new MapPropertySource("git-config", env));
        }

    }
}
