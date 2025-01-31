package fun.golinks.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GitRunner implements Runnable {

    private final GitConfig gitConfig;
    private final Git git;

    public GitRunner(GitConfig gitConfig) throws GitAPIException {
        this.gitConfig = gitConfig;
        this.git = createGit();
        pull();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(this, 30, TimeUnit.SECONDS);
    }

    private Git createGit() throws GitAPIException {
        try {
            Path path = Paths.get(gitConfig.getDir(), "/.git");
            Repository repository = new RepositoryBuilder().setGitDir(path.toFile()).readEnvironment().findGitDir()
                    .build();
            return new Git(repository);
        } catch (Throwable e1) {
            log.warn("git.findGitDir", e1);
            try {
                FileUtils.deleteDirectory(new File(gitConfig.getDir()));
            } catch (Throwable e2) {
                log.warn("deleteDirectory", e2);
            }
        }
        return cloneRepository();
    }

    private Git cloneRepository() throws GitAPIException {
        return Git.cloneRepository().setURI(gitConfig.getUri()).setDirectory(new File(gitConfig.getDir())).call();
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
