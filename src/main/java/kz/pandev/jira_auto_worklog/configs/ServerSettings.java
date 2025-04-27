package kz.pandev.jira_auto_worklog.configs;
import kz.pandev.jira_auto_worklog.utils.EncryptionUtil;
import kz.pandev.jira_auto_worklog.utils.SettingsFileReadWriterUtil;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

public class ServerSettings {
    public static final String TOKEN_KEY = "token";
    public static final String USERNAME_KEY = "username";
    public static final String URL_KEY = "url";

    private static final String CONFIG_FILE_NAME = "pandev_jira_auto_worklog_config.cfg";
    private static final String CACHE_FILE_NAME = "pandev_jira_auto_worklog_cache.txt";

    private static final String SETTINGS_SECTION = "settings";
    private static final String HOME_ENV = "PANDEV_METRICS_HOME";

    private String cachedHomeFolder = null;
    private File settingsFile;
    private File cacheFile;

    private final String url;
    private final String token;
    private final String username;


    public ServerSettings(String url, String username, String token) {
        this.url = url;
        this.token = token;
        this.username = username;
        Map<String, String> values = Map.of(
                URL_KEY, url,
                TOKEN_KEY,  EncryptionUtil.encrypt(token),
                USERNAME_KEY, username);
        SettingsFileReadWriterUtil.writeAll(getConfigFile(), SETTINGS_SECTION, values);
        getCacheFileFile();
    }

    public ServerSettings(String url, String token) {
        this.url = url;
        this.token = token;
        this.username = null;
        Map<String, String> values = Map.of(
                URL_KEY, url,
                TOKEN_KEY,  EncryptionUtil.encrypt(token));
        SettingsFileReadWriterUtil.writeAll(getConfigFile(), SETTINGS_SECTION, values);
        getCacheFileFile();
    }

    public ServerSettings() {
        Map<String, String> configValues = SettingsFileReadWriterUtil.readAll(getConfigFile(), SETTINGS_SECTION);
        this.url = configValues.get(URL_KEY);
        this.token = EncryptionUtil.decrypt(configValues.get(TOKEN_KEY));
        this.username = configValues.get(USERNAME_KEY) != null ? configValues.get(USERNAME_KEY) : null;
        getCacheFileFile();
    }

    /**
     * Получает значение настройки только из переменных класса.
     *
     * @param key Ключ настройки (например, EMAIL_KEY).
     * @return Значение настройки или null, если не найдено.
     */
    public String getSettingsFromFields(String key) {
        return switch (key) {
            case TOKEN_KEY -> this.token;
            case URL_KEY -> this.url;
            case USERNAME_KEY -> this.username;
            default -> null;
        };
    }

    public File getConfigFile() {
        if (this.settingsFile != null) {
            return this.settingsFile;
        }
        if (this.cachedHomeFolder == null) {
            if (System.getenv(HOME_ENV) != null && !System.getenv(HOME_ENV).trim().isEmpty()) {
                File folder = new File(System.getenv(HOME_ENV));
                if (folder.exists()) {
                    this.cachedHomeFolder = folder.getAbsolutePath();
                    this.settingsFile = new File(this.cachedHomeFolder, CONFIG_FILE_NAME);
                    return this.settingsFile;
                }
            }
            this.cachedHomeFolder = new File(System.getProperty("user.home")).getAbsolutePath();
        }
        this.settingsFile = new File(this.cachedHomeFolder, CONFIG_FILE_NAME);
        return this.settingsFile;
    }

    public void getCacheFileFile() {
        if (this.cacheFile != null) {
            return;
        }
        if (this.cachedHomeFolder == null) {
            if (System.getenv(HOME_ENV) != null && !System.getenv(HOME_ENV).trim().isEmpty()) {
                File folder = new File(System.getenv(HOME_ENV));
                if (folder.exists()) {
                    this.cachedHomeFolder = folder.getAbsolutePath();
                    this.cacheFile = new File(this.cachedHomeFolder, CACHE_FILE_NAME);
                    return;
                }
            }
            this.cachedHomeFolder = new File(System.getProperty("user.home")).getAbsolutePath();
        }
        this.cacheFile = new File(this.cachedHomeFolder, CACHE_FILE_NAME);
    }

    public boolean isConfigured() {
        return Stream.of(URL_KEY, TOKEN_KEY)
                .map(this::getSettingsFromFields)
                .noneMatch(value -> value == null || value.isEmpty());
    }

    public String getUrl() {
        return this.url;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthenticationHeader() {
        return "Bearer " + this.token;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public void setSettingsFile(File settingsFile) {
        this.settingsFile = settingsFile;
    }
}
