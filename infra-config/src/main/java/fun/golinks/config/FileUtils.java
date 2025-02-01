package fun.golinks.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
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

    public static Map<Object, Object> readConfigFiles(String directory) {
        Map<Object, Object> configMap = new HashMap<>();
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String filename = path.toString();
                if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
                    configMap.putAll(readYamlFile(filename));
                } else if (filename.endsWith(".properties")) {
                    configMap.putAll(readPropertiesFile(filename));
                }
            });
        } catch (IOException e) {
            log.error("Files.walk directory: {}", directory, e);
        }
        return configMap;
    }

    public static Map<Object, Object> readYamlFile(String filepath) {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        Resource resource = new FileSystemResource(filepath);
        yamlFactory.setResources(resource);
        Properties properties = yamlFactory.getObject();
        if (properties != null) {
            return new HashMap<>(properties);
        }
        return Collections.emptyMap();
    }

    public static Map<Object, Object> readPropertiesFile(String filepath) {
        Resource resource = new FileSystemResource(filepath);
        try {
            Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            return new HashMap<>(properties);
        } catch (IOException e) {
            log.error("loadProperties filepath: {}", filepath, e);
        }
        return Collections.emptyMap();
    }
}