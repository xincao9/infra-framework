package fun.golinks.core.config;

import fun.golinks.core.interceptor.WebHandlerMethodReturnValueHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

public class HandlerMethodReturnValueHandlerConfig implements InitializingBean {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    public HandlerMethodReturnValueHandlerConfig(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 搞不清楚 WebMvcConfigurer的addReturnValueHandlers方法添加，为什么不生效
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();
        handlers.add(new WebHandlerMethodReturnValueHandler());
        List<HandlerMethodReturnValueHandler> oHandlers = requestMappingHandlerAdapter.getReturnValueHandlers();
        if (oHandlers != null) {
            handlers.addAll(oHandlers);
        }
        requestMappingHandlerAdapter.setReturnValueHandlers(handlers);
    }
}
