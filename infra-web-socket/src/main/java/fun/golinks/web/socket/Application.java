package fun.golinks.web.socket;

import fun.golinks.web.socket.handler.GreeterHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public GreeterHandler greeterHandler() {
        return new GreeterHandler();
    }

}
