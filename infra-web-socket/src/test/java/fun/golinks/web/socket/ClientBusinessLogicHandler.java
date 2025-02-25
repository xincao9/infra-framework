package fun.golinks.web.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientBusinessLogicHandler extends SimpleChannelInboundHandler<WebSocketMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketMessage msg) {
        log.info("no: {}, payload: {}", msg.getNo(), msg.getPayload());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }
}