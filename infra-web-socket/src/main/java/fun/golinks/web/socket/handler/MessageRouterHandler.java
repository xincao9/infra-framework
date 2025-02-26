package fun.golinks.web.socket.handler;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import fun.golinks.web.socket.WebSocketMessage;
import fun.golinks.web.socket.exception.WebSocketException;
import fun.golinks.web.socket.util.Pair;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SuppressWarnings("ALL")
@ChannelHandler.Sharable
public class MessageRouterHandler extends SimpleChannelInboundHandler<WebSocketMessage> {

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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketMessage webSocketMessage) throws Exception {
        int no = webSocketMessage.getNo();
        byte[] payload = webSocketMessage.getPayload().toByteArray();
        Pair<Parser<Message>, MessageHandler<Message>> pair = routes.get(no);
        if (pair == null) {
            throw new WebSocketException(String.format("没有找到消息编号{%d}对应的处理器！", no));
        }
        Parser<Message> parser = pair.getO1();
        MessageHandler<Message> messageHandler = pair.getO2();
        Message message = parser.parseFrom(payload);
        messageHandler.handle(ctx, message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }
}