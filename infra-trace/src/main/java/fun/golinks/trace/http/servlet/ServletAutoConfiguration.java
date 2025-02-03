package fun.golinks.trace.http.servlet;

import brave.Tracing;
import brave.servlet.TracingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

/**
 * http servlet 配置类
 */
public class ServletAutoConfiguration {

    /**
     * trace http servlet 过滤器
     *
     * @param tracing
     *            追踪
     * 
     * @return 过滤器
     */
    @Bean
    public Filter tracingFilter(Tracing tracing) {
        return TracingFilter.create(tracing);
    }

    /**
     * 过滤器注册Bean
     *
     * @param tracingFilter
     *            过滤器
     * 
     * @return 过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<Filter> filterFilterRegistrationBean(Filter tracingFilter) {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(tracingFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
