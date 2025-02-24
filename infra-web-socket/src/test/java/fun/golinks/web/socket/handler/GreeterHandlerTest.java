package fun.golinks.web.socket.handler;

import fun.golinks.web.socket.config.SystemConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SystemConfig.class)
public class GreeterHandlerTest {

    @BeforeEach
    public void setup() throws Exception {

    }

    @Test
    public void testHandle() {
    }
}
