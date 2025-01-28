package fun.golinks.trace;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infra.trace")
public class TraceProperties {

    private Boolean enable = true;

    private ZipkinConfig zipkin = new ZipkinConfig();
}
