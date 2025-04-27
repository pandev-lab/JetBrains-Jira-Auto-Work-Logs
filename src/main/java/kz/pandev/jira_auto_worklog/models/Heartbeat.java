package kz.pandev.jira_auto_worklog.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class Heartbeat {

    @JsonProperty("timestamp")
    private BigDecimal timestamp;

    @JsonProperty("project")
    private String project;

    @JsonProperty("gitBranch")
    private String gitBranch;

    @JsonProperty("fileName")
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public BigDecimal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
        this.timestamp = timestamp;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Heartbeat heartbeat)) return false;
        return Objects.equals(timestamp, heartbeat.timestamp) && Objects.equals(project, heartbeat.project) &&
                Objects.equals(gitBranch, heartbeat.gitBranch) && Objects.equals(fileName, heartbeat.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, project, gitBranch, fileName);
    }
}
