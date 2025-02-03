package fun.golinks.config.git;

import fun.golinks.config.Pair;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class GitBeanPostProcessor implements BeanPostProcessor {

    private final List<Pair<String, Pair<Object, Field>>> valueAnnotationRuntime = new CopyOnWriteArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = ClassUtils.getUserClass(bean);
        Field[] fields = clazz.getDeclaredFields();
        if (ArrayUtils.isEmpty(fields)) {
            return bean;
        }
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Value.class)) {
                continue;
            }
            Value value = field.getAnnotation(Value.class);
            String key = value.value();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            key = StringUtils.substringBetween(key, "${", "}");
            key = StringUtils.substringBeforeLast(key, ":");
            if (StringUtils.isBlank(key)) {
                continue;
            }
            valueAnnotationRuntime.add(new Pair<>(key.trim(), new Pair<>(bean, field)));
        }
        return bean;
    }
}
