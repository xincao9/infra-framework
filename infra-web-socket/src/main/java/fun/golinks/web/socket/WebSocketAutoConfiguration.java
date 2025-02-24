package fun.golinks.web.socket;

import com.google.protobuf.Message;
import fun.golinks.web.socket.core.MessageHandler;
import fun.golinks.web.socket.properties.WebSocketProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@ConditionalOnProperty(prefix = "infra.web.socket", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketAutoConfiguration {

    @Bean
    public WebSocketServer webSocketServer(WebSocketProperties webSocketProperties,
            ObjectProvider<List<MessageHandler<Message>>> messageHandlersProvider) {
        return new WebSocketServer(webSocketProperties, messageHandlersProvider.getIfAvailable());
    }
}
