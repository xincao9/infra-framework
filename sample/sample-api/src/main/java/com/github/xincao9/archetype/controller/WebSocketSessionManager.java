package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.utils.JsonUtils;
import fun.golinks.core.consts.StatusEnums;
import fun.golinks.core.consts.SystemConsts;
import fun.golinks.core.properties.JwtProperties;
import fun.golinks.core.vo.R;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    @Resource
    private JwtProperties jwtProperties;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws IOException {
        String subject = getSubject(session);
        if (subject == null) {
            session.sendMessage(new TextMessage(JsonUtils.toJson(R.failed(StatusEnums.FORBIDDEN))));
            session.close(CloseStatus. NORMAL);
            return;
        }
        sessionMap.put(subject, session);
        log.info("用户 {} 已连接，当前在线用户数: {}", subject, sessionMap.size());
    }

    @Nullable
    private String getSubject(@NotNull WebSocketSession session) {
        HttpHeaders headers = session.getHandshakeHeaders();
        String authHeader = headers.getFirst(SystemConsts.AUTH_HEADER);
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7); // 移除 "Bearer " 前缀
        // 解析 JWT 令牌并获取其中的声明信息
        Claims claims = jwtProperties.parseToken(token);
        if (claims == null) {
            return null;
        }
        String subject = claims.getSubject();
        if (StringUtils.isBlank(subject)) {
            return null;
        }
        return subject;
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String subject = getSubject(session);
        if (subject == null) {
            return;
        }
        sessionMap.remove(subject);
        log.info("status {} 用户 {} 已断开，当前在线用户数: {}", status, subject, sessionMap.size());
    }

    public WebSocketSession getSessionBySubject(String subject) {
        return sessionMap.get(subject);
    }

}
