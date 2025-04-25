package kz.pandev.plugins.autoworklog.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import kz.pandev.plugins.autoworklog.PanDevJiraAutoWorklog;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitInfoProvider {

    private static final String CHECKER_IS_GIT_FILE = "\\.git$";

    private GitInfoProvider() {
    }

    public static String[] getGitBranch(String path) {
        File projectDirectory = new File(Objects.requireNonNull(path));
        File gitFolder = findGitFolder(projectDirectory);

        if (gitFolder != null) {
            String repositoryName = getRepositoryPath(gitFolder);
            String branchName = getCurrentBranch(gitFolder);
            return new String[]{repositoryName, branchName};
        } else {
            return new String[0];
        }
    }

    public static File findGitFolder(File directory) {
        PanDevJiraAutoWorklog.log.info("Looking for directory: {}", directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && ".git".equals(file.getName())) {
                    PanDevJiraAutoWorklog.log.info("Found directory: {}", file);
                    return file;
                }
            }
        }
        return null;
    }

    public static String getRepositoryPath(File gitFolder) {
        File configFile = new File(gitFolder, "config");
        if (configFile.exists()) {
            try {
                String configContent = Files.readString(configFile.toPath());
                Pattern pattern = Pattern.compile("\\[remote \"origin\"\\]\\s+url = (.+)");
                Matcher matcher = pattern.matcher(configContent);
                if (matcher.find()) {
                    String remoteUrl = matcher.group(1);
                    return extractRepositoryPath(remoteUrl);
                }
            } catch (IOException e) {
                PanDevJiraAutoWorklog.log.error(e.getMessage());
            }
        }
        return null;
    }

    private static String extractRepositoryPath(String remoteUrl) {
        if (remoteUrl.startsWith("git@")) {
            return remoteUrl.replaceFirst("git@[^:]+:", "")
                    .replaceFirst(CHECKER_IS_GIT_FILE, "");
        } else if (remoteUrl.startsWith("https://")) {
            return remoteUrl.replaceFirst("https://[^/]+/", "")
                    .replaceFirst(CHECKER_IS_GIT_FILE, "");
        } else if (remoteUrl.startsWith("http://")) {
            return remoteUrl.replaceFirst("http://[^/]+/", "")
                    .replaceFirst(CHECKER_IS_GIT_FILE, "");
        } else if (remoteUrl.startsWith("ssh://")) {
            return remoteUrl.replaceFirst("ssh://[^/]+/", "")
                    .replaceFirst(CHECKER_IS_GIT_FILE, "");
        }
        return null;
    }

    public static String getCurrentBranch(File gitFolder) {

        try (Repository repository = new RepositoryBuilder().setGitDir(gitFolder).build()) {
            return repository.getBranch();
        } catch (IOException e) {
            PanDevJiraAutoWorklog.log.error(e.getMessage());
            return null;
        }
    }

    /**
     * Получает имя Git-репозитория заданного модуля.
     *
     * @param module экземпляр модуля IntelliJ IDEA
     * @return имя Git-репозитория
     */
    public static String getModuleGitBranch(Module module) {
        File moduleGitFile;
        VirtualFile virtualModuleFile = ProjectUtil.guessModuleDir(module);
        if (virtualModuleFile == null) {
            return null;
        }
        moduleGitFile = GitInfoProvider.findGitFolder(new File(virtualModuleFile.getPath()));
        if (moduleGitFile == null) {
            return null;
        }

        return getCurrentBranch(moduleGitFile);
    }
}