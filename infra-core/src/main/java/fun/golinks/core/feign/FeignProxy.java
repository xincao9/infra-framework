package fun.golinks.core.feign;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Charsets;
import feign.Feign;
import feign.Util;
import fun.golinks.core.annotate.FeignClient;
import fun.golinks.core.exception.FeignClientException;
import fun.golinks.core.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeignProxy {

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
        }).encoder((o, type, requestTemplate) -> requestTemplate.body(JsonUtils.toJsonString(o))).target(clazz, feignClient.baseUrl());
        cached.put(clazz, obj);
        return obj;
    }
}
