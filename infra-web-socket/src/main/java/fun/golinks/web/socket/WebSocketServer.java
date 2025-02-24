package fun.golinks.web.socket;

import fun.golinks.web.socket.handler.MessageRouterHandler;
import fun.golinks.web.socket.properties.WebSocketProperties;
import fun.golinks.web.socket.util.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class WebSocketServer implements SmartLifecycle {

    private final int port;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MessageRouterHandler messageRouterHandler;

    public WebSocketServer(WebSocketProperties webSocketProperties, MessageRouterHandler messageRouterHandler) {
        this.port = webSocketProperties.getServer().getPort();
        this.messageRouterHandler = messageRouterHandler;
        this.bossGroup = new NioEventLoopGroup(webSocketProperties.getServer().getBossThreads(),
                new NamedThreadFactory("web-socket-boss-"));
        this.workerGroup = new NioEventLoopGroup(webSocketProperties.getServer().getWorkerThreads(),
                new NamedThreadFactory("web-socket-worker-"));
    }

    @Override
    public void start() {
        try {
            if (!running.compareAndSet(false, true)) {
                return;
            }
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            // Protobuf 编解码器
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(WebSocketMessage.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(messageRouterHandler);
                        }
                    });
            serverBootstrap.bind(port).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.info("websocket启动成功，端口号: {}", port);
                } else {
                    log.error("websocket启动失败，端口号: {}", port, channelFuture.cause());
                    stop();
                }
            });
        } catch (Exception e) {
            log.error("WebSocket服务器启动过程中发生异常", e);
            stop();
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        log.info("开始关闭WebSocket服务器...");
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        log.info("WebSocket服务器已关闭");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}