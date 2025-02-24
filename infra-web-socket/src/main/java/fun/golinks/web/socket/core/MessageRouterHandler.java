package fun.golinks.web.socket.core;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import fun.golinks.web.socket.WebSocketMessage;
import fun.golinks.web.socket.exception.WebSocketException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ALL")
public class MessageRouterHandler extends SimpleChannelInboundHandler<WebSocketMessage> {

    private final Map<Integer, Pair<Parser<Message>, MessageHandler<Message>>> routes = new ConcurrentHashMap<>();

    public MessageRouterHandler(List<MessageHandler<Message>> messageHandlers) {
        if (CollectionUtils.isEmpty(messageHandlers)) {
            return;
        }
        for (MessageHandler<Message> messageHandler : messageHandlers) {
            Class<Message> messageClass = messageHandler.requestType();
            int no = messageHandler.messageNo();
            if (messageClass == null) {
                continue;
            }
            Parser<Message> parser = null;
            try {
                Method parserMethod = messageClass.getMethod("parser");
                parser = (Parser<Message>) parserMethod.invoke(null);
            } catch (Throwable e) {
                throw new WebSocketException(e);
            }
            if (routes.containsKey(no)) {
                continue;
            }
            routes.put(no, new Pair<>(parser, messageHandler));
        }
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
}