package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.entity.SysUser;
import com.github.xincao9.archetype.model.GreeterSayRequestModel;
import com.github.xincao9.archetype.model.GreeterSayResponseModel;
import com.github.xincao9.archetype.rpc.invoker.GreeterInvoker;
import com.github.xincao9.archetype.service.SysUserService;
import com.github.xincao9.infra.archetype.GreeterSayRequest;
import com.github.xincao9.infra.archetype.GreeterSayResponse;
import fun.golinks.core.annotate.RateLimited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 演示，一般的业务流程
 */
@RestController
@RequestMapping("greeter")
@Validated
public class GreeterController {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private GreeterInvoker greeterInvoker;

    @Operation(summary = "Say a greeting", description = "This endpoint says a greeting based on the name provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input") })
    @RateLimited(permitsPerSecond = 10)
    @PostMapping("say")
    public GreeterSayResponseModel say(@Valid @RequestBody GreeterSayRequestModel greeterSayRequestModel)
            throws Throwable {
        // 读取数据库
        SysUser sysUser = sysUserService.findByName(greeterSayRequestModel.getName());
        if (sysUser == null) {
            return null;
        }
        // 调用grpc服务
        GreeterSayRequest request = GreeterSayRequest.newBuilder().setName(sysUser.getEmail()).build();
        GreeterSayResponse response = greeterInvoker.sayInvoker.apply(request);
        return new GreeterSayResponseModel(response.getMessage());
    }
}
