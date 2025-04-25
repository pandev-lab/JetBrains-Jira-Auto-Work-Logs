package kz.pandev.plugins.autoworklog.widgets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


/**
 * Класс для создания Widget.
 */
public class PanDevStatusBarFactory implements StatusBarWidgetFactory {


    /**
     * Возвращает уникальный идентификатор виджета.
     *
     * @return уникальный идентификатор виджета
     */
    @Override
    public @NotNull @NonNls String getId() {
        return PanDevStatusbarWidget.WIDGET_ID;
    }

    /**
     * Возвращает имя виджета, отображаемое в настраиваемых параметрах.
     *
     * @return имя виджета
     */
    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "PanDevJiraAutoWorklog Status";
    }

    /**
     * Создает и возвращает виджет строки состояния для заданного проекта.
     *
     * @param project экземпляр проекта IntelliJ IDEA
     * @return созданный виджет строки состояния
     */
    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new PanDevStatusbarWidget();
    }
}
