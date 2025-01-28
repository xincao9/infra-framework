package fun.golinks.trace;

import brave.Tracing;
import brave.sampler.Sampler;
import fun.golinks.trace.http.servlet.ServletAutoConfiguration;
import fun.golinks.trace.rpc.grpc.GrpcAutoConfiguration;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@ConditionalOnProperty(prefix = "infra.trace", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(TraceProperties.class)
@ImportAutoConfiguration({ TraceAutoConfiguration.ServletAutoConfigurationImporter.class,
        TraceAutoConfiguration.GrpcAutoConfigurationImporter.class })
public class TraceAutoConfiguration {

    @Resource
    private TraceProperties traceProperties;

    @Bean
    public AsyncZipkinSpanHandler asyncZipkinSpanHandler(TraceProperties traceProperties) {
        return AsyncZipkinSpanHandler.create(URLConnectionSender.create(traceProperties.getZipkin().getUrl()));
    }

    @Bean
    public Tracing tracing(@Value("${spring.application.name}") String applicationName,
            AsyncZipkinSpanHandler asyncZipkinSpanHandler, TraceProperties traceProperties) {
        return Tracing.newBuilder().localServiceName(applicationName).addSpanHandler(asyncZipkinSpanHandler)
                .sampler(Sampler.create(traceProperties.getZipkin().getSampler())).build();
    }

    @ConditionalOnWebApplication
    @ImportAutoConfiguration(ServletAutoConfiguration.class)
    static class ServletAutoConfigurationImporter {
    }

    @ConditionalOnClass(name = { "io.grpc.ClientInterceptor", "io.grpc.ServerInterceptor" })
    @ImportAutoConfiguration(GrpcAutoConfiguration.class)
    static class GrpcAutoConfigurationImporter {
    }
}
