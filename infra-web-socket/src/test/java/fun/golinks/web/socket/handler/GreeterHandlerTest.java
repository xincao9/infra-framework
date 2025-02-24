package fun.golinks.web.socket.handler;

import fun.golinks.web.socket.GreeterRequest;
import fun.golinks.web.socket.MessageNoEnums;
import fun.golinks.web.socket.WebSocketMessage;
import fun.golinks.web.socket.config.SystemConfig;
import fun.golinks.web.socket.core.WebSocketFrameHandler;
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
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootTest(classes = SystemConfig.class)
public class GreeterHandlerTest {

    @BeforeEach
    public void setup() throws Exception {
    }

    @Test
    public void testHandle() throws Throwable {
        EventLoopGroup group = new NioEventLoopGroup();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            URI uri = new URI("ws://localhost:8888/ws");
            WebSocketClientHandshaker webSocketClientHandshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketClientProtocolHandler(webSocketClientHandshaker));
                            pipeline.addLast(new WebSocketFrameHandler());
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(WebSocketMessage.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(new Log(countDownLatch));
                        }
                    });
            Channel channel = bootstrap.connect("localhost", 8888).sync().channel();
            webSocketClientHandshaker.handshake(channel).sync();
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
                } else {
                    log.error("发送消息失败", future.cause());
                }
            });
        } finally {
            group.shutdownGracefully();
        }
        countDownLatch.await();
    }

    public static class Log extends SimpleChannelInboundHandler<WebSocketMessage> {

        private final CountDownLatch countDownLatch;

        public Log(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketMessage msg) throws Exception {
            log.info("no: {}, payload: {}", msg.getNo(), msg.getPayload());
            countDownLatch.countDown();
        }
    }
}
