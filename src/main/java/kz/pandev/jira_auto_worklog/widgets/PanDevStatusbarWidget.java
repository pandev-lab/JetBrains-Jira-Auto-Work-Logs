package kz.pandev.jira_auto_worklog.widgets;

import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;
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
    public static final String WIDGET_ID = "PanDevAutoworklogStatusbarWidget";
    private final JLabel logoLabel;
    private JPanel panel;

    /**
     * Конструктор, инициализирующий Widget с заданным проектом.
     * */
    public PanDevStatusbarWidget() {
        this.logoLabel = new JLabel(getLogoIcon());
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
            panel.setToolTipText("Powered by PanDev");
            Color backgroundColor = JBUI.CurrentTheme.StatusBar.BACKGROUND;
            Color hoverColor = JBUI.CurrentTheme.StatusBar.Widget.HOVER_BACKGROUND;
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    PanDevJiraAutoWorklog.openDashboardWebsite("https://metrics.pandev.kz");
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
