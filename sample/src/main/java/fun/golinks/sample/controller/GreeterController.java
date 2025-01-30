package fun.golinks.sample.controller;

import fun.golinks.sample.entity.SysUser;
import fun.golinks.sample.invoker.GreeterInvoker;
import grpc.fun.golinks.sample.GreeterSayRequest;
import grpc.fun.golinks.sample.GreeterSayResponse;
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
    private GreeterInvoker greeterInvoker;

    @GetMapping("say")
    public String say(@RequestParam("name") String name) throws Throwable {
        SysUser sysUser = sysUserService.findByName(name);
        if (sysUser == null) {
            return null;
        }
        GreeterSayRequest request = GreeterSayRequest.newBuilder().setName(sysUser.getEmail()).build();
        GreeterSayResponse response = greeterInvoker.SAY_INVOKER.apply(request);
        return response.getMessage();
    }
}
