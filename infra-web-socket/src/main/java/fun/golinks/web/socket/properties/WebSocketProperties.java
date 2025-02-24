package fun.golinks.web.socket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infra.web.socket")
public class WebSocketProperties {

    private int port = 8888;
}
