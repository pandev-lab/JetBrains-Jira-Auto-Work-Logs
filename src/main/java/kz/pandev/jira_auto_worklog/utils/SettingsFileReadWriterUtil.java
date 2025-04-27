package kz.pandev.jira_auto_worklog.utils;

import kz.pandev.jira_auto_worklog.PanDevJiraAutoWorklog;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettingsFileReadWriterUtil {

    private SettingsFileReadWriterUtil() {
    }

    /**
     * Метод для считывания всех данных из конфиг файла
     *
     * @param configFile файл
     * @param section секция
     * @return {@link Map} с парами ключ-значение
     */
    public static Map<String, String> readAll(File configFile, String section) {
        Map<String, String> result = new LinkedHashMap<>();

        // Читаем все строки файла сразу
        List<String> lines;
        try {
            lines = Files.readAllLines(configFile.toPath());
        } catch (IOException e) {
            PanDevJiraAutoWorklog.log.error("Error when reading file: {}", configFile.getAbsoluteFile(), e);
            return result;
        }

        // Обрабатываем считанные строки
        String currentSection = "";
        for (String line : lines) {
            line = line.trim();

            if (isLineSettingsSection(line)) {
                currentSection = extractSectionNameFromLine(line);
            } else if (isCurrentSection(section, currentSection)) {
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    /**
     * Метод для записи всех данных в конфиг файл
     *
     * @param configFile конфигурационный файл
     * @param section секция
     * @param values пары ключ-значение
     */
    public static void writeAll(File configFile, String section, Map<String, String> values) {
        if (values == null || values.isEmpty()) return;

        try {
            // 1. Очищаем файл и создаем секцию
            StringBuilder content = new StringBuilder();
            content.append("[").append(section).append("]\n");

            // 2. Добавляем все значения
            for (Map.Entry<String, String> entry : values.entrySet()) {
                content.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }

            // 3. Записываем всё в файл (автоматически перезаписывает существующий)
            Files.writeString(configFile.toPath(), content.toString());

        } catch (IOException e) {
            PanDevJiraAutoWorklog.log.error("Failed to write config: {}", configFile.getAbsolutePath(), e);
        }
    }

    /**
     * Извлекает название секции из строки
     * @param line строка
     * @return название секции
     */
    private static @NotNull String extractSectionNameFromLine(String line) {
        return line.substring(1, line.length() - 1).toLowerCase();
    }

    /**
     * Проверяет, является ли строка названием секции
     * @param line строка
     * @return true - строка является названием секции, иначе - false
     */
    private static boolean isLineSettingsSection(String line) {
        return line.startsWith("[") && line.endsWith("]");
    }

    /**
     * Проверяет соответствие переданной секции текущей
     * @param section секция для сравнения
     * @param currentSection текущая секция
     * @return true - строка является текущей секцией
     */
    private static boolean isCurrentSection(String section, String currentSection) {
        return section.toLowerCase().equals(currentSection);
    }
}

