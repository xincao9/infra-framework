package fun.golinks.web.socket.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocketFrameRouter 根据 WebSocket 帧类型路由处理逻辑
 */
@Slf4j
public class WebSocketFrameRouter extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof BinaryWebSocketFrame) {
            // 处理二进制帧（Protobuf），直接传递给后续 Handler
            log.debug("Received BinaryWebSocketFrame, length: {}", frame.content().readableBytes());
            ctx.fireChannelRead(frame.retain()); // 保留帧并传递给下一个 Handler
        } else if (frame instanceof TextWebSocketFrame) {
            // 处理文本帧（例如调试信息）
            String text = ((TextWebSocketFrame) frame).text();
            log.info("Received TextWebSocketFrame: {}", text);
            // 这里可以添加自定义逻辑，例如回复消息
            ctx.channel().writeAndFlush(new TextWebSocketFrame("Echo: " + text));
        } else if (frame instanceof PingWebSocketFrame) {
            // 响应 Ping 帧
            log.debug("Received PingWebSocketFrame");
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof PongWebSocketFrame) {
            // 处理 Pong 帧
            log.debug("Received PongWebSocketFrame");
            // 可以选择忽略或记录
        } else if (frame instanceof CloseWebSocketFrame) {
            // 处理关闭帧
            CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
            log.info("Received CloseWebSocketFrame, status: {}, reason: {}", closeFrame.statusCode(),
                    closeFrame.reasonText());
            ctx.close();
        } else {
            // 未识别的帧类型
            log.warn("Unsupported WebSocket frame type: {}", frame.getClass().getName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("WebSocketFrameRouter error", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 连接建立时
        log.info("WebSocket connection activated");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 连接断开时
        log.info("WebSocket connection inactivated");
        super.channelInactive(ctx);
    }
}