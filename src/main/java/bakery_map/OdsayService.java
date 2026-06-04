import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service 
public class OdsayService {

    @Value("${odsay.api-key}")
    private String odsayApiKey;

    private final WebClient webClient; // WebClientConfig 빈 주입

    /**
     * ODsay 단일 구간 대중교통 경로 계산
     *
     * 흐름:
     *   1. searchPubTransPathT → 경로 정보 + mapObj 획득
     *   2. loadLane(mapObj)    → 구간별 그래픽 좌표 획득
     *   3. 두 결과 합쳐서 SegmentRouteDto 반환
     *
     * 캐시: odsayRoute, 30분 만료
     */
    @Cacheable(
            value = "odsayRoute",
            key = "@cacheKeyGenerator.generate('transit', #from.lat, #from.lng, #to.lat, #to.lng)"
    )
    public SegmentRouteDto getRoute(Bakery from, Bakery to) {
        log.debug("[ODsay] 경로 요청: {} → {}", from.getName(), to.getName());

        // ── Step 1. 경로 탐색 ───────────────────────────────────
        Map<?, ?> pathResponse = callSearchPubTransPath(from, to);
        ParsedPathResult parsed = parsePathResponse(pathResponse, from, to);

        // ── Step 2. loadLane으로 폴리라인 좌표 획득 ─────────────
        // mapObj: "21:11:0@21:12:0" 형식, searchPubTransPathT 응답의 info.mapObj
        List<List<double[]>> lanePolylines = callLoadLane(parsed.mapObj());

        // ── Step 3. leg별 폴리라인 매핑 후 최종 DTO 조립 ─────────
        List<TransitLegDto> legsWithPolyline =
                mergeLegsAndPolylines(parsed.legs(), lanePolylines);

        //build 삭제
        TransitDetailDto transitDetail = new TransitDetailDto();
        transitDetail.setTotalWalkMin(parsed.totalWalkMin);
        transitDetail.setTransferCount(parsed.transferCount);
        transitDetail.setLegs(legsWithPolyline);

        SegmentRouteDto segmentDto = new SegmentRouteDto();
        segmentDto.setFrom(from.getName());
        segmentDto.setTo(to.getName());
        segmentDto.setMode("transit");
        segmentDto.setDurationSec(parsed.totalTimeSec);
        segmentDto.setDistanceM(parsed.totalDistanceM);
        segmentDto.setPolyline(new ArrayList<>());
        segmentDto.setTransit(transitDetail);

        return segmentDto;
    }

    // ── searchPubTransPathT 호출 ─────────────────────────────────

    private Map<?, ?> callSearchPubTransPath(Bakery from, Bakery to) {
        Map<?, ?> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https").host("api.odsay.com")
                        .path("/v1/api/searchPubTransPathT")
                        .queryParam("SX", from.getLng()) // 경도
                        .queryParam("SY", from.getLat()) // 위도
                        .queryParam("EX", to.getLng())
                        .queryParam("EY", to.getLat())
                        .queryParam("apiKey", odsayApiKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> {
                    log.error("[ODsay searchPubTransPathT] HTTP 오류: {}", res.statusCode());
                    return Mono.error(new RouteCalculationException("대중교통 경로 API 오류"));
                })
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(TimeoutException.class, e ->
                        new RouteCalculationException("ODsay 경로 응답 시간 초과"))
                .block();

        if (response == null) {
            throw new RouteCalculationException("ODsay 응답이 없습니다.");
        }
        return response;
    }

    // ── searchPubTransPathT 응답 파싱 ────────────────────────────

