package fun.golinks.registry;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import fun.golinks.registry.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NacosSmartLifecycle implements SmartLifecycle {

    private static final Integer REGISTER_TIMER_PERIOD_SECOND = 30;
    private final String appName;
    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final NamingService namingService;
    private final ScheduledExecutorService scheduledExecutorService = Executors
            .newSingleThreadScheduledExecutor(r -> new Thread(r, "NacosSmartLifecycle"));

    public NacosSmartLifecycle(RegistryProperties registryProperties) throws Throwable {
        this.appName = registryProperties.getAppName();
        this.port = registryProperties.getServer().getPort();
        NacosConfig nacosConfig = registryProperties.getDiscovery().getNacos();
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosConfig.getAddress());
        properties.put(PropertyKeyConst.USERNAME, nacosConfig.getUsername());
        properties.put(PropertyKeyConst.PASSWORD, nacosConfig.getPassword());
        properties.put(PropertyKeyConst.NAMESPACE, nacosConfig.getNamespace());
        this.namingService = NacosFactory.createNamingService(properties);
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.info("ServerRegister has been started!");
            return;
        }
        if (!register()) {
            throw new RuntimeException("Failed to register Nacos");
        }
    }

    public Boolean register() {
        if (!registerInstance(appName)) {
            return false;
        }
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                List<Instance> instances = namingService.getAllInstances(appName);
                if (instances == null || instances.isEmpty()) {
                    registerInstance(appName);
                    return;
                }
                for (Instance instance : instances) {
                    if (Objects.equals(instance.getIp(), IpUtils.getIP()) && Objects.equals(instance.getPort(), port)) {
                        return;
                    }
                }
                registerInstance(appName);
            } catch (Throwable e) {
                log.error("Service registration scheduled task", e);
            }
        }, REGISTER_TIMER_PERIOD_SECOND, REGISTER_TIMER_PERIOD_SECOND, TimeUnit.SECONDS);
        return true;
    }

    private Boolean registerInstance(String serverName) {
        Instance instance = createInstance();
        Throwable finalException = null;
        int retry = 3;
        while (retry > 0) {
            try {
                namingService.registerInstance(serverName, instance);
                log.info("RPC instance successfully registered with Nacos {} => {}:{}", serverName, instance.getIp(),
                        instance.getPort());
                return true;
            } catch (Throwable e) {
                finalException = e;
                log.warn("RPC instance registration failed for Nacos", e);
            }
            retry--;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("sleep interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        log.error("RPC instance registration failed for Nacos", finalException);
        return false;
    }

    private Instance createInstance() {
        Instance instance = new Instance();
        instance.setIp(IpUtils.getIP());
        instance.setPort(port);
        instance.setEphemeral(true);
        instance.setWeight(1000);
        Map<String, String> metadata = new HashMap<>(1);
        metadata.put(SystemConsts.REGISTRATION_TIME_PROPS, String.valueOf(System.currentTimeMillis()));
        instance.setMetadata(metadata);
        return instance;
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            log.info("Failed to close ServerRegister!");
            return;
        }
        try {
            namingService.shutDown();
        } catch (Throwable e) {
            log.warn("Failed to shutdown Nacos", e);
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
