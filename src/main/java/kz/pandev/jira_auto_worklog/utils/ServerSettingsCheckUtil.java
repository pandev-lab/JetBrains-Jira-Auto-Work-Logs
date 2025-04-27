package kz.pandev.jira_auto_worklog.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import kz.pandev.jira_auto_worklog.configs.ServerSettings;
import kz.pandev.jira_auto_worklog.factory.ServerSettingsFactory;
import kz.pandev.jira_auto_worklog.ui_dialogs.LoginPage;

import static kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog.getCurrentProject;

public class ServerSettingsCheckUtil {

    private ServerSettingsCheckUtil() {}

    private static volatile boolean isLoginPageCanceled = false;
    private static volatile boolean isSettingsWindowOpen = false;


    public static void checkIsConfigured() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Project project = getCurrentProject();
            if (project == null) return;
            Application app = ApplicationManager.getApplication();
            if (app.isUnitTestMode() || !app.isDispatchThread()) return;
            ServerSettings settings = ServerSettingsFactory.getInstance();
            if (settings == null || !settings.isConfigured()) {
                if (isLoginPageCanceled || isSettingsWindowOpen) return;
                LoginPage loginPage = new LoginPage(project);
                loginPage.promptForEmail();
            }
        });
    }


    public static void setIsLoginPageCanceled(boolean isLoginPageCanceled) {
        ServerSettingsCheckUtil.isLoginPageCanceled = isLoginPageCanceled;
    }

    public static boolean isIsSettingsWindowOpen() {
        return isSettingsWindowOpen;
    }

    public static void setIsSettingsWindowOpen(boolean isSettingsWindowOpen) {
        ServerSettingsCheckUtil.isSettingsWindowOpen = isSettingsWindowOpen;
    }
}

