package fun.golinks.config;

import lombok.Data;
import lombok.ToString;

/**
 * git 配置
 */
@Data
@ToString
public class GitConfig {

    /**
     * 地址
     */
    private String uri = "git@github.com:xincao9/sample-config-repo.git";

    /**
     * 目录
     */
    private String dir = "${HOME}/.config";

    /**
     * 远程
     */
    private String remote = "origin";

    /**
     * 分支
     */
    private String remoteBranchName = "main";
}
