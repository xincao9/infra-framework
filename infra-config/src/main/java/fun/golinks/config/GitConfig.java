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
    private String uri = "git@github.com:xincao9/infra-framework.git";

    /**
     * 目录
     */
    private String dir = "/tmp";

    /**
     * 远程
     */
    private String remote = "origin";

    /**
     * 分支
     */
    private String remoteBranchName = "main";
}
