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

@Slf4j
public class GitRunner implements Runnable {

    private static final int TIMEOUT = 300;
    private final GitConfig gitConfig;
    private final Git git;

    public GitRunner(GitConfig gitConfig) throws GitAPIException, IOException {
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

    private Git cloneRepository(String repo) throws GitAPIException {
        Path path = Paths.get(gitConfig.getDir(), repo);
        return Git.cloneRepository().setURI(gitConfig.getUri()).setDirectory(path.toFile()).setTimeout(TIMEOUT).call();
    }

    private void pull() throws GitAPIException {
        git.pull().setRemote("origin").setRemoteBranchName("main").call();
    }

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
