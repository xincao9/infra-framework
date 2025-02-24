package fun.golinks.web.socket.handler;

import fun.golinks.web.socket.GreeterRequest;
import fun.golinks.web.socket.MessageNoEnums;
import fun.golinks.web.socket.WebSocketMessage;
import fun.golinks.web.socket.config.SystemConfig;
import fun.golinks.web.socket.core.WebSocketClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;

@Slf4j
@SpringBootTest(classes = SystemConfig.class)
public class GreeterHandlerTest {

    private Channel channel;

    @BeforeEach
    public void setup() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        URI uri = new URI("ws://localhost:8888/ws");
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(new WebSocketClientHandler(
                                WebSocketClientHandshakerFactory.newHandshaker(
                                        uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())
                        ));
                        pipeline.addLast(new ProtobufDecoder(WebSocketMessage.getDefaultInstance()));
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new Logger());
                    }
                });
        this.channel = bootstrap.connect("localhost", 8888).sync().channel();
        handshaker.handshake(this.channel).sync();
    }

    @Test
    public void testHandle() throws Throwable {
        GreeterRequest greeterRequest = GreeterRequest.newBuilder()
                .setName("xincao")
                .build();
        WebSocketMessage webSocketMessage = WebSocketMessage.newBuilder()
                .setNo(MessageNoEnums.GREETER_REQUEST_VALUE)
                .setPayload(greeterRequest.toByteString())
                .build();
        ChannelFuture channelFuture = this.channel.writeAndFlush(webSocketMessage);
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("发送消息成功");
                Thread.sleep(5000);
            } else {
                log.error("发送消息失败", future.cause());
            }
        });
    }

    public static class Logger extends SimpleChannelInboundHandler<WebSocketMessage> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketMessage msg) throws Exception {
            log.info("no: {}, payload: {}", msg.getNo(), msg.getPayload());
        }
    }
}
