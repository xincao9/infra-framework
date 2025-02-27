package fun.golinks.web.socket;

import fun.golinks.web.socket.handler.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeterResponseHandler extends MessageHandler<GreeterResponse> {

    @Override
    public int messageNo() {
        return MessageNoEnums.GREETER_RESPONSE_VALUE;
    }

    @Override
    public Class<GreeterResponse> requestType() {
        return GreeterResponse.class;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, GreeterResponse message) {
        log.info("message = {}", message.getMessage());
    }
}
