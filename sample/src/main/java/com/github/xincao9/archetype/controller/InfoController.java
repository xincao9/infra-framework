package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.properties.InfoProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("info")
@RestController
public class InfoController {

    @Resource
    private InfoProperties infoProperties;
    @Value("${info.version}")
    private String version;

    @GetMapping
    public InfoProperties get() {
        return this.infoProperties;
    }

    @GetMapping("version")
    public String version() {
        return this.version;
    }
}
