package fun.golinks.web.socket.core;

import com.google.protobuf.InvalidProtocolBufferException;
import fun.golinks.web.socket.WebSocketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WebSocketFrameToProtoDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) {
        if (!(frame instanceof BinaryWebSocketFrame)) {
            out.add(frame);
            return;
        }
        ByteBuf content = frame.content().retain(); // 先保留引用
        try {
            byte[] bytes = ByteBufUtil.getBytes(content);
            WebSocketMessage message = WebSocketMessage.parseFrom(bytes);
            out.add(message);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse WebSocket message", e);
            ctx.fireExceptionCaught(e);
        } finally {
            content.release(); // 最后释放引用
        }
    }

}