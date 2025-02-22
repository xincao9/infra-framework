package fun.golinks.core.interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.RateLimiter;
import fun.golinks.core.annotate.PreAuthorizeRole;
import fun.golinks.core.annotate.RateLimited;
import fun.golinks.core.consts.RoleEnums;
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
            return validateRole(response, handler, null);
        }
        // 从 Authorization 头中提取 JWT 令牌
        String token = authHeader.substring(7); // 移除 "Bearer " 前缀
        // 解析 JWT 令牌并获取其中的声明信息
        Claims claims = jwtProperties.parseToken(token);
        if (claims == null) {
            sendForbidenResponse(response);
            return false;
        }
        ClaimsUtils.setClaims(claims);
        return validateRole(response, handler, claims);
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
                    response.setStatus(StatusEnums.RATE_LIMIT_EXCEEDED.getCode());
                    response.getWriter().write(JsonUtils.toJson(R.failed(StatusEnums.RATE_LIMIT_EXCEEDED)));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 验证用户角色是否具有访问指定接口的权限。
     *
     * @param response
     *            HttpServletResponse对象，用于在验证失败时发送响应。
     * @param handler
     *            当前请求的处理方法对象，用于获取方法上的注解信息。
     * @param claims
     *            用户认证信息，包含用户的角色等数据。
     * 
     * @return 如果用户具有访问权限，返回true；否则返回false。
     * 
     * @throws IOException
     *             当发送响应时发生IO异常时抛出。
     */
    private boolean validateRole(HttpServletResponse response, Object handler, Claims claims) throws IOException {
        // 检查handler是否为HandlerMethod类型
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取方法上的PreAuthorizeRole注解
            PreAuthorizeRole preAuthorizeRole = handlerMethod.getMethodAnnotation(PreAuthorizeRole.class);

            // 如果方法上没有PreAuthorizeRole注解，表示接口不需要认证，直接返回true
            if (preAuthorizeRole == null) {
                return true;
            }

            // 如果接口需要认证，但claims为空，表示没有用户认证信息，发送403响应并返回false
            if (claims == null) {
                sendForbidenResponse(response);
                return false;
            }

            // 获取注解中定义的角色和用户的实际角色
            RoleEnums roleEnums = preAuthorizeRole.value();
            String role = claims.get(SystemConsts.ROLE_KEY, String.class);
            RoleEnums userRole = RoleEnums.fromFlag(role);

            // 检查用户角色是否包含在允许的角色列表中
            if (RoleEnums.contain(userRole, roleEnums)) {
                return true;
            } else {
                // 如果用户角色不在允许的列表中，发送403响应并返回false
                sendForbidenResponse(response);
                return false;
            }
        }
        // 如果handler不是HandlerMethod类型，直接返回true
        return true;
    }

    private void sendForbidenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(StatusEnums.FORBIDDEN.getCode()); // 设置正确的HTTP状态码
        try (PrintWriter writer = response.getWriter()) { // 使用 try-with-resources 确保资源关闭
            writer.write(JsonUtils.toJson(R.failed(StatusEnums.FORBIDDEN)));
        } catch (IOException e) {
            log.error("sendForbidenResponse", e);
        }
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
            response.setStatus(StatusEnums.INTERNAL_SERVER_ERROR.getCode());
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
