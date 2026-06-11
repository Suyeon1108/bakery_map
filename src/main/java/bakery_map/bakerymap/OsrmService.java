package bakery_map.bakerymap;

import bakery_map.Bakery;
import bakery_map.bakerymap.OsrmResponse;

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
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class OsrmService {

    private static final Logger log = LoggerFactory.getLogger(OsrmService.class);

    private final WebClient webClient;
    private final String footUrl;
    private final String carUrl;

    public OsrmService(WebClient webClient,
                       @Value("${osrm.foot.url}") String footUrl,
                       @Value("${osrm.car.url}") String carUrl) {
        this.webClient = webClient;
        this.footUrl   = footUrl;
        this.carUrl    = carUrl;
    }

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
                Locale.US,
                "%s/route/v1/%s/%f,%f;%f,%f?overview=full&geometries=geojson",
                baseUrl, profile,
                from.getLng(), from.getLat(),
                to.getLng(),   to.getLat()
        );

        log.debug("[OSRM] 요청: {} → {} (mode={})", from.getName(), to.getName(), mode);

        OsrmResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> {
                    log.error("[OSRM] HTTP 오류: {}", res.statusCode());
                    return Mono.error(new RouteCalculationException("OSRM 서버 오류"));
                })
                .bodyToMono(OsrmResponse.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorMap(TimeoutException.class, e ->
                        new RouteCalculationException("OSRM 응답 시간 초과"))
                .block();

        // null 및 빈 리스트 체크를 get(0) 호출 전에 수행
        if (response == null
                || response.routes() == null
                || response.routes().isEmpty()) {
            throw new RouteCalculationException("OSRM 경로 결과가 없습니다.");
        }

        return parseResponse(response, from, to, mode);
    }

    private SegmentRouteDto parseResponse(OsrmResponse response,
                                          Bakery from, Bakery to, String mode) {
        OsrmResponse.OsrmRoute firstRoute = response.routes().get(0);

        // OSRM [lng, lat] → 카카오맵 [lat, lng] 변환
        List<List<Double>> polyline = firstRoute.geometry().coordinates().stream()
                .map(c -> List.of(c.get(1), c.get(0)))
                .collect(Collectors.toList());

        return SegmentRouteDto.builder()
                .from(from.getName())
                .to(to.getName())
                .mode(mode)
                // double → int 변환 시 반올림 처리 (버림 방지)
                .durationSec((int) Math.round(firstRoute.duration()))
                .distanceM((int) Math.round(firstRoute.distance()))
                .polyline(polyline)
                .build();
    }
}
