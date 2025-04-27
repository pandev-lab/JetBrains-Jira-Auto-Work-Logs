package kz.pandev.jira_auto_worklog.factory;

import kz.pandev.jira_auto_worklog.configs.ServerSettings;

import java.util.Map;


/**
 * Фабрика для создания и управления настройками серверов (реализация Singleton с возможностью обновления).
 */
public class ServerSettingsFactory {

    private static volatile ServerSettings instance;
    private static final Object lock = new Object();


    private ServerSettingsFactory() {
    }

    /**
     * Обновляет текущий экземпляр новыми настройками из Map.
     *
     * @param settingsMap карта с новыми настройками
     * @throws IllegalArgumentException если settingsMap == null
     */
    public static void updateInstance(Map<String, String> settingsMap) {
        if (settingsMap == null) {
            throw new IllegalArgumentException("Settings map cannot be null");
        }
        synchronized (lock) {
            instance = createFromMap(settingsMap);
        }
    }

    /**
     * Создает настройки сервера
     * @param settings {@link Map} с настройками
     * @return настройки сервера
     */
    private static ServerSettings createFromMap(Map<String, String> settings) {
        if (settings.get(ServerSettings.USERNAME_KEY) == null || settings.get(ServerSettings.USERNAME_KEY).isEmpty()) {
        return new ServerSettings(
                settings.get(ServerSettings.URL_KEY),
                settings.get(ServerSettings.TOKEN_KEY));
        }
        else {
            return new ServerSettings(
                    settings.get(ServerSettings.URL_KEY),
                    settings.get(ServerSettings.USERNAME_KEY),
                    settings.get(ServerSettings.TOKEN_KEY)
            );}
        }


    /**
     * Получает существующий экземпляр настроек сервера.
     *
     * @return существующий экземпляр или новый дефолтный, если не был инициализирован
     */
    public static ServerSettings getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ServerSettings();
                }
            }
        }
        return instance;
    }

}
