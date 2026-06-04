import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes") 
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    /**
     * 코스 경로 조회 API
     * GET /api/routes/course/123?mode=foot
     */
    @GetMapping("/{courseId}/route")
    public ResponseEntity<CourseRouteResponse> getCourseRoute(
            @PathVariable Integer courseId,
            @RequestParam(defaultValue = "car") String mode) {

        return ResponseEntity.ok(
                routingService.getFullCourseRoute(courseId, mode));
    }
}