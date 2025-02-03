package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.properties.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("app")
@RestController
public class AppController {

    @Resource
    private AppProperties appProperties;
    @Value("${app.version}")
    private String version;

    @GetMapping
    public AppProperties get() {
        return this.appProperties;
    }

    @GetMapping("version")
    public String version() {
        return this.version;
    }
}
