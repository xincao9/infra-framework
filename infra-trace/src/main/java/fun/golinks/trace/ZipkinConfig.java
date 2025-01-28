package fun.golinks.trace;

import lombok.Data;

@Data
public class ZipkinConfig {

    private String url = "http://localhost:9411/api/v2/spans";

    private float sampler = 0.01F;
}
