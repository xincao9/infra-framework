package fun.golinks.trace.jdbc.mybatis;

import brave.Tracing;
import org.springframework.context.annotation.Bean;

/**
 * mybatis 配置类
 */
public class MyBatisAutoConfiguration {

    /**
     * mybatis trace 拦截器
     *
     * @param tracing
     *            追踪
     * 
     * @return mybatis trace 拦截器
     */
    @Bean
    public MyBatisBraveInterceptor myBatisBraveInterceptor(Tracing tracing) {
        return new MyBatisBraveInterceptor(tracing);
    }
}
