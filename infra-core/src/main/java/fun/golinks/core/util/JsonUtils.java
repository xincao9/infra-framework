package fun.golinks.core.util;

import com.alibaba.fastjson2.JSON;

public class JsonUtils {

    public static <T> String toJsonString(T o) {
        return JSON.toJSONString(o);
    }
}
