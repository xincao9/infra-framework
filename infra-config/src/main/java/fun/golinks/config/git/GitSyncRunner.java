package fun.golinks.config.git;

import fun.golinks.config.ConfigConsts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private String appName;
    private String repo;
    private String dir;
    private String remote;
    private String remoteBranchName;

    private final List<Runnable> callbacks = new ArrayList<>();

    private static GitSyncRunner instance;

    public synchronized static GitSyncRunner start() throws Throwable {
        if (instance == null) {
            instance = new GitSyncRunner();
        }
        return instance;
    }

    /**
     * 构造器
     *
     * @throws GitAPIException git api异常
     * @throws IOException     io异常
     */
    private GitSyncRunner() throws Throwable {
        Map<String, String> configEnv = FileUtils.readConfig();
        if (configEnv.isEmpty()) {
            return;
        }
        this.uri = configEnv.get(GitConsts.INFRA_CONFIG_GIT_URI);
        String home = System.getenv("HOME");
        this.appName = configEnv.get(ConfigConsts.INFRA_CONFIG_APP_NAME);
        if (StringUtils.isAnyBlank(this.uri, this.appName)) {
            return;
        }
        this.repo = StringUtils.substringAfterLast(this.uri, "/");
        this.repo = StringUtils.substringBefore(this.repo, ".git");
        this.dir = configEnv.getOrDefault(GitConsts.INFRA_CONFIG_GIT_DIR, Paths.get(home, ".config").toString());
        this.remote = configEnv.getOrDefault(GitConsts.INFRA_CONFIG_GIT_REMOTE, "origin");
        this.remoteBranchName = configEnv.getOrDefault(GitConsts.INFRA_CONFIG_GIT_REMOTE_BRANCH_NAME, "main");
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
     * @throws GitAPIException git api异常
     * @throws IOException     io异常
     */
    private void init() throws GitAPIException, IOException {
        this.git = createGit();
        this.run();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor(r -> new Thread(r, "GitSyncRunner"));
        scheduledExecutorService.scheduleAtFixedRate(this, DELAY, DELAY, TimeUnit.SECONDS);
    }

    private Git createGit() throws GitAPIException, IOException {
        Path path = Paths.get(this.dir, this.appName, this.repo, ".git");
        if (path.toFile().exists()) {
            Repository repository = new RepositoryBuilder().setGitDir(path.toFile()).readEnvironment().findGitDir()
                    .build();
            return new Git(repository);
        }
        return cloneRepository();
    }

    /**
     * clone仓库
     *
     * @return git对象
     * @throws GitAPIException git api 异常
     */
    private Git cloneRepository() throws GitAPIException {
        Path path = Paths.get(this.dir, this.appName, this.repo);
        return Git.cloneRepository().setURI(this.uri).setDirectory(path.toFile()).setTimeout(TIMEOUT).call();
    }

    /**
     * 同步拉取
     *
     * @throws GitAPIException git api 异常
     */
    private PullResult pull() throws GitAPIException {
        return this.git.pull().setRemote(this.remote).setRemoteBranchName(this.remoteBranchName).call();
    }

    /**
     * run方法
     */
    @Override
    public void run() {
        try {
            PullResult pullResult = pull();
            if (pullResult.getMergeResult().getMergeStatus() == MergeResult.MergeStatus.FAST_FORWARD
                    && !this.callbacks.isEmpty()) {
                this.callbacks.forEach(Runnable::run);
            }
        } catch (GitAPIException e) {
            log.warn("GitSyncRunner failure!", e);
        } catch (Throwable e) {
            log.error("GitSyncRunner error!", e);
        }
    }

    public void add(Runnable callback) {
        this.callbacks.add(callback);
    }
}
