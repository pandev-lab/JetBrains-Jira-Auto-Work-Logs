package kz.pandev.plugins.autoworklog.utils;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueUtil {

    private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("[A-Z]+-\\d+");

    private IssueUtil() {
    }

    public static  String getIssueFromSourceBranch(@NotNull String sourceBranch) {
        Matcher matcher = ISSUE_KEY_PATTERN.matcher(sourceBranch);
        return matcher.find() ? matcher.group() : null;
    }
}
