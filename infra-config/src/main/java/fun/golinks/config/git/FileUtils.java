package fun.golinks.config.git;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Slf4j
public class FileUtils {

    public static Map<String, Object> readConfig(String directory) {
        Map<Object, Object> configMap = new HashMap<>();
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String filename = path.toString();
                if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
                    configMap.putAll(readYaml(filename));
                } else if (filename.endsWith(".properties")) {
                    configMap.putAll(readProperties(filename));
                }
            });
        } catch (IOException e) {
            log.error("Files.walk directory: {}", directory, e);
        }
        if (configMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> config = new HashMap<>(configMap.size());
        configMap.forEach((k, v) -> config.put(String.valueOf(k), v));
        return config;
    }

    public static Map<Object, Object> readYaml(String filepath) {
        Resource resource = new FileSystemResource(filepath);
        return readYaml(resource);
    }

    public static Map<Object, Object> readYaml(Resource resource) {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(resource);
        Properties properties = yamlFactory.getObject();
        if (properties != null) {
            return new HashMap<>(properties);
        }
        return Collections.emptyMap();
    }

    public static Map<Object, Object> readProperties(String filepath) {
        Resource resource = new FileSystemResource(filepath);
        try {
            Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            return new HashMap<>(properties);
        } catch (IOException e) {
            log.error("loadProperties filepath: {}", filepath, e);
        }
        return Collections.emptyMap();
    }

    public static Map<String, String> readConfig() {
        Map<Object, Object> configMap = FileUtils.readYaml(new ClassPathResource(GitConsts.CONFIG_FILE));
        if (configMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> configEnv = new HashMap<>(configMap.size());
        configMap.forEach((k, v) -> configEnv.put(String.valueOf(k), String.valueOf(v)));
        return configEnv;
    }
}