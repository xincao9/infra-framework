package fun.golinks.sample.controller;

import fun.golinks.grpc.pure.util.GrpcFunction;
import fun.golinks.grpc.pure.util.GrpcInvoker;
import fun.golinks.sample.GreeterGrpc;
import fun.golinks.sample.HelloReply;
import fun.golinks.sample.HelloRequest;
import fun.golinks.sample.entity.SysUser;
import fun.golinks.sample.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("greeter")
public class GreeterController {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    private final GrpcInvoker<HelloRequest, HelloReply> grpcInvoker = GrpcInvoker
            .wrap(new GrpcFunction<HelloRequest, HelloReply>() {
                @Override
                public HelloReply apply(HelloRequest helloRequest) throws Throwable {
                    return greeterBlockingStub.sayHello(helloRequest);
                }
            });

    @GetMapping
    public String say(@RequestParam("name") String name) throws Throwable {
        SysUser sysUser = sysUserService.findByName(name);
        if (sysUser == null) {
            return "not found";
        }
        HelloRequest request = HelloRequest.newBuilder().setName(sysUser.getEmail()).build();
        HelloReply response = grpcInvoker.apply(request);
        return response.getMessage();
    }
}
