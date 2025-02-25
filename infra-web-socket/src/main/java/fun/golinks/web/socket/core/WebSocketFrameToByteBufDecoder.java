package fun.golinks.web.socket.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public class WebSocketFrameToByteBufDecoder extends MessageToMessageDecoder<WebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame == null) {
            throw new NullPointerException("WebSocketFrame cannot be null");
        }
        ByteBuf buf = frame.content();
        if (buf == null) {
            throw new NullPointerException("ByteBuf content cannot be null");
        }
        out.add(buf);
        buf.retain();
    }

}