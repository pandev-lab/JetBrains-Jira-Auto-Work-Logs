package kz.pandev.jira_auto_worklog.ui_dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import kz.pandev.jira_auto_worklog.clients.ApiClient;
import kz.pandev.jira_auto_worklog.utils.ServerSettingsCheckUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class AbstractSettingsView extends DialogWrapper {

    protected final JPanel mainPanel;
    protected final JPanel titlePanel;
    protected final JPanel userInfoFormPanel;

    protected JLabel urlLabel;
    protected JTextField urlInput;
    protected JLabel tokenLabel;
    protected JPasswordField tokenInput;
    protected JLabel usernameLabel;
    protected JTextField usernameInput;
    protected JPanel reportProblemLinkPanel;
    protected JButton generateTokenBtn;

    protected AbstractSettingsView(@Nullable Project project) {
        super(project, true);
        mainPanel = new JPanel(new BorderLayout());
        setSize(700, 400);
        titlePanel = getTitlePanel();

        userInfoFormPanel = getUserInfoFormPenal(getDocumentListener());
        updateGenerateBtnState();
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

        JPanel companyInfoLinkPanel = new JPanel();
        LinkPane companyLink = new LinkPane("https://pandev.kz/", "Powered by PanDev");
        companyInfoLinkPanel.add(companyLink);

        reportProblemLinkPanel = new JPanel();
        LinkPane reportProblemLink = new LinkPane("https://t.me/pandev_metrics_support_bot",
                "Report a Bug or Suggestion");
        reportProblemLinkPanel.add(reportProblemLink);

        companyInfoLinkPanel.setOpaque(false);
        reportProblemLinkPanel.setOpaque(false);

        outerPanel.add(companyInfoLinkPanel);
        outerPanel.add(reportProblemLinkPanel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(userInfoFormPanel, BorderLayout.CENTER);
        mainPanel.add(outerPanel, BorderLayout.SOUTH);
        ServerSettingsCheckUtil.setIsSettingsWindowOpen(true);
    }

    private JPanel getUserInfoFormPenal(DocumentListener documentListener) {
        JPanel userInfoOuterPanel = new JPanel(new GridLayout(0, 1));
        userInfoOuterPanel.setBorder(JBUI.Borders.empty(5, 150));

        urlLabel = new JLabel("URL");
        urlInput = new JTextField();
        urlLabel.setBorder(JBUI.Borders.emptyTop(5));
        urlInput.getDocument().addDocumentListener(documentListener);
        userInfoOuterPanel.add(urlLabel);
        userInfoOuterPanel.add(urlInput);
        urlInput.setToolTipText("Enter your Jira server URL, e.g., https://jira.example.com");

        usernameLabel = new JLabel("Username");
        usernameInput = new JTextField();
        usernameLabel.setBorder(JBUI.Borders.emptyTop(5));
        usernameInput.getDocument().addDocumentListener(documentListener);
        usernameInput.setToolTipText("Enter your Jira username");

        userInfoOuterPanel.add(usernameLabel);
        userInfoOuterPanel.add(usernameInput);

        tokenLabel = new JLabel("Token");
        tokenInput = new JPasswordField();
        tokenInput.getDocument().addDocumentListener(documentListener);
        tokenLabel.setBorder(JBUI.Borders.emptyTop(5));
        tokenInput.setToolTipText("Enter your Jira API token");

        generateTokenBtn = new JButton("Generate Token");
        generateTokenBtn.setEnabled(false);
        generateTokenBtn.addActionListener(e -> openGeneratePage());

        userInfoOuterPanel.add(tokenLabel);
        userInfoOuterPanel.add(tokenInput);
        userInfoOuterPanel.add(generateTokenBtn);
        userInfoOuterPanel.revalidate();
        userInfoOuterPanel.repaint();

        return userInfoOuterPanel;
    }

    protected JPanel getTitlePanel() {
        JPanel panel = new JPanel();

        JLabel imageLabel;
        try {
            InputStream inputStream = getLogoImage();
            var img = ImageIO.read(inputStream);
            Image dimg = img.getScaledInstance(28, 36,
                    Image.SCALE_SMOOTH);
            var imageIcon = new ImageIcon(dimg);
            imageLabel = new JLabel(imageIcon);
        } catch (IOException e) {
            imageLabel = new JLabel(e.getMessage());
        }

        JLabel titleLabel = new JLabel("PanDev Jira Auto Worklog");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        imageLabel.setBorder(JBUI.Borders.emptyRight(5));

        panel.add(imageLabel);
        panel.add(titleLabel);

        panel.setBorder(JBUI.Borders.empty(10, 0));
        return panel;
    }

    private static @NotNull InputStream getLogoImage() throws IOException {
        String currentTheme = JBColor.isBright() ? "light" : "dark";

        ClassLoader classLoader = AbstractSettingsView.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("images/icons/logo-" + currentTheme + "-theme.png");

        if (inputStream == null) {
            throw new IOException("Could not find logo-dark-theme.png");
        }
        return inputStream;
    }
    private void updateGenerateBtnState() {
        boolean enabled = !urlInput.getText().trim().isBlank();
        generateTokenBtn.setEnabled(enabled);
    }
    private DocumentListener getDocumentListener() {
        return new DocumentListener() {
            private void changed() {
                setErrorText(null);
                getOKAction().setEnabled(true);
                updateGenerateBtnState();
            }
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                setErrorText(null);
                getOKAction().setEnabled(true);
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                insertUpdate(documentEvent);
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                insertUpdate(documentEvent);
                changed();
            }
        };
    }
    private void openGeneratePage() {
        String url = urlInput.getText().trim();
        if (url.isBlank()) return;        // безопасность

        boolean cloud = ApiClient.isJiraCloudHost(url);   // публичный метод
        String link = cloud
                ? "https://id.atlassian.com/manage-profile/security/api-tokens"
                : url.replaceAll("/+$", "")
                + "/secure/ViewProfile.jspa?selectedPage=personal-access-tokens";

        com.intellij.ide.BrowserUtil.browse(link);
    }
    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected boolean continuousValidation() {
        return false;
    }

    @Override
    public void doCancelAction() {
        ServerSettingsCheckUtil.setIsLoginPageCanceled(true);
        ServerSettingsCheckUtil.setIsSettingsWindowOpen(false);
        super.doCancelAction();
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String username = usernameInput.getText().trim();
        try {
        ApiClient.validateHost(urlInput.getText(), username, new String(tokenInput.getPassword()));
        }
        catch (Exception e) {
            return new ValidationInfo(e.getMessage());
        }
        return null;
    }

    @Override
    public void doOKAction() {
        ServerSettingsCheckUtil.setIsSettingsWindowOpen(false);
        super.doOKAction();
    }

    public static void showInformationDialog() {
        InformationDialog successDialog = new InformationDialog();
        successDialog.show();
    }
}
