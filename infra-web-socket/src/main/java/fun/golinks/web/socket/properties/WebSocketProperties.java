package fun.golinks.web.socket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infra.web.socket")
public class WebSocketProperties {

    private ServerProperties server = new ServerProperties();

    @Data
    public static class ServerProperties {

        private Integer port = 8888;
    }
}
