package fun.golinks.sample.rpc;

import fun.golinks.grpc.pure.util.GrpcConsumer;
import grpc.fun.golinks.sample.GreeterRPCServiceGrpc;
import grpc.fun.golinks.sample.GreeterSayRequest;
import grpc.fun.golinks.sample.GreeterSayResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class GreeterRPCServiceImpl extends GreeterRPCServiceGrpc.GreeterRPCServiceImplBase {

    private static final GrpcConsumer<GreeterSayRequest, GreeterSayResponse> grpcConsumer = GrpcConsumer
            .wrap(greeterSayRequest -> GreeterSayResponse.newBuilder()
                    .setMessage(String.format("Hello %s", greeterSayRequest.getName())).build());

    @Override
    public void say(GreeterSayRequest request, StreamObserver<GreeterSayResponse> responseObserver) {
        grpcConsumer.accept(request, responseObserver);
    }
}
