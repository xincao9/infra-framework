package com.github.xincao9.archetype.client;

import com.github.xincao9.archetype.client.dto.Contributor;
import feign.Param;
import feign.Request;
import feign.RequestLine;
import fun.golinks.core.annotate.FeignClient;

import java.util.List;

/**
 * 演示，调用三方http接口的方式
 */
@FeignClient(baseUrl = "https://api.github.com")
public interface GitHubClient {

    /**
     * 查看贡献者
     *
     * @param owner
     *            拥有者
     * @param repo
     *            仓库
     * @param options
     *            不修改默认调用参数时，可以不用这个参数
     * 
     * @return 贡献者
     */
    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo, Request.Options options);
}