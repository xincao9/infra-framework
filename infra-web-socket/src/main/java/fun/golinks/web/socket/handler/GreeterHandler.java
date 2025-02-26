package fun.golinks.web.socket.handler;

import fun.golinks.web.socket.GreeterRequest;
import fun.golinks.web.socket.GreeterResponse;
import fun.golinks.web.socket.MessageNoEnums;
import fun.golinks.web.socket.WebSocketMessage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeterHandler implements MessageHandler<GreeterRequest> {

    @Override
    public int messageNo() {
        return MessageNoEnums.GREETER_REQUEST_VALUE;
    }

    @Override
    public Class<GreeterRequest> requestType() {
        return GreeterRequest.class;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, GreeterRequest greeterRequest) {
        WebSocketMessage webSocketMessage = WebSocketMessage.newBuilder().setNo(MessageNoEnums.GREETER_RESPONSE_VALUE)
                .setPayload(GreeterResponse.newBuilder().setMessage("hello " + greeterRequest.getName()).build()
                        .toByteString())
                .build();
        // 返回响应示例
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(webSocketMessage);
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                log.info("发送消息成功");
            } else {
                log.error("发送消息失败", future.cause());
            }
        });

    }
}