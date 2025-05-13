package kz.pandev.jira_auto_worklog.utils;

public final class TimeFormatUtil {
    private TimeFormatUtil() {}
    public static String pretty(long sec) {
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        if (h > 0)  return String.format("%dh%02dm", h, m);
        if (m > 0)  return String.format("%dm%02ds", m, s);
        return s + "s";
    }
}
