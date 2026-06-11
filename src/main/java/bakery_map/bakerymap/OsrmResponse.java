package bakery_map.bakerymap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * OSRM /route API 응답 매핑 Record
 *
 * 실제 OSRM 응답 구조:
 * {
 *   "routes": [
 *     {
 *       "duration": 300.5,      // 소요 시간 (초)
 *       "distance": 1500.2,     // 거리 (미터)
 *       "geometry": {
 *         "coordinates": [[lng, lat], [lng, lat], ...]
 *       }
 *     }
 *   ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 불필요한 필드 무시
public record OsrmResponse(
        List<OsrmRoute> routes
) {

    /**
     * 단일 경로 정보
     * OSRM은 여러 경로를 반환할 수 있으나 첫 번째(최적 경로)만 사용
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OsrmRoute(
            double duration,      // 소요 시간 (초, double)
            double distance,      // 거리 (미터, double)
            OsrmGeometry geometry
    ) {}

    /**
     * 경로 좌표 정보
     * OSRM 좌표 순서: [경도(lng), 위도(lat)]
     * → 카카오맵 변환 필요: [위도(lat), 경도(lng)]
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OsrmGeometry(
            List<List<Double>> coordinates  // [[lng, lat], [lng, lat], ...]
    ) {}
}
