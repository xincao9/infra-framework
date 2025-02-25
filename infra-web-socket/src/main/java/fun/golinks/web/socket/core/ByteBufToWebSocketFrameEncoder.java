package fun.golinks.web.socket.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public class ByteBufToWebSocketFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf == null) {
            throw new IllegalArgumentException("byteBuf cannot be null");
        }

        ByteBuf retainedByteBuf = byteBuf.retain();
        try {
            WebSocketFrame frame = new BinaryWebSocketFrame(retainedByteBuf);
            out.add(frame);
        } finally {
            retainedByteBuf.release();
        }
    }

}
