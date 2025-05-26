package kz.pandev.jira_auto_worklog.models;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service(Level.PROJECT)
public final class HeartbeatManager {

    /** branch -> seconds */
    private final Map<String, Long> time = new ConcurrentHashMap<>();

    public void add(String branch, long sec) {
        time.merge(branch, sec, Long::sum);
    }

    public void reset(String branch) { time.remove(branch); }
    private volatile String currentBranch;
    public long forBranch(String branch) {
        return time.getOrDefault(branch, 0L);
    }

    public boolean switchBranch(String branch) {
        if (!Objects.equals(currentBranch, branch)) {
            currentBranch = branch;
            return true;
        }
        return false;
    }
    public String getCurrentBranch(){
        return currentBranch;
    }
    public long total() { return time.values().stream().mapToLong(Long::longValue).sum(); }
}