package bakery_map.bakerymap;

import bakery_map.Bakery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class OdsayApiClient {

    private static final Logger log = LoggerFactory.getLogger(OdsayApiClient.class);
    private static final String BASE_URL = "https://api.odsay.com/v1/api";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String odsayApiKey;

    public OdsayApiClient(@Value("${odsay.api-key}") String odsayApiKey) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
	this.odsayApiKey =  odsayApiKey.replace("+", "%2B")
                                    .replace("/", "%2F")
                                    .replace("=", "%3D");
    }

    public OdsayPathResponse fetchPath(Bakery from, Bakery to) {
        String url = BASE_URL + "/searchPubTransPathT"
                + "?SX=" + from.getLng()
                + "&SY=" + from.getLat()
                + "&EX=" + to.getLng()
                + "&EY=" + to.getLat()
                + "&apiKey=" + odsayApiKey;

        log.debug("[ODsay fetchPath] 요청 URL: {}", url);

        String body = sendGet(url, "fetchPath");
        OdsayPathResponse response = parseJson(body, OdsayPathResponse.class, "fetchPath");

        validateResponse(response.error(), "fetchPath");

        return response;
    }

    public OdsayLaneResponse fetchLane(String mapObj) {
        String url = BASE_URL + "/loadLane"
                + "?mapObject=0:0@" + mapObj
                + "&apiKey=" + odsayApiKey;

        log.debug("[ODsay fetchLane] 요청 URL: {}", url);

        String body = sendGet(url, "fetchLane");
        OdsayLaneResponse response = parseJson(body, OdsayLaneResponse.class, "fetchLane");

        validateResponse(response.error(), "fetchLane");

        if (response.result() == null) {
            log.warn("[ODsay fetchLane] result 없음, null 반환");
            return null;
        }

        return response;
    }

    private String sendGet(String url, String apiName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 400) {
                log.error("[ODsay {}] HTTP 오류: {}", apiName, response.statusCode());
                throw new RouteCalculationException("ODsay HTTP 오류: " + response.statusCode());
            }

            return response.body();

        } catch (RouteCalculationException e) {
            throw e;
        } catch (IOException e) {
            log.error("[ODsay {}] 네트워크 오류: {}", apiName, e.getMessage());
            throw new RouteCalculationException("ODsay 네트워크 오류: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RouteCalculationException("ODsay 요청 인터럽트");
        }
    }

    private <T> T parseJson(String body, Class<T> clazz, String apiName) {
        try {
            T result = objectMapper.readValue(body, clazz);

            if (result == null) {
                throw new RouteCalculationException("ODsay 응답이 없습니다. (" + apiName + ")");
            }

            return result;

        } catch (JsonProcessingException e) {
            log.error("[ODsay {}] JSON 파싱 실패: {}", apiName, body);
            throw new RouteCalculationException("ODsay 응답 파싱 실패");
        }
    }

    private void validateResponse(List<OdsayErrorResponse> errors, String methodName) {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        OdsayErrorResponse error = errors.get(0);

        throw new RouteCalculationException(
                "ODsay API error in " + methodName
                        + " / code=" + error.code()
                        + " / message=" + error.message()
        );
    }
}
