package com.github.xincao9.archetype.client;

import com.github.xincao9.archetype.client.dto.Contributor;
import feign.Param;
import feign.Request;
import feign.RequestLine;
import fun.golinks.core.annotate.FeignClient;

import java.util.List;

@FeignClient(baseUrl = "https://api.github.com")
public interface GitHubClient {

    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo, Request.Options options);
}