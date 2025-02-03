package fun.golinks.core.interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import fun.golinks.core.annotate.RateLimited;
import fun.golinks.core.consts.StatusEnums;
import fun.golinks.core.model.R;
import fun.golinks.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebHandlerInterceptor implements HandlerInterceptor {

    private static final int STATUS = 200;
    private final Cache<Method, RateLimiter> methodRateLimiterCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(1000).build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
            Method method = handlerMethod.getMethod();
            if (rateLimited != null && rateLimited.permitsPerSecond() > 0) {
                RateLimiter rateLimiter = methodRateLimiterCache.getIfPresent(handlerMethod.getMethod());
                if (rateLimiter == null) {
                    rateLimiter = RateLimiter.create(rateLimited.permitsPerSecond());
                    methodRateLimiterCache.put(method, rateLimiter);
                }
                if (!rateLimiter.tryAcquire()) {
                    response.setStatus(STATUS);
                    response.getWriter().write(JsonUtils.toJsonString(R.failed(StatusEnums.RATE_LIMIT_EXCEEDED)));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (ex != null) {
            response.setStatus(STATUS);
            response.getWriter().write(JsonUtils.toJsonString(R.failed(StatusEnums.SYSTEM_EXCEPTION)));
        }
    }
}
