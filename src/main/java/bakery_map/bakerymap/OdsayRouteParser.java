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
                    // loadLane 응답: x=경도, y=위도 → 카카오맵 [lat, lon]으로 변환
                    lanePolyline.add(List.of(pos.y(), pos.x()));
                }
            }
            allPolylines.add(lanePolyline);
        }

        return allPolylines;
    }

    // ── legs와 폴리라인 병합 ─────────────────────────────────────

    public List<TransitLegDto> mergeLegsAndPolylines(
            List<TransitLegDto> legs,
            List<List<List<Double>>> lanePolylines) {

        List<TransitLegDto> result = new ArrayList<>();
        int laneIndex = 0;

        for (TransitLegDto leg : legs) {
            List<List<Double>> polyline;

            if ("WALK".equals(leg.getType())) {
                polyline = new ArrayList<>();
            } else {
                polyline = (laneIndex < lanePolylines.size())
                        ? lanePolylines.get(laneIndex++)
                        : new ArrayList<>();
            }

            result.add(TransitLegDto.builder()
                    .type(leg.getType())
                    .lineName(leg.getLineName())
                    .startStop(leg.getStartStop())
                    .endStop(leg.getEndStop())
                    .sectionMin(leg.getSectionMin())
                    .polyline(polyline)
                    .build());
        }

        if (laneIndex < lanePolylines.size()) {
            log.warn("[ODsay] lanePolylines 미사용 항목 존재: 전체 {}개 중 {}개만 매핑됨",
                    lanePolylines.size(), laneIndex);
        }

        return result;
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
                .polyline(new ArrayList<>());

        if (trafficType != 3) {
            List<OdsayPathResponse.OdsayLane> lanes = sub.lane();
            if (lanes == null || lanes.isEmpty()) {
                builder.lineName("정보없음")
                       .startStop(sub.startName())
                       .endStop(sub.endName());
            } else {
                OdsayPathResponse.OdsayLane lane = lanes.get(0);
                String lineName = trafficType == 1 ? lane.name() : lane.busNo();
                builder.lineName(lineName)
                       .startStop(sub.startName())
                       .endStop(sub.endName());
            }
        }

        return builder.build();
    }
}
