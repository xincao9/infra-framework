package fun.golinks.web.socket;

import fun.golinks.web.socket.handler.MessageHandler;
import fun.golinks.web.socket.handler.MessageRouterHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URI;

@Slf4j
@SuppressWarnings("ALL")
public class WebSocketTest {

    @Test
    public void testHandle() throws Exception {
        GreeterResponseHandler greeterResponseHandler = new GreeterResponseHandler();
        MessageHandler messageHandler = greeterResponseHandler;
        MessageRouterHandler messageRouterHandler = new MessageRouterHandler();
        messageRouterHandler.addHandler(messageHandler);
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            URI uri = new URI("ws://localhost:8888/ws"); // 考虑从配置文件读取
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketClientProtocolHandler(handshaker));
                            pipeline.addLast(new MessageRouterHandler());
                        }
                    });
            Channel channel = bootstrap.connect("localhost", 8888).sync().channel();
            GreeterRequest greeterRequest = GreeterRequest.newBuilder()
                    .setName("xincao")
                    .build();
            WebSocketMessage webSocketMessage = WebSocketMessage.newBuilder()
                    .setNo(MessageNoEnums.GREETER_REQUEST_VALUE)
                    .setPayload(greeterRequest.toByteString())
                    .build();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(webSocketMessage.toByteArray());
            ChannelFuture channelFuture = channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("发送消息成功");
                    // 更好的方式是等待服务器响应而不是固定睡眠时间
                    channel.closeFuture().sync();
                } else {
                    log.error("发送消息失败", future.cause());
                }
            });
        } finally {
            group.shutdownGracefully();
        }
    }


}
