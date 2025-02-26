package fun.golinks.web.socket.core;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProtoToWebSocketFrameEncoder extends MessageToMessageEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, List<Object> out) throws Exception {
        if (obj instanceof Message) {
            ByteBuf byteBuf = null;
            try {
                Message message = (Message) obj;
                byteBuf = Unpooled.wrappedBuffer(message.toByteArray());
                out.add(new BinaryWebSocketFrame(byteBuf));
                log.debug("Encoded message with ByteBuf refCnt: {}", byteBuf.refCnt());
            } catch (Throwable e) {
                ctx.fireExceptionCaught(e);
            }
        } else {
            out.add(obj); // 将非Message类的对象直接向下传递
        }
    }

}