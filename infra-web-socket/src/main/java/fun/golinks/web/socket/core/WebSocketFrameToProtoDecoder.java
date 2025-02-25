package fun.golinks.web.socket.core;

import com.google.protobuf.InvalidProtocolBufferException;
import fun.golinks.web.socket.WebSocketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public class WebSocketFrameToProtoDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) {
        if (frame instanceof BinaryWebSocketFrame) {
            ByteBuf content = frame.content();
            try {
                WebSocketMessage message = WebSocketMessage.parseFrom(ByteBufUtil.getBytes(content));
                out.add(message);
            } catch (InvalidProtocolBufferException e) {
                ctx.fireExceptionCaught(e);
            }
        } else {
            out.add(frame);
        }
    }
}