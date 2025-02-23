package com.github.xincao9.archetype.controller;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketControllerTest {

    @Value("${local.server.port}")
    private int port; // 随机端口

    private StompSession stompSession;
    private BlockingQueue<String> messageQueue;

    @BeforeEach
    public void setup() throws Exception {
        // 初始化 WebSocketStompClient
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))
        ));
        stompClient.setMessageConverter(new StringMessageConverter());

        // 初始化消息队列
        messageQueue = new LinkedBlockingQueue<>();

        // 连接到 WebSocket 服务器
        String url = "ws://localhost:" + port + "/ws";
        stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {
        }).get(1, TimeUnit.SECONDS);

        // 订阅消息
        stompSession.subscribe("/topic/messages", new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                messageQueue.add((String) payload);
            }
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (stompSession != null) {
            stompSession.disconnect();
        }
    }

    @Test
    public void testChat() throws Exception {
        // 发送消息
        String message = "Hello, WebSocket!";
        stompSession.send("/app/chat", message);
        log.info("send message：{}",message);
        // 等待并验证接收到的消息
        String receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
        log.info("received-message: {}", receivedMessage);
        Assertions.assertEquals("Echo: " + message, receivedMessage);
    }
}