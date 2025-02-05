package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.dto.Contributor;
import com.github.xincao9.archetype.saas.GitHub;
import fun.golinks.core.exception.FeignClientException;
import fun.golinks.core.feign.FeignProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("github")
@RestController
public class GithubController {

    @Resource
    private FeignProxy feignProxy;

    @GetMapping("contributors")
    public List<Contributor> contributors() throws FeignClientException {
        return feignProxy.getOrCreate(GitHub.class).contributors("xincao9", "infra-framework");
    }
}
