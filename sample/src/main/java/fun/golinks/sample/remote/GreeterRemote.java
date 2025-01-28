package fun.golinks.sample.remote;

import fun.golinks.grpc.pure.util.GrpcConsumer;
import fun.golinks.sample.GreeterGrpc;
import fun.golinks.sample.HelloReply;
import fun.golinks.sample.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class GreeterRemote extends GreeterGrpc.GreeterImplBase {

    private static final String GREETING_PREFIX = "Server:Hello ";

    private static final GrpcConsumer<HelloRequest, HelloReply> grpcConsumer = GrpcConsumer
            .wrap(helloRequest -> buildHelloReply(helloRequest.getName()));

    private static HelloReply buildHelloReply(String name) {
        return HelloReply.newBuilder().setMessage(GREETING_PREFIX + name).build();
    }

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        grpcConsumer.accept(req, responseObserver);
    }
}
