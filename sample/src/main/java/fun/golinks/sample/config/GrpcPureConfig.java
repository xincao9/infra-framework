package fun.golinks.sample.config;

import fun.golinks.grpc.pure.GrpcChannels;
import grpc.fun.golinks.sample.GreeterRPCServiceGrpc;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcPureConfig {

    private static final String GREETER_APP_URL = "nacos://greeter";

    @Bean
    public GreeterRPCServiceGrpc.GreeterRPCServiceBlockingStub greeterRPCServiceBlockingStub(
            GrpcChannels grpcChannels) {
        ManagedChannel channel = grpcChannels.create(GREETER_APP_URL);
        return GreeterRPCServiceGrpc.newBlockingStub(channel);
    }
}
