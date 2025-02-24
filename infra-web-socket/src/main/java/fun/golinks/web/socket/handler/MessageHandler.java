package fun.golinks.web.socket.handler;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;

public interface MessageHandler<T extends Message> {

    int messageNo();

    Class<T> requestType();

    void handle(ChannelHandlerContext ctx, T message);
}