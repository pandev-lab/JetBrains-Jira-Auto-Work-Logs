package kz.pandev.jira_auto_worklog.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import kz.pandev.jira_auto_worklog.configs.ServerSettings;
import kz.pandev.jira_auto_worklog.factory.ServerSettingsFactory;
import kz.pandev.jira_auto_worklog.ui_dialogs.LoginPage;
import kz.pandev.jira_auto_worklog.ui_dialogs.Settings;

/**
 * Класс, представляющий действие для открытия настроек плагина PanDev Jira Auto Worklog из меню.
 */
public class Menu extends AnAction {

    /**
     * Конструктор класса PluginMenu.
     * Устанавливает название действия в меню.
     */
    public Menu() {
        super("PanDev Jira Auto Worklog Settings");
    }

    /**
     * Метод, который выполняется при выборе действия в меню.
     * Открывает окно настроек плагина.
     *
     * @param e событие, которое инициировало действие
     */
    public void actionPerformed(AnActionEvent e) {
            Project project = e.getProject();
            ServerSettings settings = ServerSettingsFactory.getInstance();
            if (settings.getUrl() == null) {
            LoginPage loginPopup = new LoginPage(project);
            loginPopup.show();}
            else {
                Settings settingsPage = new Settings(project, settings);
                settingsPage.show();
            }
    }
}
