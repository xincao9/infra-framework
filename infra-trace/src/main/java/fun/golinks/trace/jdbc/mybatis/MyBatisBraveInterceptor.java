package fun.golinks.trace.jdbc.mybatis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * mybatis trace 拦截器
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }) })
public class MyBatisBraveInterceptor implements Interceptor {

    private static final String SQL = "SQL";
    private final Tracing tracing;

    /**
     * 构造器
     * 
     * @param tracing
     *            追踪
     */
    public MyBatisBraveInterceptor(Tracing tracing) {
        this.tracing = tracing;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String[] strArr = mappedStatement.getId().split("\\.");
        String methodName = strArr[strArr.length - 2] + "." + strArr[strArr.length - 1];
        Tracer tracer = tracing.tracer();
        TraceContext currentTraceContext = tracer.currentSpan().context();
        String sql = showSQL(invocation, mappedStatement);
        Span span = currentTraceContext != null ? tracer.nextSpan().name("mybatis: " + methodName).tag(SQL, sql).start()
                : null;
        try {
            return invocation.proceed();
        } finally {
            if (span != null) {
                span.finish();
            }
        }
    }

    private String showSQL(Invocation invocation, MappedStatement mappedStatement) {
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        StringBuilder sb = new StringBuilder(sql);
        if (!parameterMappings.isEmpty() && parameterObject != null) {
            int start = sb.indexOf("?");
            int end = start + 1;
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sb.replace(start, end, getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sb.replace(start, end, getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sb.replace(start, end, getParameterValue(obj));
                    }
                    start = sb.indexOf("?");
                    end = start + 1;
                }
            }
        }
        return sb.toString();
    }

    private String getParameterValue(Object obj) {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof String) {
            sb.append("'").append(obj).append("'");
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            sb.append("'").append(formatter.format((Date) obj)).append("'");
        } else {
            sb.append("'").append(obj == null ? "" : obj).append("'");
        }
        return sb.toString();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}