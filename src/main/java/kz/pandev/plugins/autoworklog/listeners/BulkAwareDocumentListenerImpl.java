package kz.pandev.plugins.autoworklog.listeners;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import kz.pandev.plugins.autoworklog.PanDevJiraAutoWorklog;
import org.jetbrains.annotations.NotNull;

public class BulkAwareDocumentListenerImpl implements BulkAwareDocumentListener.Simple {
    @Override
    public void documentChangedNonBulk(@NotNull DocumentEvent documentEvent) {
        try {
            if (PanDevJiraAutoWorklog.isAppInactive()) return;
            Document document = documentEvent.getDocument();
            VirtualFile file = PanDevJiraAutoWorklog.getVirtualFile(document);
            if (file == null) return;
            Project project = PanDevJiraAutoWorklog.getProject(document);
            if (PanDevJiraAutoWorklog.isProjectUninitialized(project)) return;
            PanDevJiraAutoWorklog.appendHeartbeat(file, project, false);
        } catch(Exception e) {
            PanDevJiraAutoWorklog.debugException(e);
        }
    }
}