package bakery_map.bakerymap;

import org.springframework.stereotype.Component;

/**
 * 캐시 키 생성기
 *
 * 부동소수점(double) 좌표를 그대로 캐시 키로 사용하면
 * 37.123456 vs 37.123457처럼 미세한 차이로 캐시 미스가 발생할 수 있음. 
 *
 * SpEL에서 @cacheKeyGenerator.generate(...) 로 호출됨.
 */
@Component("cacheKeyGenerator")
public class CacheKeyGenerator {

    /**
     * @param mode     이동수단 (foot | car | transit)
     * @param startLat 출발 위도
     * @param startLon 출발 경도
     * @param endLat   도착 위도
     * @param endLon   도착 경도
     * @return "mode:반올림위도,반올림경도:반올림위도,반올림경도"
     */
    public String generate(String mode,
                           double startLat, double startLon,
                           double endLat,   double endLon) {
        return String.format("%s:%d,%d:%d,%d",
                mode,
                Math.round(startLat * 10000),
                Math.round(startLon * 10000),
                Math.round(endLat   * 10000),
                Math.round(endLon   * 10000));
    }
}
