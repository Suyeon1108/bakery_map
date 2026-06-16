package bakery_map.bakerymap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import bakery_map.Bakery;
import bakery_map.CourseBakery;
import bakery_map.BakeryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CourseBakeryRepository courseBakeryRepository;
    private final BakeryRepository bakeryRepository;
    private final OsrmService osrmService;
    private final OdsayService odsayService;

    public CourseRouteResponse getCourseRoute(Integer courseId, String mode) {
        List<CourseBakery> list =courseBakeryRepository.findByCourseIdOrderBySequence(courseId);
        if (list.isEmpty()) {
            throw new RuntimeException("코스에 빵집이 없습니다.");
        }

        List<Integer> ids = list.stream()
                .map(CourseBakery::getBakeryId)
                .toList();
        List<Bakery> bakeryList = bakeryRepository.findByIdIn(ids);

        Map<Integer, Bakery> bakeryMap = bakeryList.stream().collect(Collectors.toMap(Bakery::getId, b -> b));
        List<Bakery> bakeries = new ArrayList<>();

        for (Integer id : ids) {
            Bakery b = bakeryMap.get(id);
            if (b != null) {
                bakeries.add(b);
            }
        } 
        if (bakeries.size() < 2) {
            throw new RuntimeException("코스에 빵집이 2개 이상 필요합니다.");
        } 

        List<SegmentRouteDto> segments = new ArrayList<>();
        for (int i = 0; i < bakeries.size() - 1; i++) {
            Bakery from = bakeries.get(i);
            Bakery to = bakeries.get(i + 1);

            log.debug("[Routing] 경로 계산: {} -> {} (mode={})", from.getName(), to.getName(), mode);
            SegmentRouteDto segment;

            switch (mode.toLowerCase()) {
                case "bus":
                    log.debug("[Routing] Odsay 호출");
                    segment = odsayService.getRoute(from, to); 
                    break;

                case "car":
                    segment = osrmService.getRoute(mode, from, to);
                    break;
                case "walk": 
                    segment = osrmService.getRoute("foot", from, to); 
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 이동수단: " + mode);
            }
                segments.add(segment);
        }
 
        int totalDistance = 0;
        int totalDuration = 0;

        for (SegmentRouteDto seg : segments) {
            totalDistance += seg.getDistanceM();
            totalDuration += seg.getDurationSec();
        }
 
        return CourseRouteResponse.builder()
                .courseId(courseId)
                .mode(mode)
                .totalDistanceM(totalDistance)
                .totalDurationSec(totalDuration)
                .segments(segments)
                .build();
    }
}
