package kz.pandev.plugins.autoworklog.listeners;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import kz.pandev.plugins.autoworklog.PanDevJiraAutoWorklog;
import org.jetbrains.annotations.NotNull;

public class FileDocumentManagerListenerImpl implements FileDocumentManagerListener {
    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        try {
            if (PanDevJiraAutoWorklog.isAppInactive()) return;
            VirtualFile file = PanDevJiraAutoWorklog.getVirtualFile(document);
            if (file == null) return;
            Project project = PanDevJiraAutoWorklog.getProject(document);
            if (PanDevJiraAutoWorklog.isProjectUninitialized(project)) return;
            PanDevJiraAutoWorklog.appendHeartbeat(file, project, true);
        } catch(Exception e) {
            PanDevJiraAutoWorklog.debugException(e);
        }
    }

    @Override
    public void beforeAllDocumentsSaving() {
        // Не реализовано
    }

    @Override
    public void beforeFileContentReload(@NotNull VirtualFile file, @NotNull Document document) {
        // Не реализовано
    }

    @Override
    public void fileWithNoDocumentChanged(@NotNull VirtualFile file) {
        // Не реализовано
    }

    @Override
    public void fileContentReloaded(@NotNull VirtualFile file, @NotNull Document document) {
        // Не реализовано
    }

    @Override
    public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
        // Не реализовано
    }

    @Override
    public void unsavedDocumentsDropped() {
        // Не реализовано
    }
}