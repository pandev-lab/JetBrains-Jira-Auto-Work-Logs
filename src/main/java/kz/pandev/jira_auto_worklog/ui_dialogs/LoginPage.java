package kz.pandev.jira_auto_worklog.ui_dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import kz.pandev.jira_auto_worklog.configs.ServerSettings;
import kz.pandev.jira_auto_worklog.factory.ServerSettingsFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginPage extends AbstractSettingsView {

    public LoginPage(@Nullable Project project) {
        super(project);
        setOKButtonText("Save");
        init();
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String urlPattern = "^https?://[a-zA-Z0-9-\\.]+(?:\\:\\d+)?$";
        Pattern compile = Pattern.compile(urlPattern);
        Matcher matcherUrl = compile.matcher(urlInput.getText());
        if (!matcherUrl.matches()) {
            return new ValidationInfo("Invalid url.", urlInput);
        }
        return super.doValidate();
    }

    @Override
    public void doOKAction() {
        ServerSettingsFactory.updateInstance(Map.of(
                        ServerSettings.URL_KEY, urlInput.getText(),
                        ServerSettings.USERNAME_KEY, isBasicAuthEnabled ? usernameInput.getText() : "",
                        ServerSettings.TOKEN_KEY, new String(passwordInput.getPassword())));
        super.doOKAction();
        showInformationDialog();
    }

    public void promptForEmail() {
        this.show();
    }
}