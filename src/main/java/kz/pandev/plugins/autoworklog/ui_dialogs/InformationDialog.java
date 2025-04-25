package kz.pandev.plugins.autoworklog.ui_dialogs;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class InformationDialog {
    private static final String TITLE = "PanDev Jira Auto Worklog - Information";
    private JRootPane rootPane;
    private String[] options;
    private int optionIndex;

    public InformationDialog() {
        setRootPane(null);
        setOptions(new String[]{"OK"});
        setOptionSelection(0);
    }

    public void setRootPane(JRootPane rootPane) {
        this.rootPane = rootPane;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public void setOptionSelection(int optionIndex) {
        this.optionIndex = optionIndex;
    }

    public void show() {
        int optionType = JOptionPane.OK_CANCEL_OPTION;
        Object optionSelection = options.length != 0 ? options[optionIndex] : null;

        JLabel header = new JLabel("<html><div style='font-size:14pt; font-weight:bold; margin-bottom:10px;'>" +
                "PanDev Autoworklog Plugin</div></html>");

        JTextPane message = new JTextPane();
        message.setContentType("text/html");
        message.setText("<html><div style='width:300px;'>" +
                "<p style='margin-top:0; margin-bottom:10px;'>This plugin automatically tracks your development time:</p>" +
                "<ul style='margin-top:0; margin-bottom:10px;'>" +
                "<li style='margin-bottom:5px;'>Extracts task key from branch name</li>" +
                "<li style='margin-bottom:5px;'>Logs work time to Jira</li>" +
                "<li style='margin-bottom:5px;'>Uses actual commit development time</li>" +
                "</ul>" +
                "<p style='font-style:italic; margin-bottom:0;'>Example branch: <b>PROJ-123-feature</b></p>" +
                "<p style='font-style:italic; margin-top:10px; margin-bottom:0;'>This plugin is designed specifically " +
                "for integration with Jira to log work time.</p>" +
                "<p style='font-style:italic; margin-top:10px; margin-bottom:0;'>Please note: This is an independent " +
                "tool and is not officially affiliated with Atlassian.</p>" +
                "</div></html>");
        message.setEditable(false);
        message.setBackground(UIManager.getColor("Panel.background"));
        message.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        List<JComponent> allComponents = new ArrayList<>();
        allComponents.add(header);
        allComponents.add(message);

        JOptionPane.showOptionDialog(rootPane,
                allComponents.toArray(),
                TITLE,
                optionType,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                optionSelection);
    }
}