package com.github.xincao9.archetype.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
public class WebSocketController {

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
    public void chatToUser(String userId, String message) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", "ChatToUser Echo: " + message);
    }
}