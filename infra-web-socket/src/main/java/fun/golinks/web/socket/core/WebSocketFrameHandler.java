package fun.golinks.web.socket.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof BinaryWebSocketFrame) {
            ByteBuf content = frame.content();
            // 传递给后续的 Protobuf 解码器
            ctx.fireChannelRead(content);
        } else {
            throw new UnsupportedOperationException("不支持帧类型: " + frame.getClass().getName());
        }
    }
}