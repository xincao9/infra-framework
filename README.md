# infra-framework

开箱即用的Java基础框架

## 项目简介

infra-framework 是一个开箱即用的Java基础框架，旨在提供常用功能，简化项目开发。框架包括以下子模块：

- [infra-core](#infra-core)
- [infra-trace](#infra-trace)
- [infra-config](#infra-config)

## 子模块

### infra-core

提供一些Java项目实践中常用的功能：

- 异常、CORS、Trace 统一处理
- 状态码枚举 (`fun.golinks.core.consts.StatusEnums`)
- 限流功能
- 接口声明方式调用外部接口

#### 示例代码

限流功能示例：

```java
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
### infra-trace

**简单专注**：拒绝臃肿的依赖，保持功能专一。

#### 功能点

infra-trace 提供以下功能：

* 使用 Zipkin 追踪调用
* 追踪范围：
    * Spring MVC 接口追踪
    * MyBatis 数据库调用追踪
    * gRPC 远程调用追踪
    * Redis 访问追踪
    * 
#### 配置文件

在你的 `application.yml` 文件中添加以下配置：

```yaml
infra:
  trace:
    enabled: true
    zipkin:
      url: "http://localhost:9411/api/v2/spans"
      sampler: "1.0F"
```

### infra-config

#### 设计思想

**简单专注**；无需为了集中配置管理，而增加新的中间件；依赖git对配置进行权限和版本管理；极少的代码量方便进行二次开发

#### 功能点

* 项目启动和运行中，实时同步git中的项目配置文件到本地，位于 ${home}/.config/{infra.config.app-name}/ 目录下
* @Value标记的Bean字段，实时更新最新的配置
* @ConfigurationProperties 标记的配置属性Bean，实时更新最新的配置

#### 配置文件

位置：classpath:/resources/config.yaml

```yaml
infra:
  config:
    app-name: sample # 应用名，一般设置为spring.application.name一样
    enabled: true # 功能是否开启
    type: git # 使用git做为配置中心
    git:
      uri: https://github.com/xincao9/sample-config-repo.git # git配置文件仓库
      remote: origin # 远程库名，一般不需要改动
      remote-branch-name: main # 配置使用的代码分支
      delay-seconds: 30 # 配置同步到本地的延迟，单位：秒
```

## 贡献

欢迎提交Issues和Pull Requests进行贡献。
