package kz.pandev.plugins.autoworklog;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.messages.MessageBusConnection;
import kz.pandev.plugins.autoworklog.configs.ServerSettings;
import kz.pandev.plugins.autoworklog.factory.ServerSettingsFactory;
import kz.pandev.plugins.autoworklog.listeners.*;
import kz.pandev.plugins.autoworklog.models.Heartbeat;
import kz.pandev.plugins.autoworklog.utils.GitInfoProvider;
import kz.pandev.plugins.autoworklog.utils.SettingsFileReadWriterUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;

public class PanDevJiraAutoWorklog implements ApplicationComponent {

    public static final BigDecimal FREQUENCY = new BigDecimal(2 * 60);
    public static final Logger log = LogManager.getLogger(PanDevJiraAutoWorklog.class);
    public static Map<String, Long> heartbeatsCache = new HashMap<>();
    private static final String CACHE_SETTINGS = "cache";
    private static MessageBusConnection connection;
    private static Heartbeat lastHeartbeat;
    private static String lastFile = null;
    private static BigDecimal lastTime = new BigDecimal(0);

    public static Icon getLogoIcon() {
        String theme = JBColor.isBright() ? "light" : "dark";
        return IconLoader.getIcon("images/icons/status-bar-icon-" + theme + "-theme.svg",
                PanDevJiraAutoWorklog.class);
    }

    public PanDevJiraAutoWorklog() {}

    @Override
    public void initComponent() {
        setConnection(ApplicationManager.getApplication().getMessageBus().connect());
        ServerSettings settings  = ServerSettingsFactory.getInstance();
        SettingsFileReadWriterUtil.readAll(settings.getCacheFile(), CACHE_SETTINGS)
                .forEach((key, value) -> {
            try {
                heartbeatsCache.put(key, Long.parseLong(value));
            } catch (NumberFormatException e) {
                heartbeatsCache.put(key, 0L);  // или null, если нужно
            }
        });
        initEventListeners();
    }

    public static void setConnection(MessageBusConnection connection) {
        PanDevJiraAutoWorklog.connection = connection;
    }

    private void initEventListeners() {
        ApplicationManager.getApplication().invokeLater(() -> {

            Disposable disposable = Disposer.newDisposable("PanDevJiraAutoWorklogListener");
            // save file
            connection.subscribe(FileDocumentManagerListener.TOPIC, new FileDocumentManagerListenerImpl());

            // edit document
            EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new BulkAwareDocumentListenerImpl(),
                    disposable);

            // mouse press
            EditorFactory.getInstance().getEventMulticaster().addEditorMouseListener(new EditorMouseListenerImpl(),
                    disposable);

            // scroll document
            EditorFactory.getInstance().getEventMulticaster().addVisibleAreaListener(new VisibleAreaListenerImpl(),
                    disposable);

            // caret moved
            EditorFactory.getInstance().getEventMulticaster().addCaretListener(new CaretListenerImpl(), disposable);
        });
    }

    @Override
    public void disposeComponent() {
        try {
            connection.disconnect();
        } catch (Exception ignored) {
            // Nothing to do
        }
        Map<String, String> tempMap = new HashMap<>();
        heartbeatsCache.forEach((k,v) -> tempMap.put(k, v.toString()));
        SettingsFileReadWriterUtil.writeAll(ServerSettingsFactory.getInstance().getCacheFile(), CACHE_SETTINGS, tempMap);
    }

    public static BigDecimal getCurrentTimestamp() {
        return new BigDecimal(String.valueOf(System.currentTimeMillis() / 1000.0)).setScale(4,
                RoundingMode.HALF_UP);
    }

    public static void appendHeartbeat(final VirtualFile file, final Project project, final boolean isWrite) {

        if (!shouldLogFile(file)) return;

        final BigDecimal time = PanDevJiraAutoWorklog.getCurrentTimestamp();

        if (!isWrite && file.getPath().equals(lastFile) && !enoughTimePassed(time)) {
            return;
        }

        lastFile = file.getPath();
        lastTime = time;

        Module currentModule = getCurrentModule(project, file);
        String moduleGitBranch = null;

        if (currentModule != null) {
            moduleGitBranch = GitInfoProvider.getModuleGitBranch(currentModule);
        }

        final String projectBasePath = project.getBasePath();
        final String projectName = project.getName();
        final String[] gitInfo = GitInfoProvider.getGitBranch(projectBasePath);
        final String gitBranch;

        if (Objects.nonNull(moduleGitBranch)) {
            gitBranch = moduleGitBranch;
        } else {
            gitBranch = gitInfo.length > 1 ? gitInfo[1] : null;
        }

        Heartbeat h = new Heartbeat();
        h.setTimestamp(time);
        h.setProject(projectName);
        h.setGitBranch(gitBranch);
        h.setFileName(file.getName());

        if (lastHeartbeat == null) {
            lastHeartbeat = h;
        }

        Pattern hashPattern = Pattern.compile("^[0-9a-f]{40}$");

        if (gitBranch != null && !hashPattern.matcher(gitBranch).matches()) {

            String complexKey = lastHeartbeat.getProject() + "|||" + lastHeartbeat.getGitBranch();

            long diff = (h.getTimestamp().subtract(lastHeartbeat.getTimestamp())).longValue();

            if (diff <= 900) {
                heartbeatsCache.put(complexKey, heartbeatsCache.getOrDefault(complexKey, 0L) +
                        diff);
            }
            lastHeartbeat = h;
        }
    }


    public static Module getCurrentModule(Project project, VirtualFile virtualFile) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();

        for (Module module : modules) {
            if (ModuleRootManager.getInstance(module).getFileIndex().isInContent(virtualFile)) {
                return module;
            }
        }
        return null;
    }

    public static boolean enoughTimePassed(BigDecimal currentTime) {
        return lastTime.add(FREQUENCY).compareTo(currentTime) < 0;
    }

    public static boolean shouldLogFile(VirtualFile file) {
        if (file == null || file.getUrl().startsWith("mock://")) {
            return false;
        }
        String filePath = file.getPath();
        return !filePath.equals("atlassian-ide-plugin.xml") && !filePath.contains("/.idea/workspace.xml");
    }

    public static boolean isAppInactive() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() == null;
    }

    public static boolean isProjectUninitialized(Project project) {
        if (project == null) return false;
        return !project.isInitialized();
    }

    @Nullable
    public static VirtualFile getVirtualFile(Document document) {
        if (document == null) return null;
        FileDocumentManager instance = FileDocumentManager.getInstance();
        return instance.getFile(document);
    }


    public static Project getProject(Document document) {
        Editor[] editors = EditorFactory.getInstance().getEditors(document);
        if (editors.length > 0) {
            return editors[0].getProject();
        }
        return null;
    }

    @Nullable
    public static Project getCurrentProject() {
        Project project = null;
        try {
            project = ProjectManager.getInstance().getDefaultProject();
        } catch (Exception ignored) {
//            Nothing to do  
        }
        return project;
    }

    public static void openDashboardWebsite(String url) {
        BrowserUtil.browse(url);
    }

    public static void debugException(Exception e) {
        if (!log.isDebugEnabled()) return;
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String str = e.getMessage() + "\n" + sw;
        log.debug(str);
    }
}
