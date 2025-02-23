package com.github.xincao9.archetype.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class WebSocketController {

    private final Map<String, WebSocketSession> userIdSessionMap = new ConcurrentHashMap<>();

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 广播消息
     */
    @MessageMapping("/chat/broadcast")
    @SendTo("/topic/messages")
    public String chatBroadcast(String message) {
        return "Broadcast Echo: " + message;
    }

    /**
     * 向调用的用户，自己发送消息
     */
    @MessageMapping("/chat/private")
    @SendToUser("/queue/messages")
    public String chatPrivate(String message) {
        return "ChatPrivate Echo: " + message;
    }

    /**
     * 向指定用户发送消息
     */
    @MessageMapping("/chat/toUser")
    @SendToUser("/queue/messages")
    public void chatToUser(String userId, String message) {
        WebSocketSession webSocketSession = userIdSessionMap.get(userId);
        if (webSocketSession == null) {
            log.warn("User {} not found in session map.", userId);
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage("ChatToUser Echo: " + message));
            log.info("Message sent to user {}: {}", userId, message);
        } catch (IOException e) {
            log.error("Failed to send message to user {}: {}", userId, message, e);
        } catch (IllegalStateException e) {
            log.error("Session is not open for user {}: {}", userId, message, e);
        }
    }

    @SuppressWarnings("ALL")
    @EventListener
    public void handleSessionConnectedEvent(SessionConnectedEvent event) {
        Object source = event.getSource();
        if (source instanceof WebSocketSession) {
            WebSocketSession session = (WebSocketSession) event.getSource();
            if (session == null) {
                return;
            }
            Object object = session.getAttributes().get("userId");
            if (object == null) {
                return;
            }
            String userId = String.valueOf(object);
            userIdSessionMap.put(userId, session);
        }
    }

    @SuppressWarnings("ALL")
    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        Object source = event.getSource();
        if (source instanceof WebSocketSession) {
            WebSocketSession session = (WebSocketSession) event.getSource();
            if (session == null) {
                return;
            }
            Object object = session.getAttributes().get("userId");
            if (object == null) {
                return;
            }
            String userId = String.valueOf(object);
            userIdSessionMap.remove(userId);
        }
    }
}