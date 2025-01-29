package fun.golinks.trace.redis;

import brave.Tracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Configuration
public class RedisAutoConfiguration {

    @Bean
    public RedisTemplateAspect redisTemplateAspect(Tracing tracing) {
        return new RedisTemplateAspect(tracing);
    }

}
