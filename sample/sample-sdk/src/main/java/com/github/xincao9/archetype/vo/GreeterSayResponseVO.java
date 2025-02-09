package com.github.xincao9.archetype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GreeterSayResponseVO {

    @Schema(description = "问候消息")
    private String message;
}
