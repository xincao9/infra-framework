package com.github.xincao9.archetype.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理，设置消息代理的前缀和应用目的地的前缀。 该方法用于配置消息代理，启用一个简单的内存消息代理，并设置应用目的地的前缀。
     *
     * @param config
     *            MessageBrokerRegistry对象，用于配置消息代理的相关设置。
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用一个简单的内存消息代理，并指定消息代理的前缀为"/topic"
        config.enableSimpleBroker("/topic");

        // 设置应用目的地的前缀为"/app"，用于标识消息将路由到带有@MessageMapping注解的方法
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 注册STOMP端点，用于WebSocket连接。 该方法重写了父类的方法，用于配置STOMP端点。
     *
     * @param registry
     *            STOMP端点注册器，用于注册WebSocket端点。
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册"/ws"端点，并启用SockJS支持，以便在不支持WebSocket的浏览器中回退到其他协议。
        registry.addEndpoint("/ws").withSockJS();
    }

}