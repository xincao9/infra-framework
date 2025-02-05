package com.github.xincao9.archetype.saas;

import com.github.xincao9.archetype.dto.Contributor;
import com.github.xincao9.archetype.dto.Issue;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface GitHub {

    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("POST /repos/{owner}/{repo}/issues")
    void createIssue(Issue issue, @Param("owner") String owner, @Param("repo") String repo);
}