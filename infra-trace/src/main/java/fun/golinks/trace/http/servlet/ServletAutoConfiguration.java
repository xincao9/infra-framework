package fun.golinks.trace.http.servlet;

import brave.Tracing;
import brave.jakarta.servlet.TracingFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletAutoConfiguration {

    @Bean
    public Filter tracingFilter(Tracing tracing) {
        return TracingFilter.create(tracing);
    }

    @Bean
    public FilterRegistrationBean<Filter> filterFilterRegistrationBean(Filter tracingFilter) {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(tracingFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
