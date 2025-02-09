# infra-core

提供一些Java项目实践中常用的功能

## 功能点

- 异常、CORS、Trace 统一处理
- 状态码枚举 (`fun.golinks.core.consts.StatusEnums`)
- 限流功能
- 接口声明方式调用外部接口

## 限流

以下示例展示了如何在Spring Boot项目中实现限流功能：

```java
import com.github.xincao9.archetype.entity.SysUser;
import com.github.xincao9.archetype.model.GreeterSayRequestVO;
import com.github.xincao9.archetype.model.GreeterSayResponseVO;
import com.github.xincao9.archetype.rpc.invoker.GreeterInvoker;
import com.github.xincao9.archetype.service.SysUserService;
import com.github.xincao9.infra.archetype.GreeterSayRequest;
import com.github.xincao9.infra.archetype.GreeterSayResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 演示，一般的业务流程
 */
@RestController
@RequestMapping("greeter")
public class GreeterController {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private GreeterInvoker greeterInvoker;

    @RateLimited(permitsPerSecond = 10)
    @PostMapping("say")
    public GreeterSayResponseVO say(@RequestBody GreeterSayRequestVO greeterSayRequestVO) throws Throwable {
        // 读取数据库
        SysUser sysUser = sysUserService.findByName(greeterSayRequestVO.getName());
        if (sysUser == null) {
            return null;
        }
        // 调用grpc服务
        GreeterSayRequest request = GreeterSayRequest.newBuilder().setName(sysUser.getEmail()).build();
        GreeterSayResponse response = greeterInvoker.sayInvoker.apply(request);
        return new GreeterSayResponseVO(response.getMessage());
    }
}
```

## 接口声明方式调用外部接口

### 定义Model

定义用于接收远程接口数据的模型类：

```java
import lombok.Data;

@Data
public class Contributor {
    private String login;
    private int contributions;
}
```

### 描述远程接口
使用Feign描述远程HTTP接口：

```java
import feign.Param;
import feign.Request;
import feign.RequestLine;
import fun.golinks.core.annotate.FeignClient;

import java.util.List;

/**
 * 演示，调用三方http接口的方式
 */
@FeignClient(baseUrl = "https://api.github.com")
public interface GitHubClient {

    /**
     * 查看贡献者
     *
     * @param owner 拥有者
     * @param repo 仓库
     * @param options 不修改默认调用参数时，可以不用这个参数
     * 
     * @return 贡献者
     */
    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo, Request.Options options);
}
```

### 引用远程接口

在控制器中引用远程接口：

```java
import feign.Request;
import fun.golinks.core.annotate.FeginResource;
import fun.golinks.core.exception.FeignClientException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 演示，调用三方http接口的方式
 */
@RequestMapping("github")
@RestController
public class GithubController {

    @FeginResource
    private GitHubClient gitHubClient;

    @GetMapping("contributors")
    public List<Contributor> contributors() throws FeignClientException {
        return gitHubClient.contributors("xincao9", "infra-framework", new Request.Options(1000, 500));
    }
}
```


