package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.client.GitHubClient;
import com.github.xincao9.archetype.dto.ContributorDTO;
import feign.Request;
import fun.golinks.core.annotate.FeginResource;
import fun.golinks.core.exception.FeignClientException;
import fun.golinks.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 演示，调用三方http接口的方式
 */
@Slf4j
@RequestMapping("github")
@RestController
public class GithubController {

    @FeginResource
    private GitHubClient gitHubClient;

    @GetMapping("contributors")
    public String contributors() throws FeignClientException {
        List<ContributorDTO> contributorDTOS = gitHubClient.contributors("xincao9", "infra-framework",
                new Request.Options(1000, 500));
        return JsonUtils.toJsonString(contributorDTOS);
    }
}
