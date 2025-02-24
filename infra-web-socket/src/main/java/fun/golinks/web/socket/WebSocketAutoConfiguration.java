package fun.golinks.web.socket;

import com.google.protobuf.Message;
import fun.golinks.web.socket.core.MessageHandler;
import fun.golinks.web.socket.core.MessageRouterHandler;
import fun.golinks.web.socket.properties.WebSocketProperties;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "infra.web.socket", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketAutoConfiguration {

    @Bean
    public BeanPostProcessor beanPostProcessor(MessageRouterHandler messageRouterHandler) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
                    throws BeansException {
                if (bean instanceof MessageHandler) {
                    @SuppressWarnings("unchecked")
                    MessageHandler<Message> messageHandler = (MessageHandler<Message>) bean;
                    messageRouterHandler.addHandler(messageHandler);
                }
                return bean;
            }
        };
    }

    @Bean
    public MessageRouterHandler messageRouterHandler() {
        return new MessageRouterHandler();
    }

    @Bean
    public WebSocketServer webSocketServer(WebSocketProperties webSocketProperties,
            MessageRouterHandler messageRouterHandler) {
        return new WebSocketServer(webSocketProperties, messageRouterHandler);
    }
}
