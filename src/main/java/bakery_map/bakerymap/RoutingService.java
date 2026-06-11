package bakery_map.bakerymap;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import bakery_map.Bakery;
import bakery_map.CourseBakery;
import bakery_map.BakeryRepository;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CourseBakeryRepository courseBakeryRepository;
    private final BakeryRepository bakeryRepository;
    private final OsrmService osrmService;
    private final OdsayService odsayService;

    public CourseRouteResponse getCourseRoute(Integer courseId, String mode) {

        // 1️⃣ 코스에 포함된 빵집 (순서 유지)
        List<CourseBakery> list =
                courseBakeryRepository.findByCourseIdOrderBySequence(courseId);

        if (list.isEmpty()) {
            throw new RuntimeException("코스에 빵집이 없습니다.");
        }

        // 2️⃣ bakeryId 리스트 추출
        List<Integer> ids = list.stream()
                .map(CourseBakery::getBakeryId)
                .toList();

        // 3️⃣ 🔥 IN 조회 (한 번에)
        List<Bakery> bakeryList = bakeryRepository.findByIdIn(ids);

        // 4️⃣ id → Bakery 매핑
        Map<Integer, Bakery> bakeryMap = bakeryList.stream()
                .collect(Collectors.toMap(Bakery::getId, b -> b));

        // 5️⃣ 순서 유지하여 Bakery 리스트 재구성
        List<Bakery> bakeries = new ArrayList<>();

        for (Integer id : ids) {
            Bakery b = bakeryMap.get(id);
            if (b != null) {
                bakeries.add(b);
            }
        }

        // 6️⃣ 최소 2개 필요
        if (bakeries.size() < 2) {
            throw new RuntimeException("코스에 빵집이 2개 이상 필요합니다.");
        }

        // 7️⃣ 구간별 경로 계산
        List<SegmentRouteDto> segments = new ArrayList<>();

        for (int i = 0; i < bakeries.size() - 1; i++) {

            Bakery from = bakeries.get(i);
            Bakery to = bakeries.get(i + 1);

System.out.println("===== 경로 계산 시작 =====");
System.out.println("mode = " + mode);
System.out.println("from = " + from.getName());
System.out.println("to = " + to.getName());

	    SegmentRouteDto segment;

	switch (mode.toLowerCase()) {
	    case "bus":
	        System.out.println("ODsay 호출 시작");
	        segment = odsayService.getRoute(from, to);
	        System.out.println("ODsay 호출 성공");
	        break;

	    case "car":
	    case "walk":
	    case "bike":
	        segment = osrmService.getRoute(mode, from, to);
	        break;

	    default:
	        throw new IllegalArgumentException("지원하지 않는 이동수단: " + mode);
	}
            segments.add(segment);
        }

        // 8️⃣ 총 거리 / 시간 계산
        int totalDistance = 0;
        int totalDuration = 0;

        for (SegmentRouteDto seg : segments) {
            totalDistance += seg.getDistanceM();
            totalDuration += seg.getDurationSec();
        }

        // 9️⃣ 응답
        return CourseRouteResponse.builder()
                .courseId(courseId)
                .mode(mode)
                .totalDistanceM(totalDistance)
                .totalDurationSec(totalDuration)
                .segments(segments)
                .build();
    }
}
