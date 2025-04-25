package kz.pandev.plugins.autoworklog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kz.pandev.plugins.autoworklog.git.watcher.GitCommitWatcher;
import kz.pandev.plugins.autoworklog.utils.ServerSettingsCheckUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Класс, реализующий интерфейс ProjectActivity для выполнения действий при старте проекта.
 */
public class PanDevAutoWorklogStartupActivity implements ProjectActivity {

    /**
     * Выполняет необходимые проверки при старте проекта.
     *
     * @param project      проект, для которого выполняется стартовая активность
     * @param continuation объект для продолжения выполнения (не используется в данной реализации)
     * @return null, так как метод не возвращает результат
     */
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        project.getService(GitCommitWatcher.class);
        ServerSettingsCheckUtil.checkIsConfigured();
        return null;
    }
}