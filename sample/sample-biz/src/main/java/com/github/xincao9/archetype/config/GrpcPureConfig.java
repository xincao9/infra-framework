package com.github.xincao9.archetype.config;

import com.github.xincao9.infra.archetype.GreeterRPCServiceGrpc;
import fun.golinks.grpc.pure.GrpcChannels;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * grpc客户端配置类
 */
@Configuration
public class GrpcPureConfig {

    /**
     * nacos注册中心请使用 nacos://{应用名} 格式定义target
     */
    private static final String GREETER_APP_URL = "nacos://";
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public GreeterRPCServiceGrpc.GreeterRPCServiceBlockingStub greeterRPCServiceBlockingStub(
            GrpcChannels grpcChannels) {
        ManagedChannel channel = grpcChannels.create(GREETER_APP_URL + applicationName);
        return GreeterRPCServiceGrpc.newBlockingStub(channel);
    }
}
