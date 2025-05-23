package kz.pandev.jira_auto_worklog.git.watcher;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Disposer;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;
import kz.pandev.jira_auto_worklog.clients.ApiClient;
import kz.pandev.jira_auto_worklog.factory.ServerSettingsFactory;
import kz.pandev.jira_auto_worklog.models.WorklogDto;
import kz.pandev.jira_auto_worklog.utils.IssueUtil;
import kz.pandev.jira_auto_worklog.widgets.PanDevStatusbarWidget;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;

/**
 * Сервис для отслеживания изменений в Git-репозитории проекта.
 */
@Service(Service.Level.PROJECT)
public final class GitCommitWatcher implements Disposable {

    private WatchService watchService;
    private String lastKnownCommit;
    private boolean running = true;

    private final Project project;
    private final Disposable disposable;
    private final Path gitLogPath;
    private final String projectBasePath;
    private final String projectName;

    private static final String NO_COMMIT = "NO_COMMIT";
    private static final String GIT_LOG_PATH = "/.git/logs/HEAD";

    /**
     * Конструктор {@link GitCommitWatcher}
     *
     * @param project проект, для которого инициализируется GitCommitWatcher
     */
    public GitCommitWatcher(Project project) {
        this.project = project;
        this.disposable = Disposer.newDisposable();
        this.projectBasePath = project.getBasePath();
        this.projectName = project.getName();
        this.gitLogPath = Paths.get(projectBasePath + GIT_LOG_PATH);
        this.lastKnownCommit = loadLastCommit();

        PanDevJiraAutoWorklog.log.info("GitWatcher initialized for project: {}", projectName);

        project.getMessageBus().connect().subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectClosed(@NotNull Project project) {
                PanDevJiraAutoWorklog.log.info("Project closed: {}", project.getName());
            }
        });
        startWatchingGitLogs();
    }

    /**
     * Начинает отслеживание изменений в Git-логах с использованием WatchService.
     */
    private void startWatchingGitLogs() {
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                initializeWatchService();
                startWatchThread();
                PanDevJiraAutoWorklog.log.info("WatchService started for project: {}", projectName);
                return;
            } catch (IOException e) {
                retryCount++;
                PanDevJiraAutoWorklog.log.error("Attempt {}/{}: Failed to start WatchService for project {}:" +
                                " message - {}, class - {}",
                        retryCount, maxRetries, projectName, e.getMessage(), e.getClass().getSimpleName());
                if (retryCount < maxRetries) {
                    long delay = (long) Math.pow(2, retryCount) * 1000L;
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        PanDevJiraAutoWorklog.log.error("Thread interrupted during sleep between retries: " +
                                "message - {}, class- {}", ie.getMessage(), ie.getClass().getName());
                        return;
                    }
                }
            }
        }
        PanDevJiraAutoWorklog.log.error("Failed to start WatchService for Git log file in project {} after {} attempts",
                projectName, maxRetries);
    }

    /**
     * Инициализирует WatchService и регистрирует каталог для отслеживания изменений.
     */
    private void initializeWatchService() throws IOException {
        if (watchService != null) {
            watchService.close();
        }
        watchService = FileSystems.getDefault().newWatchService();
        Path parentDir = gitLogPath.getParent();
        parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    /**
     * Запускает поток, который будет отслеживать изменения файла с помощью WatchService.
     */
    private void startWatchThread() {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            while (running) {
                try {
                    processWatchKey(watchService.take());
                } catch (InterruptedException e) {
                    PanDevJiraAutoWorklog.log.error("WatchService interrupted: message - {}, class - {}"
                            , e.getMessage(), e.getClass().getName());
                    Thread.currentThread().interrupt();
                    break;
                } catch (ClosedWatchServiceException e) {
                    PanDevJiraAutoWorklog.log.warn("WatchService closed unexpectedly, restarting...");
                    startWatchingGitLogs();
                    break;
                }
            }
        });
    }

    /**
     * Обрабатывает WatchKey, полученный от WatchService.
     *
     * @param key WatchKey
     */
    private void processWatchKey(WatchKey key) {
        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                Path modifiedFile = (Path) event.context();
                if (modifiedFile.equals(gitLogPath.getFileName())) {
                    PanDevJiraAutoWorklog.log.info("Detected changes in Git log file for project: {}",
                            projectName);
                    checkForNewCommits();
                }
            }
        }
        key.reset();
    }

    /**
     * Проверяет наличие новых коммитов в репозитории.
     */
    private void checkForNewCommits() {
        ApplicationManager.getApplication().invokeLater(() -> {
            try (Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(projectBasePath, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
                 Git git = new Git(repository)) {
                Iterator<RevCommit> logIterator = git.log().setMaxCount(1).call().iterator();
                if (logIterator.hasNext()) {
                    RevCommit latestCommit = logIterator.next();
                    String commitHash = latestCommit.getName();
                    if (!Objects.equals(commitHash, lastKnownCommit)) {
                        lastKnownCommit = commitHash;
                        handleNewCommit(latestCommit, repository.getBranch());
                        saveLastCommit(lastKnownCommit);
                    }
                }
            } catch (Exception e) {
                PanDevJiraAutoWorklog.log.error("Exception while checking for new commits in project {}: " +
                                "message- {}, class - {}",
                        projectName,e.getMessage(), e.getClass().getSimpleName());
            }
        });
    }

    /**
     * Останавливает отслеживание изменений в Git-логах.
     */
    public void stopWatchingGitLogs() {
        running = false; // Завершаем цикл
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            PanDevJiraAutoWorklog.log.error("Error closing WatchService: {}", e.getMessage(), e);
        }
    }

    /**
     * Сохраняет хэш последнего коммита.
     *
     * @param commitHash хэш коммита
     */
    private void saveLastCommit(String commitHash) {
        PanDevJiraAutoWorklog.log.info("Saving last commit to Git log in project {}: {}", projectName,
                commitHash);
        PropertiesComponent.getInstance(project).setValue(getProjectSpecificKey(), commitHash);
    }

    /**
     * Загружает хэш последнего коммита.
     *
     * @return хэш последнего коммита
     */
    private String loadLastCommit() {
        String lastCommit = PropertiesComponent.getInstance(project).getValue(getProjectSpecificKey(), NO_COMMIT);
        PanDevJiraAutoWorklog.log.info("Last commit from Git log for project {}: {}", projectName, lastCommit);
        return lastCommit;
    }

    /**
     * Возвращает ключ, специфичный для проекта, для сохранения последнего коммита.
     *
     * @return ключ для сохранения последнего коммита
     */
    private String getProjectSpecificKey() {
        String projectPath = projectBasePath;
        return "lastKnownCommit:" + (projectPath != null ? Base64.getEncoder().encodeToString(projectPath.getBytes()) :
                "default");
    }

    /**
     * Освобождает ресурсы.
     */
    @Override
    public void dispose() {
        stopWatchingGitLogs();
        Disposer.dispose(disposable);
    }

    /**
     * Обрабатывает новый коммит.
     *
     * @param commit коммит
     * @param branch ветка
     */
    private void handleNewCommit(RevCommit commit, String branch) {
        PanDevJiraAutoWorklog.log.info("Processing new commit for project {} branch {}", projectName, branch);

        String issueKey = IssueUtil.getIssueFromSourceBranch(branch);

        String complexKey = projectName + "|||" + branch;

        Map<String, Long> heartbeatsCache = PanDevJiraAutoWorklog.heartbeatsCache;
        long timeSpentSeconds = heartbeatsCache.get(complexKey);
        if (timeSpentSeconds < 60) timeSpentSeconds = 60;
        PanDevJiraAutoWorklog.log.info("Processing new commit for project {} branch {}", projectName, branch);
        PanDevJiraAutoWorklog.log.info("time {}", timeSpentSeconds);

        WorklogDto worklogDto = new WorklogDto();
        worklogDto.setTimeSpentSeconds(timeSpentSeconds);
        worklogDto.setComment(commit.getShortMessage());

        HttpResponse<String> response = ApiClient.sendWorklogRequest(ServerSettingsFactory.getInstance(),
                worklogDto, issueKey);

        if (response != null && response.statusCode() == 201) {
            heartbeatsCache.remove(complexKey);
            PanDevStatusbarWidget.updateTime(project,0);
        }
    }
}