package fun.golinks.trace.redis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RedisTemplateInvocationHandler implements InvocationHandler {

    private final Tracing tracing;
    private final RedisTemplate<?, ?> redisTemplate;

    public RedisTemplateInvocationHandler(Tracing tracing, RedisTemplate<?, ?> redisTemplate) {
        this.tracing = tracing;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Tracer tracer = tracing.tracer();
        TraceContext currentTraceContext = tracer.currentSpan().context();
        Span span = currentTraceContext != null ? tracer.nextSpan().name(method.getName()).start() : null;
        try {
            return method.invoke(redisTemplate, args);
        } finally {
            if (span != null) {
                span.finish();
            }
        }
    }
}
