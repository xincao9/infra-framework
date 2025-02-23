package com.github.xincao9.archetype.controller;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketControllerTest {

    @LocalServerPort
    private int port;

    @Test
    public void testWebSocketConnection() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        SampleSocketHandler handler = new SampleSocketHandler();
        URI uri = new URI(String.format("ws://127.0.0.1:%d/app/chat", port));
        ListenableFuture<WebSocketSession> listenableFuture = client.doHandshake(handler, null, uri);
        listenableFuture.addCallback(
                session -> {
                    log.info("connection success!");
                },
                throwable -> {
                    log.info("connection failed!");
                }
        );
        WebSocketSession session = listenableFuture.get();
        session.sendMessage(new TextMessage("Hello Server"));
        int read = System.in.read();
        log.info("{}", read);
        session.close();
    }

    @Slf4j
    private static class SampleSocketHandler extends TextWebSocketHandler {

        @Override
        public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
            log.info("{}", message.getPayload());
        }

        @Override
        public void afterConnectionEstablished(@NonNull WebSocketSession session) {
            log.info("afterConnectionEstablished");
        }

        @Override
        public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
            log.info("afterConnectionClosed");
        }
    }
}
