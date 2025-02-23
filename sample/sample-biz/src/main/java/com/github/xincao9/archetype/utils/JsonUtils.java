package com.github.xincao9.archetype.utils;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;

/**
 * JSON工具类
 */
public class JsonUtils {

    public static <T> String toJson(T o) {
        return JSON.toJSONString(o);
    }

    public static <T> T toBean(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    public static <T> T toBean(String text, Type objectType) {
        return JSON.parseObject(text, objectType);
    }
}
