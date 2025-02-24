package fun.golinks.web.socket.config;

import fun.golinks.web.socket.WebSocketAutoConfiguration;
import fun.golinks.web.socket.handler.GreeterHandler;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;

@ImportAutoConfiguration(WebSocketAutoConfiguration.class)
public class SystemConfig {

    @Bean
    public GreeterHandler greeterHandler() {
        return new GreeterHandler();
    }
}
