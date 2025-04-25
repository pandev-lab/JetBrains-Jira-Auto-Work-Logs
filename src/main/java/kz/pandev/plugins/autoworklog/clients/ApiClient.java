package kz.pandev.plugins.autoworklog.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.pandev.plugins.autoworklog.configs.ServerSettings;
import kz.pandev.plugins.autoworklog.models.WorklogDto;
import org.codehaus.plexus.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Класс ApiClient отвечает за взаимодействие с внешним API.
 */
public class ApiClient {

    static HttpClient httpClient = HttpClient.newHttpClient();
    private static final String APPLICATION_CONTENT_TYPE = "application/json";
    private static final String MYSELF = "/rest/api/latest/myself";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String BASIC = "Basic ";


    private ApiClient() {}

    /**
     * Отправляет запрос для работы с ворклогами с использованием HttpClient.
     *
     * @param settings     настройки сервера
     * @param worklog объект {@link WorklogDto}, содержащий данные запроса.
     * @param issueKey Ключ задачи, для которой добавляется ворклог.
     */
    public static HttpResponse<String> sendWorklogRequest(ServerSettings settings, WorklogDto worklog, String issueKey) {
        try {
            String requestBody = new ObjectMapper().writeValueAsString(worklog);

            String url = settings.getUrl() + "/rest/api/latest/issue/%s/worklog";
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(url, issueKey)))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            getHttpClientHeaders(settings.getUsername(), settings.getToken()).forEach(requestBuilder::header);

            HttpRequest request = requestBuilder.build();

           return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
             catch(Exception e){
                return null;
            }
        }


    /**
     * Формирует заголовки для запросов в формате, совместимом с HttpClient.
     *
     * @return Map с заголовками для HTTP-запроса
     */
    private static Map<String, String> getHttpClientHeaders(String username, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", APPLICATION_CONTENT_TYPE);

        if (!StringUtils.isEmpty(username)) {
            headers.put(AUTHORIZATION, BASIC + Base64.getEncoder()
                    .encodeToString((username + ":" + token).getBytes()));
        } else {
            headers.put(AUTHORIZATION, BEARER + token);
        }


        return headers;
    }

    /**
     * Отправляет тестовый GET запрос на URL хоста
     *
     * @return ResponseEntity с результатом запроса.
     */
    public static boolean validateHost(String url, String username, String token) {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url + MYSELF))
                .GET();

        getHttpClientHeaders(username, token).forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response != null &&  response.statusCode() == 200;
        }
        catch(Exception e) {
            return false;
        }
    }
}



