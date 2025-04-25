package kz.pandev.plugins.autoworklog.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import kz.pandev.plugins.autoworklog.configs.ServerSettings;
import kz.pandev.plugins.autoworklog.factory.ServerSettingsFactory;
import kz.pandev.plugins.autoworklog.ui_dialogs.LoginPage;

import static kz.pandev.plugins.autoworklog.PanDevJiraAutoWorklog.getCurrentProject;

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

    public static boolean isIsLoginPageCanceled() {
        return isLoginPageCanceled;
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

