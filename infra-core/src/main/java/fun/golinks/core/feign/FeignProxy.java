package fun.golinks.core.feign;

import com.google.common.base.Charsets;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Util;
import fun.golinks.core.annotate.FeignClient;
import fun.golinks.core.exception.FeignClientException;
import fun.golinks.core.utils.JsonUtils;
import fun.golinks.core.utils.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FeignProxy<T> {

    private static final int CONNECT_TIMEOUT_MILLIS = 1000;
    private static final int READ_TIMEOUT_MILLIS = 1000;
    private static final Request.Options OPTIONS = new Request.Options(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS,
            READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS, true);
    private final Map<Class<T>, T> cached = new ConcurrentHashMap<>();

    public T getOrCreate(Class<T> clazz) throws FeignClientException {
        if (cached.containsKey(clazz)) {
            return cached.get(clazz);
        }
        FeignClient feignClient = clazz.getAnnotation(FeignClient.class);
        if (feignClient == null || StringUtils.isBlank(feignClient.baseUrl())) {
            throw new FeignClientException("@FeignClient is not used correctly");
        }
        T obj = Feign.builder().decoder((response, type) -> {
            if (response.body() == null) {
                return null;
            } else {
                byte[] bytes = Util.toByteArray(response.body().asInputStream());
                return JsonUtils.toBean(new String(bytes, Charsets.UTF_8), type);
            }
        }).encoder((o, type, requestTemplate) -> {
            requestTemplate.body(JsonUtils.toJson(o));
        }).logger(new Logger() {
            @Override
            protected void log(String configKey, String format, Object... args) {
                log.info("[{}]{}", configKey, String.format(format, args));
            }
        }).logLevel(Logger.Level.BASIC)
                .requestInterceptor(
                        requestTemplate -> requestTemplate.header(TraceContext.TRACE_ID, TraceContext.getTraceId()))
                .options(OPTIONS).target(clazz, feignClient.baseUrl());
        cached.put(clazz, obj);
        return obj;
    }
}
