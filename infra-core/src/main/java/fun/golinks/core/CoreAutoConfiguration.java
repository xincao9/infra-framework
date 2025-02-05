package fun.golinks.core;

import fun.golinks.core.annotate.FeginResource;
import fun.golinks.core.config.WebConfig;
import fun.golinks.core.exception.FeignClientException;
import fun.golinks.core.feign.FeignProxy;
import fun.golinks.core.interceptor.WebHandlerMethodReturnValueHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ImportAutoConfiguration(CoreAutoConfiguration.WebConfigImporter.class)
public class CoreAutoConfiguration {

    @Bean
    public FeignProxy feignProxy() {
        return new FeignProxy();
    }

    @Bean
    public BeanPostProcessor feginResourceBeanPostProcessor(FeignProxy feignProxy) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                Class<?> clazz = ClassUtils.getUserClass(bean);
                Field[] fields = clazz.getDeclaredFields();
                if (ArrayUtils.isEmpty(fields)) {
                    return bean;
                }
                for (Field field : fields) {
                    if (!field.isAnnotationPresent(FeginResource.class)) {
                        continue;
                    }
                    try {
                        Object obj = feignProxy.getOrCreate(field.getType());
                        field.setAccessible(true);
                        field.set(bean, obj);
                    } catch (FeignClientException e) {
                        log.error("feignProxy.getOrCreate({})", field.getType(), e);
                    } catch (IllegalAccessException e) {
                        log.error("field.set", e);
                    }
                }
                return bean;
            }
        };
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
