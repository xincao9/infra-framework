package com.github.xincao9.archetype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GreeterSayRequestVO {

    @Schema(description = "用户名", required = true, example = "Tom")
    @NotBlank
    private String name;
}
