package fun.golinks.core.feign;

import fun.golinks.core.annotate.FeginResource;
import fun.golinks.core.exception.FeignClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;

@Slf4j
public class FeginResourceBeanPostProcessor implements BeanPostProcessor {

    private final FeignProxy feignProxy;

    public FeginResourceBeanPostProcessor(FeignProxy feignProxy) {
        this.feignProxy = feignProxy;
    }

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
}
