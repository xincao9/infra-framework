package fun.golinks.core.interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.RateLimiter;
import fun.golinks.core.annotate.RateLimited;
import fun.golinks.core.consts.StatusEnums;
import fun.golinks.core.model.R;
import fun.golinks.core.utils.JsonUtils;
import fun.golinks.core.utils.MDCUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebHandlerInterceptor implements HandlerInterceptor {

    private static final int STATUS = 200;
    private static final String METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String OPTIONS = "OPTIONS";
    private final Cache<Method, RateLimiter> methodRateLimiterCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(1000).build();

    private static boolean cors(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, METHODS);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        String method = request.getMethod();
        if (Objects.equals(method, OPTIONS)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        logRequestDetails(request);
        if (rateLimiter(response, handler)) {
            return false;
        }
        if (cors(request, response))
            return false;
        return true;
    }

    /**
     * 判断限流
     *
     * @param response
     *            响应
     * @param handler
     *            处理器
     * 
     * @return
     * 
     * @throws IOException
     *             IO异常
     */
    private boolean rateLimiter(HttpServletResponse response, Object handler) throws IOException {
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
                    return true;
                }
            }
        }
        return false;
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
            response.getWriter().write(JsonUtils.toJsonString(R.failed(StatusEnums.INTERNAL_SERVER_ERROR)));
        }
    }

    private void logRequestDetails(HttpServletRequest request) {
        String traceId = request.getHeader(MDCUtils.TRACE_ID);
        MDCUtils.setTraceId(traceId);
        StringBuilder requestDetails = new StringBuilder();
        // Basic request information
        requestDetails.append("Request-URI: ").append(request.getRequestURI()).append(", ");
        requestDetails.append("Remote-Address: ").append(request.getRemoteAddr()).append(", ");
        // Headers information
        requestDetails.append("Headers: [");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            requestDetails.append(headerName).append(": ").append(headerValue).append("; ");
        }
        requestDetails.append("], ");
        // Parameters information
        requestDetails.append("Parameters: [");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            requestDetails.append(paramName).append(": ").append(paramValue).append("; ");
        }
        requestDetails.append("]");
        // Log request details
        log.info(requestDetails.toString());
    }
}
