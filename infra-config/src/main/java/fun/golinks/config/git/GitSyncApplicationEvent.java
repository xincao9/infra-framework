package fun.golinks.config.git;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * git配置同步事件
 */
@Getter
public class GitSyncApplicationEvent extends ApplicationEvent {

    public Map<String, Object> configItems;

    public GitSyncApplicationEvent(Object source, Map<String, Object> configItems) {
        super(source);
        this.configItems = configItems;
    }
}
