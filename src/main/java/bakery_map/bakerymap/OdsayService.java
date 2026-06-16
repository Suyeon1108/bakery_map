package bakery_map.bakerymap;

import bakery_map.Bakery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OdsayService {

        private static final Logger log = LoggerFactory.getLogger(OdsayService.class);

        private final OdsayApiClient apiClient;
        private final OdsayRouteParser routeParser;

        public OdsayService(OdsayApiClient apiClient, OdsayRouteParser routeParser) {
                this.apiClient   = apiClient;
                this.routeParser = routeParser;
        }

        /**
         * ODsay 단일 구간 대중교통 경로 계산
         *
         * 흐름:
         *   1. fetchPath  → 경로 정보 + mapObj 획득
         *   2. fetchLane  → 구간별 그래픽 좌표 획득
         *   3. 두 결과 합쳐서 SegmentRouteDto 반환
         */
        @Cacheable(
                value = "odsayRoute",
                key = "@cacheKeyGenerator.generate('transit', #from.lat, #from.lng, #to.lat, #to.lng)"
        )
        public SegmentRouteDto getRoute(Bakery from, Bakery to) {
                log.debug("[ODsay] 경로 요청: {} → {}", from.getName(), to.getName());

                try {
                        // Step 1. 경로 탐색
                        OdsayPathResponse pathResponse = apiClient.fetchPath(from, to);
                        ParsedPathResult parsed = routeParser.parsePath(pathResponse, from, to);

                        // Step 2. 폴리라인 좌표 획득
                        OdsayLaneResponse laneResponse = apiClient.fetchLane(parsed.mapObj());
                        List<List<List<Double>>> lanePolylines = routeParser.parseLane(laneResponse);

                        // Step 3. legs + 폴리라인 병합 후 DTO 조립
                        List<TransitLegDto> legs = routeParser.mergeLegsAndPolylines(parsed.legs(), lanePolylines);

                        return SegmentRouteDto.builder()
                                .from(from.getName())
                                .to(to.getName())
                                .mode("transit")
                                .durationSec(parsed.totalTimeSec())
                                .distanceM(parsed.totalDistanceM())
                                .polyline(new ArrayList<>())
                                .transit(TransitDetailDto.builder()
                                        .totalWalkMin(parsed.totalWalkMin())
                                        .transferCount(parsed.transferCount())
                                        .legs(legs)
                                        .build())
                                .build();

                } catch (RouteCalculationException e) {
                        log.warn("[ODsay] 대중교통 경로 실패 → 도보 대체: {} → {}, 이유: {}",
                                from.getName(), to.getName(), e.getMessage());

                        return buildWalkFallbackRoute(from, to);
                }
        }

        //경로 부재시 단순 직선 폴리라인 제공
        private SegmentRouteDto buildWalkFallbackRoute(Bakery from, Bakery to) {
                int distanceM = calculateDistanceM(
                        from.getLat(), from.getLng(),
                        to.getLat(), to.getLng()
                );

                //도보 분당 이동거리로 나눔->이동시간 도출
                int walkMin = Math.max(1, (int) Math.ceil(distanceM / 67.0));

                List<List<Double>> walkPolyline = List.of(
                        List.of(from.getLat(), from.getLng()),
                        List.of(to.getLat(), to.getLng())
                );

                TransitLegDto walkLeg = TransitLegDto.builder()
                        .type("WALK")
                        .lineName("도보 대체")
                        .startStop(from.getName())
                        .endStop(to.getName())
                        .sectionMin(walkMin)
                        .startX(from.getLng())  //6.14 추가 이하 4줄
                        .startY(from.getLat())  
                        .endX(to.getLng())      
                        .endY(to.getLat())      
                        .polyline(walkPolyline)
                        .build();

                        return SegmentRouteDto.builder()
                                .from(from.getName())
                                .to(to.getName())
                                .mode("transit")
                                .durationSec(walkMin * 60)
                                .distanceM(distanceM)
                                .polyline(new ArrayList<>())
                                .transit(TransitDetailDto.builder()
                                        .totalWalkMin(walkMin)
                                        .transferCount(0)
                                        .legs(List.of(walkLeg))
                                        .build())
                                .build();
        }
        
        //하버사인으로 직선거리 계산
        private int calculateDistanceM(double lat1, double lng1, double lat2, double lng2) {
                final double R = 6371000;

                double dLat = Math.toRadians(lat2 - lat1);
                double dLng = Math.toRadians(lng2 - lng1);

                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);

                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                return (int) Math.round(R * c);
        }
}

