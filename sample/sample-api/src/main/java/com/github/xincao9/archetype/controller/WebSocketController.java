package com.github.xincao9.archetype.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/chat") // Handles messages sent to /app/chat
    @SendTo("/topic/messages") // Sends the return value to /topic/messages
    public String chat(String message) {
        return "Echo: " + message;
    }
}