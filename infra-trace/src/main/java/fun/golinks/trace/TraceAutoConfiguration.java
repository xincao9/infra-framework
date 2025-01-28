package fun.golinks.trace;

import brave.Tracing;
import brave.sampler.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
public class TraceAutoConfiguration {

    private static final String ZIPKIN_URL = "http://localhost:9411/api/v2/spans";

    @Bean
    public AsyncZipkinSpanHandler asyncZipkinSpanHandler() {
        return AsyncZipkinSpanHandler.create(URLConnectionSender.create(ZIPKIN_URL));
    }

    @Bean
    public Tracing tracing(@Value("${spring.application.name}") String applicationName) {
        return Tracing.newBuilder().localServiceName(applicationName).addSpanHandler(asyncZipkinSpanHandler())
                .sampler(Sampler.ALWAYS_SAMPLE).build();
    }
}
