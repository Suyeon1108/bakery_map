import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OsrmService {

    @Value("${osrm.foot.url}")
    private String footUrl;

    @Value("${osrm.car.url}")
    private String carUrl;

    private final WebClient webClient; // ✅ WebClientConfig 빈 주입 (필드 직접 생성 X)

    /**
     * OSRM 단일 구간 경로 계산
     *
     * 캐시 키: CacheKeyGenerator.generate(mode, lat, lon, lat, lon)
     * → 소수점 4자리 반올림으로 부동소수점 캐시 미스 방지
     *
     * @param mode foot | car
     * @param from 출발 빵집
     * @param to   도착 빵집
     */
    @Cacheable(
            value = "osrmRoute",
            key = "@cacheKeyGenerator.generate(#mode, #from.lat, #from.lng, #to.lat, #to.lng)"
    )
    public SegmentRouteDto getRoute(String mode, Bakery from, Bakery to) {
        String baseUrl = mode.equals("foot") ? footUrl : carUrl;
        String profile = mode.equals("foot") ? "foot" : "driving";

        // OSRM 좌표 순서: 경도(lng), 위도(lat)
        String url = String.format(
                "%s/route/v1/%s/%f,%f;%f,%f?overview=full&geometries=geojson",
                baseUrl, profile,
                from.getLng(), from.getLat(),
                to.getLng(),   to.getLat()
        );

        log.debug("[OSRM] 요청: {} → {} (mode={})", from.getName(), to.getName(), mode);

        Map<?, ?> response = webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> {
                    log.error("[OSRM] HTTP 오류: {}", res.statusCode());
                    return Mono.error(new RouteCalculationException("OSRM 서버 오류"));
                })
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))  
                .onErrorMap(TimeoutException.class, e ->
                        new RouteCalculationException("OSRM 응답 시간 초과"))
                .block();

        if (response == null || !response.containsKey("routes")) {
            throw new RouteCalculationException("OSRM 응답 데이터가 유효하지 않습니다.");
        }

        return parseResponse(response, from, to, mode);
    }

    @SuppressWarnings("unchecked")
    private SegmentRouteDto parseResponse(Map<?, ?> response,
                                          Bakery from, Bakery to, String mode) {
        List<?> routes       = (List<?>) response.get("routes");
        Map<?, ?> route      = (Map<?, ?>) routes.get(0);
        Map<?, ?> geometry   = (Map<?, ?>) route.get("geometry");
        List<List<Double>> coords = (List<List<Double>>) geometry.get("coordinates");

        // OSRM 응답 좌표 [lon, lat] → 카카오맵 [lat, lon] 변환
        List<double[]> polyline = coords.stream()
                .map(c -> new double[]{c.get(1), c.get(0)})
                .collect(Collectors.toList());

        return SegmentRouteDto.builder()
                .from(from.getName())
                .to(to.getName())
                .mode(mode)
                .durationSec(((Number) route.get("duration")).intValue())
                .distanceM(((Number) route.get("distance")).intValue())
                .polyline(polyline)
                .build();
    }
}
