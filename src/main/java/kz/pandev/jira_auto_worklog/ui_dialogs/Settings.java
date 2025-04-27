package kz.pandev.jira_auto_worklog.ui_dialogs;

import com.intellij.openapi.project.Project;
import kz.pandev.jira_auto_worklog.configs.ServerSettings;
import kz.pandev.jira_auto_worklog.factory.ServerSettingsFactory;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class Settings extends AbstractSettingsView {

    public Settings(@Nullable Project project, ServerSettings serverSettings) {
        super(project);
        setTitle("Settings");
        setOKButtonText("Save");

        urlInput.setVisible(true);
        urlInput.setText(serverSettings.getUrl());


        boolean isBasicAuth = StringUtils.isNotEmpty(serverSettings.getUsername());
        basicAuthCheckbox.setSelected(isBasicAuth);
        usernameInput.setText(serverSettings.getUsername());

        init();
    }

    @Override
    public void doOKAction() {
        ServerSettingsFactory.updateInstance(Map.of(
                        ServerSettings.URL_KEY, urlInput.getText(),
                        ServerSettings.USERNAME_KEY,  isBasicAuthEnabled ? usernameInput.getText() : "",
                        ServerSettings.TOKEN_KEY,  new String(passwordInput.getPassword())
                )
        );
        super.doOKAction();
        showInformationDialog();
    }
}
