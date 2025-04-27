package kz.pandev.jira_auto_worklog.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;
import org.jetbrains.annotations.NotNull;

public class EditorMouseListenerImpl implements com.intellij.openapi.editor.event.EditorMouseListener {
    @Override
    public void mousePressed(@NotNull EditorMouseEvent editorMouseEvent) {
        try {
            if (PanDevJiraAutoWorklog.isAppInactive()) return;
            Document document = editorMouseEvent.getEditor().getDocument();
            VirtualFile file = PanDevJiraAutoWorklog.getVirtualFile(document);
            if (file == null) return;
            Project project = editorMouseEvent.getEditor().getProject();
            if (PanDevJiraAutoWorklog.isProjectUninitialized(project)) return;
            ApplicationManager.getApplication().invokeLater(() ->
                PanDevJiraAutoWorklog.appendHeartbeat(file, project, false));
        } catch(Exception e) {
            PanDevJiraAutoWorklog.debugException(e);
        }
    }

    @Override
    public void mouseClicked(@NotNull EditorMouseEvent editorMouseEvent) {
        // Not implemented
    }

    @Override
    public void mouseReleased(@NotNull EditorMouseEvent editorMouseEvent) {
        // Not implemented
    }

    @Override
    public void mouseEntered(@NotNull EditorMouseEvent editorMouseEvent) {
        // Not implemented
    }

    @Override
    public void mouseExited(@NotNull EditorMouseEvent editorMouseEvent) {
        // Not implemented
    }
}
