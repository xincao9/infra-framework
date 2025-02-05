package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.entity.SysUser;
import com.github.xincao9.archetype.rpc.invoker.GreeterInvoker;
import com.github.xincao9.archetype.service.SysUserService;
import com.github.xincao9.infra.archetype.GreeterSayRequest;
import com.github.xincao9.infra.archetype.GreeterSayResponse;
import fun.golinks.core.annotate.RateLimited;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @RateLimited(permitsPerSecond = 1)
    @GetMapping("say")
    public String say(@RequestParam("name") String name) throws Throwable {
        // 读取数据库
        SysUser sysUser = sysUserService.findByName(name);
        if (sysUser == null) {
            return null;
        }
        // 调用grpc服务
        GreeterSayRequest request = GreeterSayRequest.newBuilder().setName(sysUser.getEmail()).build();
        GreeterSayResponse response = greeterInvoker.sayInvoker.apply(request);
        return response.getMessage();
    }
}
