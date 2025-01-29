package fun.golinks.trace.jdbc.mybatis;

import brave.Tracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisAutoConfiguration {

    @Bean
    public MyBatisBraveInterceptor myBatisBraveInterceptor(Tracing tracing) {
        return new MyBatisBraveInterceptor(tracing);
    }
}
