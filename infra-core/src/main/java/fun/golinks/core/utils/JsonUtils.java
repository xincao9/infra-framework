package fun.golinks.core.utils;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;

/**
 * JSON工具类
 */
public class JsonUtils {

    public static <T> String toJsonString(T o) {
        return JSON.toJSONString(o);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    public static <T> T parseObject(String text, Type objectType) {
        return JSON.parseObject(text, objectType);
    }
}