    @SuppressWarnings("unchecked")
    private ParsedPathResult parsePathResponse(Map<?, ?> response,
                                               Bakery from, Bakery to) {
        Map<?, ?> result = (Map<?, ?>) response.get("result");
        if (result == null) {
            throw new RouteCalculationException("해당 구간의 대중교통 경로를 찾을 수 없습니다.");
        }

        List<?> pathList = (List<?>) result.get("path");
        if (pathList == null || pathList.isEmpty()) {
            throw new RouteCalculationException(
                    from.getName() + " → " + to.getName() + " 대중교통 경로가 존재하지 않습니다.");
        }

        Map<?, ?> bestPath          = (Map<?, ?>) pathList.get(0);
        Map<?, ?> info              = (Map<?, ?>) bestPath.get("info");
        List<Map<?, ?>> subPaths    = (List<Map<?, ?>>) bestPath.get("subPath");

        // mapObj: loadLane 호출에 필요한 식별자 (경로 탐색 응답에 포함)
        String mapObj = (String) info.get("mapObj");
        if (mapObj == null || mapObj.isBlank()) {
            throw new RouteCalculationException("mapObj 값이 없어 폴리라인을 가져올 수 없습니다.");
        }

        int totalTimeSec   = ((Number) info.get("totalTime")).intValue() * 60;
        int totalDistanceM = ((Number) info.get("totalDistance")).intValue();
        int transferCount  =
                ((Number) info.get("busTransitCount")).intValue() +
                ((Number) info.get("subwayTransitCount")).intValue();

        // totalWalk: 단위 미터 → 도보속도(67m/min) 기준 분 환산
        int totalWalkM   = ((Number) info.get("totalWalk")).intValue();
        int totalWalkMin = (int) Math.ceil(totalWalkM / 67.0);

        List<TransitLegDto> legs = subPaths.stream()
                .map(this::toTransitLeg)
                .collect(Collectors.toList());

        return new ParsedPathResult(mapObj, totalTimeSec, totalDistanceM, transferCount, totalWalkMin, legs);
    }

    // ── loadLane 호출 ────────────────────────────────────────────

    /**
     * loadLane API: 노선 그래픽 좌표 조회
     *
     * @param mapObj searchPubTransPathT 응답의 info.mapObj
     * @return 교통수단 구간별 폴리라인 목록 (도보 구간은 빈 리스트)
     *         인덱스는 subPath의 BUS/SUBWAY 구간 순서와 일치
     */
    @SuppressWarnings("unchecked")
    private List<List<double[]>> callLoadLane(String mapObj) {
        log.debug("[ODsay loadLane] mapObj: {}", mapObj);

        Map<?, ?> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https").host("api.odsay.com")
                        .path("/v1/api/loadLane")
                        .queryParam("mapObject", "0:0@" + mapObj) // 필수 prefix 포함
                        .queryParam("apiKey", odsayApiKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> {
                    log.error("[ODsay loadLane] HTTP 오류: {}", res.statusCode());
                    return Mono.error(new RouteCalculationException("loadLane API 오류"));
                })
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(TimeoutException.class, e ->
                        new RouteCalculationException("ODsay loadLane 응답 시간 초과"))
                .block();

        if (response == null) {
            log.warn("[ODsay loadLane] 응답 없음, 빈 폴리라인으로 처리");
            return new ArrayList<>();
        }
 
        Map<?, ?> result = (Map<?, ?>) response.get("result");
        if (result == null) return new ArrayList<>();

        List<?> lanes = (List<?>) result.get("lane");
        if (lanes == null || lanes.isEmpty()) return new ArrayList<>();

        List<List<double[]>> allPolylines = new ArrayList<>();

        for (Object laneObj : lanes) {
            Map<?, ?> lane = (Map<?, ?>) laneObj;
            List<?> sections = (List<?>) lane.get("section");
            if (sections == null || sections.isEmpty()) {
                allPolylines.add(new ArrayList<>());
                continue;
            }

            // 한 lane의 모든 section 좌표를 이어붙여 하나의 폴리라인으로
            List<double[]> lanePolyline = new ArrayList<>();
            for (Object sectionObj : sections) {
                Map<?, ?> section = (Map<?, ?>) sectionObj;
                List<?> graphPosList = (List<?>) section.get("graphPos");
                if (graphPosList == null) continue;

                for (Object posObj : graphPosList) {
                    Map<?, ?> pos = (Map<?, ?>) posObj;
                    // loadLane 응답: x=경도, y=위도 → 카카오맵 [lat, lon]으로 변환
                    double lon = ((Number) pos.get("x")).doubleValue();
                    double lat = ((Number) pos.get("y")).doubleValue();
                    lanePolyline.add(new double[]{lat, lon});
                }
            }
            allPolylines.add(lanePolyline);
        }

        return allPolylines;
    }

