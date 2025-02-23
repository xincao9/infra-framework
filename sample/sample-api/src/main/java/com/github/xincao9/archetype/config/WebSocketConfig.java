package com.github.xincao9.archetype.config;

import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

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
        config.enableSimpleBroker("/topic", "/queue");

        // 设置应用目的地的前缀为"/app"，用于标识消息将路由到带有@MessageMapping注解的方法
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
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
        registry.addEndpoint("/ws").addInterceptors(new HandshakeInterceptor() {

            @Override
            public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                    @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
                // 从请求中获取 userId 参数
                if (request instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                    String userId = servletRequest.getServletRequest().getParameter("userId");
                    if (userId != null) {
                        // 将 userId 保存到 WebSocket 会话属性中
                        attributes.put("userId", userId);
                    }
                }
                return true;
            }

            @Override
            public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                    @NonNull WebSocketHandler wsHandler, Exception exception) {

            }
        }).withSockJS();
    }

}