package fun.golinks.web.socket;

import fun.golinks.web.socket.handler.MessageRouterHandler;
import fun.golinks.web.socket.properties.WebSocketProperties;
import fun.golinks.web.socket.util.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
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
                            pipeline.addLast(messageRouterHandler);
                        }
                    });

            ChannelFuture bindFuture = serverBootstrap.bind(port);
            bindFuture.addListener((ChannelFutureListener) channelFuture -> {
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
        try {
            // 关闭资源以防止泄漏
            if (bossGroup != null && !bossGroup.isShutdown()) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null && !workerGroup.isShutdown()) {
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("停止WebSocket服务器时发生异常", e);
        } finally {
            running.set(false); // 确保状态重置
            log.info("WebSocket服务器已关闭");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}