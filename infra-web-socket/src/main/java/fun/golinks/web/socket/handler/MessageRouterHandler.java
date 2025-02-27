package fun.golinks.web.socket.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import fun.golinks.web.socket.WebSocketMessage;
import fun.golinks.web.socket.exception.WebSocketException;
import fun.golinks.web.socket.util.Pair;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SuppressWarnings("ALL")
@ChannelHandler.Sharable
public class MessageRouterHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final Map<Integer, Pair<Parser<Message>, MessageHandler<Message>>> routes = new ConcurrentHashMap<>();

    public void addHandler(MessageHandler<Message> messageHandler) {
        Class<Message> messageClass = messageHandler.requestType();
        int no = messageHandler.messageNo();
        if (messageClass == null) {
            return;
        }
        Parser<Message> parser = null;
        try {
            Method parserMethod = messageClass.getMethod("parser");
            parser = (Parser<Message>) parserMethod.invoke(null);
        } catch (Throwable e) {
            throw new WebSocketException(e);
        }
        if (routes.containsKey(no)) {
            return;
        }
        routes.put(no, new Pair<>(parser, messageHandler));
    }

    private WebSocketMessage toProto(WebSocketFrame frame) throws InvalidProtocolBufferException {
        if (!(frame instanceof BinaryWebSocketFrame)) {
            return null;
        }
        ByteBuf content = frame.content(); // 先保留引用
        try {
            byte[] bytes = ByteBufUtil.getBytes(content);
            return WebSocketMessage.parseFrom(bytes);
        } finally {
            content.release(); // 最后释放引用
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) {
        WebSocketMessage webSocketMessage = null;
        try {
            webSocketMessage = toProto(webSocketFrame);
        } catch (InvalidProtocolBufferException e) {
            log.error("WebSocketFrame to Proto failure：", e);
            // TODO 可以选择发送（处理WebSocket消息时发生错误）错误响应给客户端
        }
        if (webSocketMessage == null) {
            return;
        }
        try {
            int no = webSocketMessage.getNo();
            byte[] payload = webSocketMessage.getPayload().toByteArray();
            // 输入验证
            if (no <= 0 || payload == null || payload.length == 0) {
                throw new WebSocketException("无效的消息编号或负载！");
            }
            Pair<Parser<Message>, MessageHandler<Message>> pair = routes.get(no);
            if (pair == null) {
                log.error("没有找到消息编号{}对应的处理器！", no);
                throw new WebSocketException("没有找到消息编号对应的处理器！");
            }
            Parser<Message> parser = pair.getO1();
            MessageHandler<Message> messageHandler = pair.getO2();
            try {
                Message message = parser.parseFrom(payload);
                messageHandler.handle(ctx, message);
            } catch (Exception e) {
                log.error("处理消息时发生错误：", e);
                // TODO 可以选择发送（服务器内部错误）错误响应给客户端
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息时发生错误：", e);
            // TODO 可以选择发送（处理WebSocket消息时发生错误）错误响应给客户端
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

}