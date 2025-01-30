package fun.golinks.sample.invoker;

import fun.golinks.grpc.pure.util.GrpcFunction;
import fun.golinks.grpc.pure.util.GrpcInvoker;
import grpc.fun.golinks.sample.GreeterRPCServiceGrpc;
import grpc.fun.golinks.sample.GreeterSayRequest;
import grpc.fun.golinks.sample.GreeterSayResponse;
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
