package kz.pandev.jira_auto_worklog.ui_dialogs;
import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class LinkPane extends JTextPane {

    private final String url;

    public LinkPane(String url, String text) {
        this.url = url;
        this.setEditable(false);
        this.addHyperlinkListener(new UrlHyperlinkListener());
        this.setContentType("text/html");
        this.setOpaque(false);
        this.setText(text);
    }

    private static class UrlHyperlinkListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(final HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (final IOException | URISyntaxException e) {
                    PanDevJiraAutoWorklog.log.error("Can't open URL {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void setText(final String text) {
        super.setText("<html><body style='text-align:center;'><a href='" + url +
                "' style='font-size:10px; font-weight:normal; text-decoration:none; padding-right:1px;'>" +
                text + "</a></body></html>");
    }
}
