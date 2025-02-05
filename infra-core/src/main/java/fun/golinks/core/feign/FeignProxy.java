package fun.golinks.core.feign;

import feign.Feign;
import fun.golinks.core.annotate.FeignClient;
import fun.golinks.core.exception.FeignClientException;
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
            throw new FeignClientException("@FeignClient未正确使用");
        }
        T o = Feign.builder().target(clazz, feignClient.baseUrl());
        cached.put(clazz, o);
        return o;
    }
}
