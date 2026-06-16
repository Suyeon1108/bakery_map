package bakery_map.bakerymap;

import bakery_map.Bakery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OdsayRouteParser {

    private static final Logger log = LoggerFactory.getLogger(OdsayRouteParser.class);

    private final OsrmService osrmService;

    public OdsayRouteParser(OsrmService osrmService) {
        this.osrmService = osrmService;
    }

    // ── OdsayPathResponse → ParsedPathResult ────────────────────

    public ParsedPathResult parsePath(OdsayPathResponse response, Bakery from, Bakery to) {
        if (response.result() == null) {
            throw new RouteCalculationException("해당 구간의 대중교통 경로를 찾을 수 없습니다.");
        }

        List<OdsayPathResponse.OdsayPath> pathList = response.result().path();
        if (pathList == null || pathList.isEmpty()) {
            throw new RouteCalculationException(
                    from.getName() + " → " + to.getName() + " 대중교통 경로가 존재하지 않습니다.");
        }

        OdsayPathResponse.OdsayPath bestPath = pathList.get(0);
        OdsayPathResponse.OdsayPathInfo info = bestPath.info();

        List<OdsayPathResponse.OdsaySubPath> subPaths =
                bestPath.subPath() != null ? bestPath.subPath() : new ArrayList<>();

        String mapObj = info.mapObj();
        if (mapObj == null || mapObj.isBlank()) {
            throw new RouteCalculationException("mapObj 값이 없어 폴리라인을 가져올 수 없습니다.");
        }

        int totalTimeSec   = info.totalTime() * 60;
        int totalDistanceM = info.totalDistance();
        int transferCount  = info.busTransitCount() + info.subwayTransitCount();
        int totalWalkMin   = (int) Math.ceil(info.totalWalk() / 67.0);

        List<TransitLegDto> legs = subPaths.stream()
                .map(this::toTransitLeg)
                .collect(Collectors.toList());

        return new ParsedPathResult(mapObj, totalTimeSec, totalDistanceM, transferCount, totalWalkMin, legs);
    }

    // ── OdsayLaneResponse → 구간별 폴리라인 목록 ────────────────

    public List<List<List<Double>>> parseLane(OdsayLaneResponse response) {
        if (response == null || response.result() == null) {
            return new ArrayList<>();
        }

        List<OdsayLaneResponse.OdsayLane> lanes = response.result().lane();
        if (lanes == null || lanes.isEmpty()) return new ArrayList<>();

        List<List<List<Double>>> allPolylines = new ArrayList<>();

        for (OdsayLaneResponse.OdsayLane lane : lanes) {
            List<OdsayLaneResponse.OdsaySection> sections = lane.section();
            if (sections == null || sections.isEmpty()) {
                allPolylines.add(new ArrayList<>());
                continue;
            }

            List<List<Double>> lanePolyline = new ArrayList<>();
            for (OdsayLaneResponse.OdsaySection section : sections) {
                if (section.graphPos() == null) continue;
                for (OdsayLaneResponse.OdsayGraphPos pos : section.graphPos()) {
                    lanePolyline.add(List.of(pos.y(), pos.x()));
                }
            }
            allPolylines.add(lanePolyline);
        }

        return allPolylines;
    }

    // ── legs와 폴리라인 병합 + WALK 구간 OSRM 호출 ──────────────
    public List<TransitLegDto> mergeLegsAndPolylines(
        List<TransitLegDto> legs,
        List<List<List<Double>>> lanePolylines) {

        // 1. BUS/SUBWAY 개수와 lanePolylines 개수 일치 여부 사전 검증
        long transitCount = legs.stream()
                .filter(l -> !"WALK".equals(l.getType()))
                .count();

        boolean isLaneValid = (transitCount == lanePolylines.size());

        if (!isLaneValid) {
            log.warn("[ODsay] 대중교통 구간 수({})와 폴리라인 수({}) 불일치. 대중교통 궤적은 생략합니다.",
                    transitCount, lanePolylines.size());
        }

        List<TransitLegDto> result = new ArrayList<>();
        int laneIndex = 0;

        // 2. 구간(Legs) 순회 및 폴리라인 매핑
        for (TransitLegDto leg : legs) {
            List<List<Double>> polyline;

            if ("WALK".equals(leg.getType())) {
                // 대중교통 궤적 오류와 무관하게 도보 궤적은 OSRM으로 정상 호출
                polyline = fetchWalkPolyline(leg);
            } else {
                // 개수가 일치(isLaneValid)할 때만 인덱스로 매핑, 아니면 빈 리스트 처리
                polyline = isLaneValid ? lanePolylines.get(laneIndex++) : new ArrayList<>();
            }

            result.add(TransitLegDto.builder()
                    .type(leg.getType())
                    .lineName(leg.getLineName())
                    .startStop(leg.getStartStop())
                    .endStop(leg.getEndStop())
                    .sectionMin(leg.getSectionMin())
                    .startX(leg.getStartX())
                    .startX(leg.getStartX())
                    .startY(leg.getStartY())
                    .endX(leg.getEndX())
                    .endY(leg.getEndY())
                    .polyline(polyline)
                    .build());
        }

        // 3. 매핑 후 상태 검증 (방어 로직)
        if (isLaneValid && laneIndex != lanePolylines.size()) {
            log.error("[ODsay] 폴리라인 매핑 상태 이상: 총 {}개의 폴리라인 중 {}개만 매핑되었습니다.",
                    lanePolylines.size(), laneIndex);
        }

        return result;
    }

   
    // ── WALK 구간 OSRM 도보 경로 조회 ───────────────────────────

    private List<List<Double>> fetchWalkPolyline(TransitLegDto leg) {
        // 좌표가 없는 경우 (출발/도착 지점이 동일하거나 0인 경우) 빈 리스트 반환
        if (leg.getStartX() == 0 && leg.getStartY() == 0) {
            log.warn("[WALK] 좌표 없음, 폴리라인 생략");
            return new ArrayList<>();
        }

        try {
            // OSRM은 Bakery 객체를 받으므로 임시 Bakery 생성
            Bakery walkFrom = Bakery.ofCoord("도보출발", leg.getStartY(), leg.getStartX());
            Bakery walkTo   = Bakery.ofCoord("도보도착", leg.getEndY(),   leg.getEndX());

            SegmentRouteDto walkRoute = osrmService.getRoute("foot", walkFrom, walkTo);
            log.debug("[WALK] 도보 폴리라인 {}개 좌표 조회 완료", walkRoute.getPolyline().size());
            return walkRoute.getPolyline();

        } catch (Exception e) {
            log.warn("[WALK] OSRM 도보 경로 조회 실패, 빈 폴리라인 반환: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── subPath → TransitLegDto 변환 ────────────────────────────

    private TransitLegDto toTransitLeg(OdsayPathResponse.OdsaySubPath sub) {
        int trafficType = sub.trafficType();
        String type;
        if (trafficType == 1)      type = "SUBWAY";
        else if (trafficType == 2) type = "BUS";
        else                       type = "WALK";

        TransitLegDto.Builder builder = TransitLegDto.builder()
                .type(type)
                .sectionMin(sub.sectionTime())
                .startX(sub.startX())   // ← 좌표 세팅
                .startY(sub.startY())
                .endX(sub.endX())
                .endY(sub.endY())
                .polyline(new ArrayList<>());

        if (trafficType != 3) {
            List<OdsayPathResponse.OdsayLane> lanes = sub.lane();
            if (lanes == null || lanes.isEmpty()) {
                builder.lineName("정보없음")
                       .startStop(sub.startName())
                       .endStop(sub.endName());
            } else {
                OdsayPathResponse.OdsayLane lane = lanes.get(0);
                // 지하철: 노선명, 버스: 버스번호
                String lineName = trafficType == 1 ? lane.name() : lane.busNo();
                builder.lineName(lineName != null ? lineName : "정보없음")
                       .startStop(sub.startName())
                       .endStop(sub.endName());
            }
        } else {
            // WALK는 정류소 이름 없음
            builder.lineName("")
                   .startStop("")
                   .endStop("");
        }

        return builder.build();
    }
}
