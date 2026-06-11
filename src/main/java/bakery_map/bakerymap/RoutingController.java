package bakery_map.bakerymap;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class RoutingController {

    private final RoutingService routingService;

    private static final Set<String> VALID_MODES = Set.of("car", "walk", "bike", "bus");

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    /**
     * GET /api/courses/{courseId}/route?mode=car
     * GET /api/courses/{courseId}/route?mode=bus
     */
    @GetMapping("/{courseId}/route")
    public ResponseEntity<CourseRouteResponse> getCourseRoute(
            @PathVariable Integer courseId,
            @RequestParam(defaultValue = "car") String mode) {

        if (!VALID_MODES.contains(mode)) {
            throw new IllegalArgumentException("지원하지 않는 이동수단: " + mode);
        }

        return ResponseEntity.ok(
                routingService.getCourseRoute(courseId, mode)
        );
    }
}
