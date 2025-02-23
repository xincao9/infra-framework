package com.github.xincao9.archetype.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Controller
public class WebSocketController {

    @Resource
    private WebSocketSessionManager webSocketSessionManager;

    /**
     * 广播消息
     */
    @MessageMapping("/chat/broadcast")
    @SendTo("/topic/messages")
    public String chatBroadcast(String message) {
        return "ChatBroadcast Echo: " + message;
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
    public void chatToUser(String message, MessageHeaders headers) {
        String userId = String.valueOf(headers.get("userId"));
        WebSocketSession webSocketSession = webSocketSessionManager.getSessionByUserId(userId);
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

}