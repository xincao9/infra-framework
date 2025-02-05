package fun.golinks.core;

import fun.golinks.core.config.WebConfig;
import fun.golinks.core.feign.FeignProxy;
import fun.golinks.core.interceptor.WebHandlerMethodReturnValueHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@ImportAutoConfiguration(CoreAutoConfiguration.WebConfigImporter.class)
public class CoreAutoConfiguration {

    @Bean
    public FeignProxy feignProxy() {
        return new FeignProxy();
    }

    @ConditionalOnWebApplication
    @ImportAutoConfiguration(WebConfig.class)
    public static class WebConfigImporter implements InitializingBean {

        @Resource
        private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

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
}
