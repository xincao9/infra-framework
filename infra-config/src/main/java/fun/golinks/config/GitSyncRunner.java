package fun.golinks.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * git 远程同步runner
 */
@Slf4j
public class GitSyncRunner implements Runnable {

    private static final int TIMEOUT = 600;
    private static final int DELAY = 30;
    private Git git;
    private String uri;
    private String dir;
    private String appName;
    private String remote;
    private String remoteBranchName;

    /**
     * 构造器
     *
     * @throws GitAPIException
     *             git api异常
     * @throws IOException
     *             io异常
     */
    public GitSyncRunner() throws Throwable {
        Map<String, String> configEnv = FileUtils.readConfig();
        if (configEnv.isEmpty()) {
            return;
        }
        boolean enabled = Boolean.parseBoolean(configEnv.get(ConfigConsts.INFRA_CONFIG_ENABLED));
        if (!enabled) {
            return;
        }
        String type = configEnv.get(ConfigConsts.INFRA_CONFIG_TYPE);
        if (!Objects.equals(type, ConfigConsts.GIT) && StringUtils.isNotBlank(type)) {
            return;
        }
        this.uri = configEnv.get(ConfigConsts.INFRA_CONFIG_GIT_URI);
        String home = System.getenv("HOME");
        this.dir = configEnv.getOrDefault(ConfigConsts.INFRA_CONFIG_GIT_DIR, Paths.get(home, ".config").toString());
        this.appName = configEnv.get(ConfigConsts.INFRA_CONFIG_APP_NAME);
        if (StringUtils.isAnyBlank(this.uri, this.dir, this.appName)) {
            return;
        }
        this.remote = configEnv.getOrDefault(ConfigConsts.INFRA_CONFIG_GIT_REMOTE, "origin");
        this.remoteBranchName = configEnv.getOrDefault(ConfigConsts.INFRA_CONFIG_GIT_REMOTE_BRANCH_NAME, "main");
        Path path = Paths.get(this.dir, this.appName);
        boolean r = path.toFile().mkdirs();
        if (r) {
            log.info("create dir {} success!", path);
        }
        init();
    }

    /**
     * 初始化方法
     *
     * @throws GitAPIException
     *             git api异常
     * @throws IOException
     *             io异常
     */
    private void init() throws GitAPIException, IOException {
        String repo = StringUtils.substringAfterLast(this.uri, "/");
        repo = StringUtils.substringBefore(repo, ".git");
        this.git = createGit(repo);
        this.run();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor(r -> new Thread(r, "GitSyncRunner"));
        scheduledExecutorService.scheduleAtFixedRate(this, DELAY, DELAY, TimeUnit.SECONDS);
    }

    private Git createGit(String repo) throws GitAPIException, IOException {
        Path path = Paths.get(this.dir, this.appName, repo, ".git");
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
        Path path = Paths.get(this.dir, this.appName, repo);
        return Git.cloneRepository().setURI(this.uri).setDirectory(path.toFile()).setTimeout(TIMEOUT).call();
    }

    /**
     * 同步拉取
     *
     * @throws GitAPIException
     *             git api 异常
     */
    private PullResult pull() throws GitAPIException {
        return git.pull().setRemote(remote).setRemoteBranchName(remoteBranchName).call();
    }

    /**
     * run方法
     */
    @Override
    public void run() {
        try {
            PullResult pullResult = pull();
            log.info("GitSyncRunner pullResult {}", pullResult);
        } catch (GitAPIException e) {
            log.warn("GitSyncRunner failure!", e);
        } catch (Throwable e) {
            log.error("GitSyncRunner error!", e);
        }
    }
}
