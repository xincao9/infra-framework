package fun.golinks.core.feign;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Charsets;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Util;
import fun.golinks.core.annotate.FeignClient;
import fun.golinks.core.exception.FeignClientException;
import fun.golinks.core.utils.JsonUtils;
import fun.golinks.core.utils.MDCUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FeignProxy {

    private static final int CONNECT_TIMEOUT_MILLIS = 1000;
    private static final int READ_TIMEOUT_MILLIS = 1000;
    private static final Request.Options OPTIONS = new Request.Options(CONNECT_TIMEOUT_MILLIS, READ_TIMEOUT_MILLIS);
    private final Map<Class<?>, Object> cached = new ConcurrentHashMap<>();

    public <T> T getOrCreate(Class<T> clazz) throws FeignClientException {
        if (cached.containsKey(clazz)) {
            return (T) cached.get(clazz);
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
                return JSONObject.parseObject(new String(bytes, Charsets.UTF_8), type);
            }
        }).encoder((o, type, requestTemplate) -> {
            requestTemplate.header(MDCUtils.TRACE_ID, MDCUtils.getTraceId());
            requestTemplate.body(JsonUtils.toJsonString(o));
        }).logger(new Logger() {
            @Override
            protected void log(String configKey, String format, Object... args) {
                log.info("[{}]{}", configKey, String.format(format, args));
            }
        }).logLevel(Logger.Level.BASIC).options(OPTIONS).target(clazz, feignClient.baseUrl());
        cached.put(clazz, obj);
        return obj;
    }
}
