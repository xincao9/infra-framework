package fun.golinks.web.socket.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

public class BinaryWebSocketFrameToByteBufDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame, List<Object> out) {
        // Retain the content to prevent premature release
        out.add(frame.content().retain());
    }
}