package fun.golinks.archetype.rpc;

import com.github.xincao9.infra.archetype.GreeterRPCServiceGrpc;
import com.github.xincao9.infra.archetype.GreeterSayRequest;
import com.github.xincao9.infra.archetype.GreeterSayResponse;
import fun.golinks.grpc.pure.util.GrpcConsumer;
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
