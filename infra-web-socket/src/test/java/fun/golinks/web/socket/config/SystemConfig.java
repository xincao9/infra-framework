package fun.golinks.web.socket.config;

import fun.golinks.web.socket.handler.GreeterHandler;
import org.springframework.context.annotation.Bean;

public class SystemConfig {

    @Bean
    public GreeterHandler greeterHandler() {
        return new GreeterHandler();
    }
}
