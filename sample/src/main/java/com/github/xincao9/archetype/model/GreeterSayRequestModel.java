package com.github.xincao9.archetype.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GreeterSayRequestModel {

    @NotBlank
    private String name;
}
