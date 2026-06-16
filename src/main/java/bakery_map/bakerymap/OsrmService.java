package bakery_map.bakerymap;

import bakery_map.Bakery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class OsrmService {

    private static final Logger log = LoggerFactory.getLogger(OsrmService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String footUrl;
    private final String carUrl;

    public OsrmService(@Value("${osrm.foot.url}") String footUrl,
                       @Value("${osrm.car.url}") String carUrl) {
        this.httpClient   = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.footUrl      = footUrl;
        this.carUrl       = carUrl;
    }

    @Cacheable(
            value = "osrmRoute",
            key = "@cacheKeyGenerator.generate(#mode, #from.lat, #from.lng, #to.lat, #to.lng)"
    )
    public SegmentRouteDto getRoute(String mode, Bakery from, Bakery to) {
        String baseUrl = mode.equals("foot") ? footUrl : carUrl;
        String profile = mode.equals("foot") ? "foot" : "driving";

        String url = String.format(
                Locale.US,
                "%s/route/v1/%s/%f,%f;%f,%f?overview=full&geometries=geojson",
                baseUrl, profile,
                from.getLng(), from.getLat(),
                to.getLng(),   to.getLat()
        );

        log.debug("[OSRM] 요청: {} → {} (mode={})", from.getName(), to.getName(), mode);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                log.error("[OSRM] HTTP 오류: {}", response.statusCode());
                throw new RouteCalculationException("OSRM 서버 오류");
            }

            OsrmResponse osrmResponse = objectMapper.readValue(
                    response.body(), OsrmResponse.class);

            if (osrmResponse == null
                    || osrmResponse.routes() == null
                    || osrmResponse.routes().isEmpty()) {
                throw new RouteCalculationException("OSRM 경로 결과가 없습니다.");
            }

            return parseResponse(osrmResponse, from, to, mode);

        } catch (RouteCalculationException e) {
            throw e;
        } catch (IOException e) {
            log.error("[OSRM] 네트워크 오류: {}", e.getMessage());
            throw new RouteCalculationException("OSRM 네트워크 오류: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RouteCalculationException("OSRM 요청 인터럽트");
        }
    }

    private SegmentRouteDto parseResponse(OsrmResponse response,
                                          Bakery from, Bakery to, String mode) {
        OsrmResponse.OsrmRoute firstRoute = response.routes().get(0);

        List<List<Double>> polyline = firstRoute.geometry().coordinates().stream()
                .map(c -> List.of(c.get(1), c.get(0)))
                .collect(Collectors.toList());

        return SegmentRouteDto.builder()
                .from(from.getName())
                .to(to.getName())
                .mode(mode)
                .durationSec((int) Math.round(firstRoute.duration()))
                .distanceM((int) Math.round(firstRoute.distance()))
                .polyline(polyline)
                .build();
    }
}
