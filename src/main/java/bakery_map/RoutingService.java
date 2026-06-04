import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoutingService {

    private final CourseBakeryRepository courseBakeryRepository;
    private final OsrmService osrmService;
    private final OdsayService odsayService;


    public RoutingService(CourseBakeryRepository courseBakeryRepository,
                          OsrmService osrmService,
                          OdsayService odsayService) {
        this.courseBakeryRepository = courseBakeryRepository;
        this.osrmService            = osrmService;
        this.odsayService           = odsayService;
    }
    /**
     * 코스 전체 경로 계산
     * - visitNo 순서대로 빵집 목록 조회
     * - A→B, B→C 구간별로 경로 계산 (CompletableFuture 병렬 처리)
     *
     * @ param courseId 코스 ID
     * @ param mode     이동수단 (foot | car | transit)
     */
    public CourseRouteResponse getFullCourseRoute(Integer courseId, String mode) {
        List<Bakery> bakeries =
                courseBakeryRepository.findBakeriesByCourseIdOrderByVisitNo(courseId);

        if (bakeries.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 코스입니다: " + courseId);
        }
        if (bakeries.size() < 2) {
            throw new IllegalArgumentException("코스에 최소 2개 이상의 목적지가 필요합니다.");
        }

        // 1. 각 구간별 경로 조회를 병렬(CompletableFuture) 작업으로 생성
        List<CompletableFuture<SegmentRouteDto>> futureSegments = new ArrayList<>();

        for (int i = 0; i < bakeries.size() - 1; i++) {
            final Bakery from = bakeries.get(i);
            final Bakery to = bakeries.get(i + 1);

            log.debug("[Route] {} → {} (mode={})", from.getName(), to.getName(), mode);

            SegmentRouteDto segment;
            if ("foot".equals(mode) || "car".equals(mode)) {
                segment = osrmService.getRoute(mode, from, to);
            } else if ("transit".equals(mode)) {
                segment = odsayService.getRoute(from, to);
            } else {
                throw new IllegalArgumentException("지원하지 않는 이동수단: " + mode);
            }

            segments.add(segment);
        }

        // 2. 모든 병렬 작업이 끝날 때까지 대기 후 결과 수집
        int totalDurationSec = 0;
        int totalDistanceM   = 0;
        for (SegmentRouteDto seg : segments) {
            totalDurationSec += seg.getDurationSec();
            totalDistanceM   += seg.getDistanceM();
        }
        // 3. 최종 응답 조립
        return CourseRouteResponse.builder()
                .courseId(courseId)
                .mode(mode)
                .totalDurationSec(totalDurationSec)
                .totalDistanceM(totalDistanceM)
                .segments(segments)
                .build();
    }
}