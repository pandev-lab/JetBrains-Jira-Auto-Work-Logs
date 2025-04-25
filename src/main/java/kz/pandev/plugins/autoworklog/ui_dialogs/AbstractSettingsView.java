package kz.pandev.plugins.autoworklog.ui_dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import kz.pandev.plugins.autoworklog.clients.ApiClient;
import kz.pandev.plugins.autoworklog.utils.ServerSettingsCheckUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.InputStream;

public class AbstractSettingsView extends DialogWrapper {

    protected final JPanel mainPanel;
    protected final JPanel titlePanel;
    protected final JPanel userInfoFormPanel;

    protected JLabel urlLabel;
    protected JTextField urlInput;
    protected JLabel passwordLabel;
    protected JPasswordField passwordInput;
    protected JCheckBox basicAuthCheckbox;
    protected JLabel usernameLabel;
    protected JTextField usernameInput;
    protected JPanel reportProblemLinkPanel;
    protected boolean isBasicAuthEnabled;

    protected AbstractSettingsView(@Nullable Project project) {
        super(project, true);
        mainPanel = new JPanel(new BorderLayout());
        setSize(700, 400);
        titlePanel = getTitlePanel();

        userInfoFormPanel = getUserInfoFormPenal(getDocumentListener());

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

        usernameLabel = new JLabel("Username");
        usernameInput = new JTextField();
        usernameLabel.setBorder(JBUI.Borders.emptyTop(5));
        usernameInput.getDocument().addDocumentListener(documentListener);
        usernameLabel.setVisible(false); // Initially hidden
        usernameInput.setVisible(false); // Initially hidden
        userInfoOuterPanel.add(usernameLabel);
        userInfoOuterPanel.add(usernameInput);

        passwordLabel = new JLabel("Token");
        passwordInput = new JPasswordField();
        passwordInput.getDocument().addDocumentListener(documentListener);
        passwordLabel.setBorder(JBUI.Borders.emptyTop(5));
        userInfoOuterPanel.add(passwordLabel);
        userInfoOuterPanel.add(passwordInput);

        JPanel basicAuthPanel = new JPanel();
        basicAuthPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Centered horizontally
        JLabel basicAuthLabel = new JLabel("Basic Auth");
        basicAuthCheckbox = new JCheckBox();
        basicAuthCheckbox.addItemListener(e -> {
            isBasicAuthEnabled = e.getStateChange() == ItemEvent.SELECTED;
            if (isBasicAuthEnabled) {
                passwordLabel.setText("Password");
                usernameLabel.setVisible(true);
                usernameInput.setVisible(true);
                passwordInput.setVisible(true);
            } else {
                passwordLabel.setText("Token");
                usernameLabel.setVisible(false);
                usernameInput.setVisible(false);
                passwordInput.setVisible(true);
            }
            userInfoOuterPanel.revalidate();
            userInfoOuterPanel.repaint();
        });
        basicAuthPanel.add(basicAuthLabel);
        basicAuthPanel.add(basicAuthCheckbox);
        userInfoOuterPanel.add(basicAuthPanel);

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

        JLabel titleLabel = new JLabel("PanDev Autoworklog");
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

    private DocumentListener getDocumentListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                setErrorText(null);
                getOKAction().setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                insertUpdate(documentEvent);
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                insertUpdate(documentEvent);
            }
        };
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
        String username = isBasicAuthEnabled ? usernameInput.getText().trim() : null;
        if (!ApiClient.validateHost(urlInput.getText(), username, new String(passwordInput.getPassword()))) {
            return new ValidationInfo("Invalid host or password." +
                    (isBasicAuthEnabled ? " Please check your username and password." : ""));
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
