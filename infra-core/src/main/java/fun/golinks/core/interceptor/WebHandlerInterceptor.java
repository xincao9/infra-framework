package fun.golinks.core.interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.RateLimiter;
import fun.golinks.core.annotate.RateLimited;
import fun.golinks.core.consts.StatusEnums;
import fun.golinks.core.consts.SystemConsts;
import fun.golinks.core.properties.JwtProperties;
import fun.golinks.core.utils.ClaimsUtils;
import fun.golinks.core.utils.JsonUtils;
import fun.golinks.core.utils.TraceContext;
import fun.golinks.core.vo.R;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Slf4j
public class WebHandlerInterceptor implements HandlerInterceptor {

    private static final int STATUS = 200;
    private static final String METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String OPTIONS = "OPTIONS";
    private final Cache<Method, RateLimiter> methodRateLimiterCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(1000).build();

    /**
     * 配置请求日志忽略的请求头信息
     */
    private static final Set<String> IGNORE_HEADERS = new HashSet<>();

    static {
        IGNORE_HEADERS.add("connection");
        IGNORE_HEADERS.add("accept-language");
        IGNORE_HEADERS.add("accept-encoding");
        IGNORE_HEADERS.add("sec-fetch-dest");
        IGNORE_HEADERS.add("sec-fetch-mode");
        IGNORE_HEADERS.add("sec-fetch-site");
        IGNORE_HEADERS.add("sec-ch-ua-mobile");
        IGNORE_HEADERS.add("sec-ch-ua");
        IGNORE_HEADERS.add("sec-ch-ua-platform");
    }

    private final JwtProperties jwtProperties;

    public WebHandlerInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * web请求的前置处理
     *
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            chosen handler to execute, for type and/or instance evaluation
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        logRequestDetails(request, null); // 确保 handler 不为 null
        if (rateLimiter(response, handler)) {
            return false;
        }
        if (cors(request, response)) {
            return false;
        }
        String authHeader = request.getHeader(SystemConsts.AUTH_HEADER);
        // 如果 Authorization 头为空或不以 "Bearer " 开头，则直接继续执行过滤器链
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return true;
        }
        // 从 Authorization 头中提取 JWT 令牌
        String token = authHeader.substring(7); // 移除 "Bearer " 前缀
        // 解析 JWT 令牌并获取其中的声明信息
        Claims claims = jwtProperties.parseToken(token);
        if (claims == null) {
            // 如果解析失败，返回未授权的响应
            response.setStatus(StatusEnums.SUCCESS.getCode());
            try (PrintWriter writer = response.getWriter()) { // 使用 try-with-resources 确保资源关闭
                writer.write(JsonUtils.toJson(R.failed(StatusEnums.UNAUTHORIZED)));
            }
            return false;
        } else {
            ClaimsUtils.setClaims(claims);
        }
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
                    response.setStatus(StatusEnums.SUCCESS.getCode());
                    response.getWriter().write(JsonUtils.toJson(R.failed(StatusEnums.RATE_LIMIT_EXCEEDED)));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 配置cors
     *
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * 
     * @return
     */
    private static boolean cors(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, METHODS);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        String method = request.getMethod();
        return Objects.equals(method, OPTIONS);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    /**
     * 后置处理器
     *
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the handler (or {@link HandlerMethod}) that started asynchronous execution, for type and/or instance
     *            examination
     * @param ex
     *            any exception thrown on handler execution, if any; this does not include exceptions that have been
     *            handled through an exception resolver
     * 
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        ClaimsUtils.removeClaims();
        if (ex != null) {
            log.error(ex.getMessage(), ex);
            response.setStatus(StatusEnums.SUCCESS.getCode());
            response.getWriter().write(JsonUtils.toJson(R.failed(StatusEnums.INTERNAL_SERVER_ERROR)));
        }
    }

    /**
     * 打印请求日志
     *
     * @param request
     *            current HTTP request
     * @param ex
     *            any exception thrown on handler execution
     */
    private void logRequestDetails(HttpServletRequest request, Throwable ex) {
        String traceId = request.getHeader(TraceContext.TRACE_ID);
        TraceContext.setTraceId(traceId);
        StringBuilder requestDetails = new StringBuilder();
        // Basic request information
        requestDetails.append("|").append(request.getRequestURI()).append("|");
        requestDetails.append(request.getRemoteAddr()).append("|");
        // Headers information
        requestDetails.append("[");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (IGNORE_HEADERS.contains(headerName)) {
                continue;
            }
            String headerValue = request.getHeader(headerName);
            requestDetails.append(headerName).append(": ").append(headerValue).append("; ");
        }
        requestDetails.append("]|");
        // Parameters information
        requestDetails.append("[");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            requestDetails.append(paramName).append(": ").append(paramValue).append("; ");
        }
        requestDetails.append("]");
        // Log request details
        if (ex != null) {
            log.error("{} error: ", requestDetails.toString(), ex);
            return;
        }
        log.info(requestDetails.toString());

    }
}
