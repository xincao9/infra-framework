package com.github.xincao9.archetype.rpc.impl;

import com.github.xincao9.archetype.rpc.consumer.GreeterConsumer;
import com.github.xincao9.infra.archetype.GreeterRPCServiceGrpc;
import com.github.xincao9.infra.archetype.GreeterSayRequest;
import com.github.xincao9.infra.archetype.GreeterSayResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 演示，grpc服务实现方式
 */
@Service
public class GreeterRPCServiceImpl extends GreeterRPCServiceGrpc.GreeterRPCServiceImplBase {

    @Resource
    private GreeterConsumer greeterConsumer;

    @Override
    public void say(GreeterSayRequest request, StreamObserver<GreeterSayResponse> responseObserver) {
        greeterConsumer.sayConsumer.accept(request, responseObserver);
    }
}
