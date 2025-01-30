package fun.golinks.sample.invoker;

import fun.golinks.grpc.pure.util.GrpcFunction;
import fun.golinks.grpc.pure.util.GrpcInvoker;
import fun.golinks.sample.rpc.GreeterRPCServiceGrpc;
import fun.golinks.sample.rpc.GreeterSayRequest;
import fun.golinks.sample.rpc.GreeterSayResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class GreeterInvoker {

    @Resource
    private GreeterRPCServiceGrpc.GreeterRPCServiceBlockingStub greeterRPCServiceBlockingStub;

    public final GrpcInvoker<GreeterSayRequest, GreeterSayResponse> SAY_INVOKER = GrpcInvoker
            .wrap(new GrpcFunction<GreeterSayRequest, GreeterSayResponse>() {
                @Override
                public GreeterSayResponse apply(GreeterSayRequest greeterSayRequest) throws Throwable {
                    return greeterRPCServiceBlockingStub.say(greeterSayRequest);
                }
            });
}
