package fun.golinks.trace.rpc.grpc;

import brave.Tracing;
import fun.golinks.trace.rpc.grpc.brave.GrpcTracing;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * grpc配置类
 */
public class GrpcAutoConfiguration {

    @Bean
    public GrpcTracing grpcTracing(Tracing tracing) {
        return GrpcTracing.create(tracing);
    }

    @Bean
    public ClientInterceptor grpcTracingClientInterceptor(GrpcTracing grpcTracing) {
        return grpcTracing.newClientInterceptor();
    }

    @Bean
    public ServerInterceptor grpcTracingServerInterceptor(GrpcTracing grpcTracing) {
        return grpcTracing.newServerInterceptor();
    }
}
