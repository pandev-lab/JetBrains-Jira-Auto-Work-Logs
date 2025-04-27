package kz.pandev.jira_auto_worklog.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;

public class CaretListenerImpl implements com.intellij.openapi.editor.event.CaretListener {
    @Override
    public void caretPositionChanged(CaretEvent event) {
        try {
            if (PanDevJiraAutoWorklog.isAppInactive()) return;
            Editor editor = event.getEditor();
            Document document = editor.getDocument();
            VirtualFile file = PanDevJiraAutoWorklog.getVirtualFile(document);
            if (file == null) return;
            Project project = editor.getProject();
            if (PanDevJiraAutoWorklog.isProjectUninitialized(project)) return;
            ApplicationManager.getApplication().invokeLater(() ->
                PanDevJiraAutoWorklog.appendHeartbeat(file, project, false));
        } catch(Exception e) {
            PanDevJiraAutoWorklog.debugException(e);
        }
    }
}