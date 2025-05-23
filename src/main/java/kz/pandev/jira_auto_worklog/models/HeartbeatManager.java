package kz.pandev.jira_auto_worklog.models;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Level.PROJECT)
public final class HeartbeatManager {

    /** branch -> seconds */
    private final Map<String, Long> time = new ConcurrentHashMap<>();

    public void add(String branch, long sec) {
        time.merge(branch, sec, Long::sum);
    }

    public void reset(String branch) { time.remove(branch); }
    public long forBranch(String branch) {
        return time.getOrDefault(branch, 0L);
    }

    public long total() { return time.values().stream().mapToLong(Long::longValue).sum(); }
}