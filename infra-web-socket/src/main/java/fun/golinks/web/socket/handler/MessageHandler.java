package fun.golinks.web.socket.handler;

import com.google.protobuf.Message;
import fun.golinks.web.socket.WebSocketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public abstract class MessageHandler<T extends Message> {

    public abstract int messageNo();

    public abstract Class<T> requestType();

    public abstract void handle(ChannelHandlerContext ctx, T message);

    protected BinaryWebSocketFrame toBinaryWebSocketFrame(WebSocketMessage webSocketMessage) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(webSocketMessage.toByteArray()).retain();
        return new BinaryWebSocketFrame(byteBuf);
    }
}