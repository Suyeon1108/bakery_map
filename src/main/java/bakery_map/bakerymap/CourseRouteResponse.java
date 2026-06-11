package bakery_map.bakerymap;

import java.util.List;

/**
 * 전체 코스 경로 응답 DTO
 * GET /api/courses/{courseId}/route?mode= 의 최종 응답
 */
public class CourseRouteResponse {

    private Integer courseId;
    private String mode;
    private int totalDurationSec;
    private int totalDistanceM;
    private List<SegmentRouteDto> segments;

    // ── 기본 생성자 ───────────────────────────────────────
    public CourseRouteResponse() {}

    // ── 전체 생성자 ───────────────────────────────────────
    public CourseRouteResponse(Integer courseId, String mode,
                               int totalDurationSec, int totalDistanceM,
                               List<SegmentRouteDto> segments) {
        this.courseId        = courseId;
        this.mode            = mode;
        this.totalDurationSec = totalDurationSec;
        this.totalDistanceM  = totalDistanceM;
        this.segments        = segments;
    }

    // ── Getter ────────────────────────────────────────────
    public Integer getCourseId()                  { return courseId; }
    public String getMode()                       { return mode; }
    public int getTotalDurationSec()              { return totalDurationSec; }
    public int getTotalDistanceM()                { return totalDistanceM; }
    public List<SegmentRouteDto> getSegments()    { return segments; }

    // ── Setter ────────────────────────────────────────────
    public void setCourseId(Integer courseId)                    { this.courseId = courseId; }
    public void setMode(String mode)                             { this.mode = mode; }
    public void setTotalDurationSec(int totalDurationSec)        { this.totalDurationSec = totalDurationSec; }
    public void setTotalDistanceM(int totalDistanceM)            { this.totalDistanceM = totalDistanceM; }
    public void setSegments(List<SegmentRouteDto> segments)      { this.segments = segments; }

    // ── Builder ───────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer courseId;
        private String mode;
        private int totalDurationSec;
        private int totalDistanceM;
        private List<SegmentRouteDto> segments;

        public Builder courseId(Integer courseId)                  { this.courseId = courseId;               return this; }
        public Builder mode(String mode)                           { this.mode = mode;                       return this; }
        public Builder totalDurationSec(int totalDurationSec)      { this.totalDurationSec = totalDurationSec; return this; }
        public Builder totalDistanceM(int totalDistanceM)          { this.totalDistanceM = totalDistanceM;   return this; }
        public Builder segments(List<SegmentRouteDto> segments)    { this.segments = segments;               return this; }

        public CourseRouteResponse build() {
            return new CourseRouteResponse(courseId, mode, totalDurationSec, totalDistanceM, segments);
        }
    }
}
