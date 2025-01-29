package fun.golinks.trace.redis;

import brave.Tracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Proxy;

@Configuration
public class RedisAutoConfiguration {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(Tracing tracing, RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return (RedisTemplate<Object, Object>) Proxy.newProxyInstance(template.getClass().getClassLoader(),
                new Class[] { RedisTemplate.class }, new RedisTemplateInvocationHandler(tracing, template));
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(Tracing tracing, RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory);
        template.setConnectionFactory(redisConnectionFactory);
        return (StringRedisTemplate) Proxy.newProxyInstance(template.getClass().getClassLoader(),
                new Class[] { StringRedisTemplate.class }, new RedisTemplateInvocationHandler(tracing, template));
    }

}
