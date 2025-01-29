package fun.golinks.trace.redis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Slf4j
public class RedisTemplateAspect {
    private final Tracer tracer;

    public RedisTemplateAspect(Tracing tracing) {
        this.tracer = tracing.tracer();
    }

    @Around("execution(* org.springframework.data.redis.core.RedisTemplate.*(..))")
    public Object traceRedisOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Span span = tracer.nextSpan().name("Redis: " + methodName).start();

        try {
            log.info("Executing Redis method: {}", methodName);
            Object result = joinPoint.proceed();
            log.info("Redis method {} executed successfully", methodName);
            return result;
        } catch (Throwable throwable) {
            span.error(throwable);
            log.error("Redis method {} threw an exception: {}", methodName, throwable.getMessage());
            throw throwable;
        } finally {
            span.finish();
        }
    }
}
