package fun.golinks.archetype.config;

import com.github.microwww.redis.RedisServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class RedisServerConfig {

    @Bean
    public RedisServer mockRedisServer(@Value("${spring.redis.port}") int port) throws IOException {
        String host = "0.0.0.0";
        RedisServer server = new RedisServer();
        server.listener(host, port); // Redis runs in the background
        log.info("Redis start :: [{}:{}]", host, port);
        return server;
    }
}
