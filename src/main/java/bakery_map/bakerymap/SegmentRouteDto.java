package bakery_map.bakerymap;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * 단일 구간(A → B) 경로 응답 DTO
 * - foot / car : polyline에 좌표 배열 존재, transit은 null
 * - transit    : polyline은 빈 리스트, transit 객체 안에 leg별 폴리라인 존재
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentRouteDto {

    private String from;
    private String to;
    private String mode;
    private int durationSec;
    private int distanceM;
    private List<List<Double>> polyline;
    private TransitDetailDto transit;

    // ── 기본 생성자 ───────────────────────────────────────
    public SegmentRouteDto() {}

    // ── 전체 생성자 ───────────────────────────────────────
    public SegmentRouteDto(String from, String to, String mode,
                           int durationSec, int distanceM,
                           List<List<Double>> polyline, TransitDetailDto transit) {
        this.from        = from;
        this.to          = to;
        this.mode        = mode;
        this.durationSec = durationSec;
        this.distanceM   = distanceM;
        this.polyline    = polyline;
        this.transit     = transit;
    }

    // ── Getter ────────────────────────────────────────────
    public String getFrom()               { return from; }
    public String getTo()                 { return to; }
    public String getMode()               { return mode; }
    public int getDurationSec()           { return durationSec; }
    public int getDistanceM()             { return distanceM; }
    public List<List<Double>> getPolyline()   { return polyline; }
    public TransitDetailDto getTransit()  { return transit; }

    // ── Setter ────────────────────────────────────────────
    public void setFrom(String from)                    { this.from = from; }
    public void setTo(String to)                        { this.to = to; }
    public void setMode(String mode)                    { this.mode = mode; }
    public void setDurationSec(int durationSec)         { this.durationSec = durationSec; }
    public void setDistanceM(int distanceM)             { this.distanceM = distanceM; }
    public void setPolyline(List<List<Double>> polyline)    { this.polyline = polyline; }
    public void setTransit(TransitDetailDto transit)    { this.transit = transit; }

    // ── Builder ───────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String from;
        private String to;
        private String mode;
        private int durationSec;
        private int distanceM;
        private List<List<Double>> polyline;
        private TransitDetailDto transit;

        public Builder from(String from)                  { this.from = from;               return this; }
        public Builder to(String to)                      { this.to = to;                   return this; }
        public Builder mode(String mode)                  { this.mode = mode;               return this; }
        public Builder durationSec(int durationSec)       { this.durationSec = durationSec; return this; }
        public Builder distanceM(int distanceM)           { this.distanceM = distanceM;     return this; }
        public Builder polyline(List<List<Double>> polyline)  { this.polyline = polyline;       return this; }
        public Builder transit(TransitDetailDto transit)  { this.transit = transit;         return this; }

        public SegmentRouteDto build() {
            return new SegmentRouteDto(from, to, mode, durationSec, distanceM, polyline, transit);
        }
    }
}
