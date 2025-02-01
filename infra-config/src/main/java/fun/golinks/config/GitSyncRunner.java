package fun.golinks.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.springframework.beans.factory.annotation.Value;

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
    private static final int DELAY = 30;
    private final GitConfig gitConfig;
    private final String application;
    private Git git;

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
    public GitSyncRunner(GitConfig gitConfig, @Value("${spring.application.name}") String application)
            throws Throwable {
        this.gitConfig = gitConfig;
        this.application = application;
        Path path = Paths.get(gitConfig.getDir(), application);
        boolean r = path.toFile().mkdirs();
        if (r) {
            log.info("create dir {} success!", path);
        }
        init(gitConfig);
    }

    /**
     * 初始化方法
     *
     * @param gitConfig
     *            git配置类
     * 
     * @throws GitAPIException
     *             git api异常
     * @throws IOException
     *             io异常
     */
    private void init(GitConfig gitConfig) throws GitAPIException, IOException {
        String repo = StringUtils.substringAfterLast(gitConfig.getUri(), "/");
        repo = StringUtils.substringBefore(repo, ".git");
        this.git = createGit(repo);
        this.run();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor(r -> new Thread(r, "GitSyncRunner"));
        scheduledExecutorService.scheduleAtFixedRate(this, DELAY, DELAY, TimeUnit.SECONDS);
        String configDir = Paths.get(gitConfig.getDir(), application).toString();
        System.setProperty("spring.config.additional-location", "file:" + configDir);
    }

    private Git createGit(String repo) throws GitAPIException, IOException {
        Path path = Paths.get(gitConfig.getDir(), application, repo, ".git");
        log.info("path = {}", path);
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
        Path path = Paths.get(gitConfig.getDir(), application, repo);
        return Git.cloneRepository().setURI(gitConfig.getUri()).setDirectory(path.toFile()).setTimeout(TIMEOUT).call();
    }

    /**
     * 同步拉取
     *
     * @throws GitAPIException
     *             git api 异常
     */
    private PullResult pull() throws GitAPIException {
        return git.pull().setRemote(gitConfig.getRemote()).setRemoteBranchName(gitConfig.getRemoteBranchName()).call();
    }

    /**
     * run方法
     */
    @Override
    public void run() {
        try {
            PullResult pullResult = pull();
            log.info("GitSyncRunner gitConfig {} pullResult {}", gitConfig, pullResult);
        } catch (GitAPIException e) {
            log.warn("GitSyncRunner gitConfig {} failure!", gitConfig, e);
        } catch (Throwable e) {
            log.error("GitSyncRunner gitConfig {} error!", gitConfig, e);
        }
    }
}
