package kz.pandev.jira_auto_worklog.listeners;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class VisibleAreaListenerImpl implements VisibleAreaListener {
    @Override
    public void visibleAreaChanged(@NotNull VisibleAreaEvent visibleAreaEvent) {
        try {
            if (!didChange(visibleAreaEvent)) return;
            if (PanDevJiraAutoWorklog.isAppInactive()) return;
            Document document = visibleAreaEvent.getEditor().getDocument();
            VirtualFile file = PanDevJiraAutoWorklog.getVirtualFile(document);
            if (file == null) return;
            Project project = visibleAreaEvent.getEditor().getProject();
            if (PanDevJiraAutoWorklog.isProjectUninitialized(project)) return;
            PanDevJiraAutoWorklog.appendHeartbeat(file, project, false);
        } catch(Exception e) {
            PanDevJiraAutoWorklog.debugException(e);
        }
    }

    private boolean didChange(VisibleAreaEvent visibleAreaEvent) {
        Rectangle oldRect = visibleAreaEvent.getOldRectangle();
        if (oldRect == null) return true;
        Rectangle newRect = visibleAreaEvent.getNewRectangle();
        if (newRect == null) return false;
        return newRect.x != oldRect.x || newRect.y != oldRect.y;
    }
}
