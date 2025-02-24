package com.github.xincao9.archetype.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
    public String chatBroadcast(@Payload String message) {
        return "ChatBroadcast Echo: " + message;
    }

    /**
     * 向调用的用户，自己发送消息
     */
    @MessageMapping("/chat/private")
    @SendToUser("/queue/messages")
    public String chatPrivate(@Payload String message) {
        return "ChatPrivate Echo: " + message;
    }

    /**
     * 向指定用户发送消息
     */
    @MessageMapping("/chat/toUser/{subject}")
    @SendToUser("/queue/messages")
    public void chatToUser(@Payload String message, @DestinationVariable("subject") String subject) {
        WebSocketSession webSocketSession = webSocketSessionManager.getSessionBySubject(subject);
        if (webSocketSession == null) {
            log.warn("User {} not found in session map.", subject);
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage("ChatToUser Echo: " + message));
            log.info("Message sent to user {}: {}", subject, message);
        } catch (IOException e) {
            log.error("Failed to send message to user {}: {}", subject, message, e);
        } catch (IllegalStateException e) {
            log.error("Session is not open for user {}: {}", subject, message, e);
        }
    }

}