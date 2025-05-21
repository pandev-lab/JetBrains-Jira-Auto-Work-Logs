package kz.pandev.jira_auto_worklog.widgets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;
import kz.pandev.jira_auto_worklog.configs.ServerSettings;
import kz.pandev.jira_auto_worklog.factory.ServerSettingsFactory;
import kz.pandev.jira_auto_worklog.ui_dialogs.LoginPage;
import kz.pandev.jira_auto_worklog.ui_dialogs.Settings;
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
    private final JLabel logoLabel;
    private JPanel panel;

    private final JLabel timeLabel;

    private static volatile PanDevStatusbarWidget INSTANCE;


    /**
     * Конструктор, инициализирующий Widget с заданным проектом.
     * */
    public PanDevStatusbarWidget() {
        INSTANCE = this;
        this.logoLabel = new JLabel(getLogoIcon());
        this.timeLabel = new JLabel("0 s");
        updateAuthState();
    }

    private void updateAuthState() {
        ServerSettings settings = ServerSettingsFactory.getInstance();
        boolean configured = settings.getUrl() != null && !settings.getUrl().isBlank()
                && settings.getToken() != null && !settings.getToken().isBlank();

        SwingUtilities.invokeLater(() -> {
            timeLabel.setVisible(configured);
            timeLabel.setText(configured ? "0s" : "");

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
        if (INSTANCE != null) INSTANCE.updateAuthState();
    }
    public static void updateTime(long seconds) {
        if (INSTANCE == null){
            System.out.println("[PanDev] updateTime: INSTANCE==null");
            return;}
        String text = kz.pandev.jira_auto_worklog.utils.TimeFormatUtil.pretty(seconds);
        SwingUtilities.invokeLater(() -> INSTANCE.timeLabel.setText(text));
        System.out.println("[PanDev] updateTime -> " + text);
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
