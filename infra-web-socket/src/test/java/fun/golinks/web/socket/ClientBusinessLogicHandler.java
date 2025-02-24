package fun.golinks.web.socket;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientBusinessLogicHandler extends SimpleChannelInboundHandler<WebSocketMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        GreeterRequest greeterRequest = GreeterRequest.newBuilder()
                .setName("xincao")
                .build();
        WebSocketMessage webSocketMessage = WebSocketMessage.newBuilder()
                .setNo(MessageNoEnums.GREETER_REQUEST_VALUE)
                .setPayload(greeterRequest.toByteString())
                .build();
        ChannelFuture channelFuture = ctx.writeAndFlush(webSocketMessage);
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("发送消息成功");
                Thread.sleep(5000);
            } else {
                log.error("发送消息失败", future.cause());
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketMessage msg) {
        log.info("no: {}, payload: {}", msg.getNo(), msg.getPayload());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}