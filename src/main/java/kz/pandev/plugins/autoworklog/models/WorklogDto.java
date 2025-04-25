package kz.pandev.plugins.autoworklog.models;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Подробная информация о Worklog.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WorklogDto {

    /**
     * Комментарий к worklog.
     */
    private String comment;

    /**
     * Идентификатор вопроса, для которого предназначен этот работник.
     */
    private String issueId;

    /**
     *Время в секундах, потраченных на работу над этим вопросом.
     * Требуется при создании рабочих мест, если Timespent не предоставлен.
     * Необязательно при обновлении рабочих мест.
     * не может быть предоставлен, если предоставлена Timespent.
     */
    private long timeSpentSeconds;

    public long getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public void setTimeSpentSeconds(long timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }
}