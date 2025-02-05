package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.client.GitHubClient;
import com.github.xincao9.archetype.client.dto.Contributor;
import feign.Request;
import fun.golinks.core.annotate.FeginResource;
import fun.golinks.core.exception.FeignClientException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("github")
@RestController
public class GithubController {

    @FeginResource
    private GitHubClient gitHubClient;

    @GetMapping("contributors")
    public List<Contributor> contributors() throws FeignClientException {
        return gitHubClient.contributors("xincao9", "infra-framework", new Request.Options(1000, 100));
    }
}
