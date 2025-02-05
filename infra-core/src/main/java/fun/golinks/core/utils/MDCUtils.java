package fun.golinks.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MDCUtils {

    public static final String TRACE_ID = "trace-id";

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static void setTraceId(String traceId) {
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        Map<String, String> context = MDC.getCopyOfContextMap();
        Map<String, String> newContext = new HashMap<>();
        newContext.put(TRACE_ID, traceId);
        if (context != null) {
            newContext.putAll(context);
        }
        MDC.setContextMap(newContext);
    }
}
