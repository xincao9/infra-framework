package com.github.xincao9.archetype.controller;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return;
        }
        String query = uri.getQuery();
        if (StringUtils.isBlank(query)) {
            return;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0].trim(), pair[1].trim());
            } else {
                result.put(pair[0].trim(), "");
            }
        }
        String userId = result.get("userId");
        if (StringUtils.isNotBlank(userId)) {
            sessionMap.put(userId, session);
            System.out.println("用户 " + userId + " 已连接，当前在线用户数: " + sessionMap.size());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = getUserIdBySession(session);
        if (StringUtils.isNotBlank(userId)) {
            sessionMap.remove(userId);
            System.out.println("用户 " + userId + " 已断开，当前在线用户数: " + sessionMap.size());
        }
    }

    public WebSocketSession getSessionByUserId(String userId) {
        return sessionMap.get(userId);
    }

    private String getUserIdBySession(WebSocketSession session) {
        return sessionMap.entrySet().stream().filter(entry -> entry.getValue().equals(session)).map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }
}
