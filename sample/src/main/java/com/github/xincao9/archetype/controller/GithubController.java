package com.github.xincao9.archetype.controller;

import com.github.xincao9.archetype.dto.Contributor;
import com.github.xincao9.archetype.saas.GitHub;
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
    private GitHub gitHub;

    @GetMapping("contributors")
    public List<Contributor> contributors() throws FeignClientException {
        return gitHub.contributors("xincao9", "infra-framework");
    }
}