    // ── legs와 폴리라인 병합 ─────────────────────────────────────

    /**
     * subPath legs와 loadLane 폴리라인을 매핑
     *
     * loadLane은 BUS/SUBWAY 구간만 반환하므로
     * WALK 구간은 빈 폴리라인, BUS/SUBWAY는 순서대로 매핑
     */
    private List<TransitLegDto> mergeLegsAndPolylines(List<TransitLegDto> legs,
                                                       List<List<double[]>> lanePolylines) {
        List<TransitLegDto> result = new ArrayList<>();
        int laneIndex = 0;

        // for (TransitLegDto leg : legs) {
        //     if (leg.getType().equals("WALK")) {
        //         // 도보 구간: 폴리라인 없음
        //         result.add(TransitLegDto.builder()
        //                 .type(leg.getType())
        //                 .lineName(leg.getLineName())
        //                 .startStop(leg.getStartStop())
        //                 .endStop(leg.getEndStop())
        //                 .sectionMin(leg.getSectionMin())
        //                 .polyline(new ArrayList<>())
        //                 .build());
        //     } else {
        //         // BUS/SUBWAY: lanePolylines에서 순서대로 매핑
        //         List<double[]> polyline = (laneIndex < lanePolylines.size())
        //                 ? lanePolylines.get(laneIndex++)
        //                 : new ArrayList<>();

        //         result.add(TransitLegDto.builder()
        //                 .type(leg.getType())
        //                 .lineName(leg.getLineName())
        //                 .startStop(leg.getStartStop())
        //                 .endStop(leg.getEndStop())
        //                 .sectionMin(leg.getSectionMin())
        //                 .polyline(polyline)
        //                 .build());
        //     }
        // }
        // return result;
        for (TransitLegDto leg : legs) {
            TransitLegDto newLeg = new TransitLegDto();
            newLeg.setType(leg.getType());
            newLeg.setLineName(leg.getLineName());
            newLeg.setStartStop(leg.getStartStop());
            newLeg.setEndStop(leg.getEndStop());
            newLeg.setSectionMin(leg.getSectionMin());

            if (leg.getType().equals("WALK")) {
                newLeg.setPolyline(new ArrayList<>());
            } else {
                List<double[]> polyline = (laneIndex < lanePolylines.size())
                        ? lanePolylines.get(laneIndex++)
                        : new ArrayList<>();
                newLeg.setPolyline(polyline);
            }
            result.add(newLeg);
        }
        return result;
    }

    // ── subPath → TransitLegDto 변환 (폴리라인 제외) ─────────────

    private TransitLegDto toTransitLeg(Map<?, ?> sub) {
        int trafficType = ((Number) sub.get("trafficType")).intValue();
        String type = switch (trafficType) {
            case 1 -> "SUBWAY";
            case 2 -> "BUS";
            default -> "WALK";
        };
        TransitLegDto leg = new TransitLegDto();
        leg.setType(type);
        leg.setSectionMin(((Number) sub.get("sectionTime")).intValue());
        leg.setPolyline(new ArrayList<>());

        if (trafficType != 3) {
            List<?> lanes  = (List<?>) sub.get("lane");
            Map<?, ?> lane = (Map<?, ?>) lanes.get(0);
            String lineName = trafficType == 1
                    ? (String) lane.get("name")
                    : (String) lane.get("busNo");

            builder.lineName(lineName)
                   .startStop((String) sub.get("startName"))
                   .endStop((String) sub.get("endName"));
        }

        return builder.build();
    }

    // ── 내부 파싱 결과 전달용 record ─────────────────────────────

    private static class ParsedPathResult {
        final String mapObj;
        final int totalTimeSec;
        final int totalDistanceM;
        final int transferCount;
        final int totalWalkMin;
        final List<TransitLegDto> legs;

        ParsedPathResult(String mapObj, int totalTimeSec, int totalDistanceM,
                         int transferCount, int totalWalkMin, List<TransitLegDto> legs) {
            this.mapObj        = mapObj;
            this.totalTimeSec  = totalTimeSec;
            this.totalDistanceM = totalDistanceM;
            this.transferCount = transferCount;
            this.totalWalkMin  = totalWalkMin;
            this.legs          = legs;
        }
    }