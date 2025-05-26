package kz.pandev.jira_auto_worklog.utils;

public final class TimeFormatUtil {
    private TimeFormatUtil() {}
    public static String pretty(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (h > 0) {
            sb.append(h).append(" h ");
        }
        if (h > 0 || m > 0) {
            sb.append(m).append(" m");
        } else {
            sb.append(s).append(" s");
        }
        return sb.toString().trim();
    }
}
