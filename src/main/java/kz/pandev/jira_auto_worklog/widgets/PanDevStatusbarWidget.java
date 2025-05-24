package kz.pandev.jira_auto_worklog.widgets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;
import kz.pandev.jira_auto_worklog.configs.ServerSettings;
import kz.pandev.jira_auto_worklog.factory.ServerSettingsFactory;
import kz.pandev.jira_auto_worklog.models.HeartbeatManager;
import kz.pandev.jira_auto_worklog.ui_dialogs.LoginPage;
import kz.pandev.jira_auto_worklog.ui_dialogs.Settings;
import kz.pandev.jira_auto_worklog.utils.GitInfoProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Сущность показа StatusBar, подробнее <a href="https://plugins.jetbrains.com/docs/intellij/status-bar-widgets.html">
 * Jetbrains plugins docs</a>
 */
public class PanDevStatusbarWidget implements CustomStatusBarWidget {

    public static final String WIDGET_ID = "PanDevAutoWorklogStatusbarWidget";
    private JPanel panel;
    private static volatile PanDevStatusbarWidget INSTANCE;
    private final Project project;
    private final JLabel logoLabel = new JLabel(getLogoIcon());
    private final JLabel timeLabel = new JLabel();

    /**
     * Конструктор, инициализирующий Widget с заданным проектом.
     * */
    public PanDevStatusbarWidget(Project project) {
        this.project = project;
        com.intellij.openapi.application.ApplicationManager
                .getApplication()
                .getService(kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog.class);
        INSTANCE = this;
        String[] gitInfo = GitInfoProvider.getGitBranch(project.getBasePath());
        String branch = gitInfo.length > 1 ? gitInfo[1] : null;
        HeartbeatManager mgr = project.getService(HeartbeatManager.class);
        long sec = project.getService(HeartbeatManager.class).forBranch(branch);
        if (sec == 0 && branch != null) {
                   String key = project.getName() + "|||" + branch;
                   sec = PanDevJiraAutoWorklog.heartbeatsCache.getOrDefault(key, 0L);
                   if (sec > 0) mgr.add(branch, sec);
              }
        setTime(sec);
    }

    private void updateAuthState() {
        ServerSettings settings = ServerSettingsFactory.getInstance();
        boolean configured = !StringUtils.isBlank(settings.getUrl())
                && !StringUtils.isBlank(settings.getToken());

        long cachedSeconds = 0;
        if (configured) {
            cachedSeconds = PanDevJiraAutoWorklog.heartbeatsCache.values()
                    .stream()
                    .mapToLong(Long::longValue)
                    .sum();
        }

        String text = configured
                ? kz.pandev.jira_auto_worklog.utils.TimeFormatUtil.pretty(cachedSeconds)
                : "";

        SwingUtilities.invokeLater(() -> {
            timeLabel.setVisible(configured);
            timeLabel.setText(text);
        });
    }
    /**
     * Получает главный компонент Widget-та.
     *
     * @return главный компонент JComponent
     */
    @Override
    public JComponent getComponent() {
        if (panel == null) {
            panel = new JPanel();
            panel.add(logoLabel);
            panel.add(timeLabel);
            panel.setToolTipText("Powered by PanDev");
            Color backgroundColor = JBUI.CurrentTheme.StatusBar.BACKGROUND;
            Color hoverColor = JBUI.CurrentTheme.StatusBar.Widget.HOVER_BACKGROUND;
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Project project = PanDevJiraAutoWorklog.getCurrentProject();
                    if (project == null) return;

                    ServerSettings settings = ServerSettingsFactory.getInstance();
                    if (settings.getUrl() == null) {
                        new LoginPage(project).promptForEmail();
                    } else {
                        new Settings(project, settings).show();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    panel.setBackground(hoverColor);
                    super.mouseEntered(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    panel.setBackground(backgroundColor);
                    super.mouseExited(e);
                }
            });
        }
        return panel;
    }

    public static void refresh() {
        StatusBar sb = WindowManager.getInstance().getStatusBar(INSTANCE.project);
        if (sb == null) return;

        StatusBarWidget w = sb.getWidget(WIDGET_ID);
        if (w instanceof PanDevStatusbarWidget widget) {
            widget.updateAuthState();
        }
    }
    public static void updateTime(Project p, long sec) {
        StatusBar sb = WindowManager.getInstance().getStatusBar(p);
        if (sb == null) return;
        StatusBarWidget w = sb.getWidget(WIDGET_ID);
        if (w instanceof PanDevStatusbarWidget pw) pw.setTime(sec);
    }
    private void setTime(long sec) {
        String txt = kz.pandev.jira_auto_worklog.utils.TimeFormatUtil.pretty(sec);
        SwingUtilities.invokeLater(() -> timeLabel.setText(txt));
    }
    /**
     * Получает уникальный идентификатор виджета.
     *
     * @return уникальный идентификатор виджета
     */
    @NotNull
    @Override
    public String ID() {
        return WIDGET_ID;
    }

    /**
     * Получает иконку плагина для статус-бара
     * @return иконку
     */
    public Icon getLogoIcon() {
        String theme = JBColor.isBright() ? "light" : "dark";
        return IconLoader.getIcon("images/icons/status-bar-icon-" + theme + "-theme.svg",
                PanDevJiraAutoWorklog.class);
    }
}
