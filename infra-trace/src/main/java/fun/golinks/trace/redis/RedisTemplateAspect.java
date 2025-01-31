package fun.golinks.trace.redis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * RedisTemplate切面
 */
@Aspect
@Slf4j
public class RedisTemplateAspect {
    private final Tracer tracer;

    public RedisTemplateAspect(Tracing tracing) {
        this.tracer = tracing.tracer();
    }

    @Around("execution(* org.springframework.data.redis.core.RedisTemplate.*(..))")
    public Object traceRedisTemplate(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Span span = tracer.nextSpan().name("redis: " + methodName).start();
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            span.error(throwable);
            throw throwable;
        } finally {
            span.finish();
        }
    }
}
