package fun.golinks.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * git 远程同步runner
 */
@Slf4j
public class GitSyncRunner implements Runnable {

    private static final int TIMEOUT = 300;
    private final GitConfig gitConfig;
    private final Git git;

    /**
     * 构造器
     *
     * @param gitConfig
     *            git配置类
     * 
     * @throws GitAPIException
     *             git api异常
     * @throws IOException
     *             io异常
     */
    public GitSyncRunner(GitConfig gitConfig) throws GitAPIException, IOException {
        this.gitConfig = gitConfig;
        this.git = createGit();
        pull();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(this, 30, TimeUnit.SECONDS);
    }

    private Git createGit() throws GitAPIException, IOException {
        String repo = StringUtils.substringAfterLast(gitConfig.getUri(), "/");
        repo = StringUtils.substringBefore(repo, ".git");
        Path path = Paths.get(gitConfig.getDir(), repo, ".git");
        if (path.toFile().exists()) {
            Repository repository = new RepositoryBuilder().setGitDir(path.toFile()).readEnvironment().findGitDir()
                    .build();
            return new Git(repository);
        }
        return cloneRepository(repo);
    }

    /**
     * clone仓库
     *
     * @param repo
     *            远程仓库
     * 
     * @return git对象
     * 
     * @throws GitAPIException
     *             git api 异常
     */
    private Git cloneRepository(String repo) throws GitAPIException {
        Path path = Paths.get(gitConfig.getDir(), repo);
        return Git.cloneRepository().setURI(gitConfig.getUri()).setDirectory(path.toFile()).setTimeout(TIMEOUT).call();
    }

    /**
     * 同步拉取
     * 
     * @throws GitAPIException
     *             git api 异常
     */
    private void pull() throws GitAPIException {
        git.pull().setRemote(gitConfig.getRemote()).setRemoteBranchName(gitConfig.getRemoteBranchName()).call();
    }

    /**
     * run方法
     */
    @Override
    public void run() {
        try {
            pull();
            log.info("git {} pull success", gitConfig);
        } catch (GitAPIException e) {
            log.warn("git {} pull failure", gitConfig);
        } catch (Throwable e) {
            log.error("git {} pull error", gitConfig);
        }
    }
}
