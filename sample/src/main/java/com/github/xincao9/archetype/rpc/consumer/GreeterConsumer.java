package com.github.xincao9.archetype.rpc.consumer;

import com.github.xincao9.infra.archetype.GreeterSayRequest;
import com.github.xincao9.infra.archetype.GreeterSayResponse;
import fun.golinks.grpc.pure.util.GrpcConsumer;
import org.springframework.stereotype.Component;

@Component
public class GreeterConsumer {

    public final GrpcConsumer<GreeterSayRequest, GreeterSayResponse> sayConsumer = GrpcConsumer
            .wrap(greeterSayRequest -> GreeterSayResponse.newBuilder()
                    .setMessage(String.format("Hello %s", greeterSayRequest.getName())).build());

}
