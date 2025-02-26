package fun.golinks.web.socket.core;

import com.google.protobuf.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

public class ProtoToWebSocketFrameEncoder extends MessageToMessageEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, List<Object> out) throws Exception {
        if (obj instanceof Message) {
            try {
                Message message = (Message) obj;
                out.add(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(message.toByteArray())));
            } catch (Throwable e) {
                ctx.fireExceptionCaught(e);
            }
        } else {
            out.add(obj); // 将非Message类的对象直接向下传递
        }
    }

}