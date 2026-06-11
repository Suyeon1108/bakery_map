package bakery_map.bakerymap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransitLegDto {

    private final String type;         // "BUS", "SUBWAY", "WALK"
    private final String lineName;     // 버스 번호 or 지하철 노선명
    private final String startStop;    // 승차 정류소
    private final String endStop;      // 하차 정류소
    private final int sectionMin;      // 구간 소요 시간 (분)
    private final List<List<Double>> polyline; // 폴리라인 좌표 [[lat, lon], ...]

    // ── 생성자 (Builder를 통해서만 생성) ──────────────────────────

    private TransitLegDto(Builder builder) {
        this.type       = builder.type;
        this.lineName   = builder.lineName   != null ? builder.lineName   : "";
        this.startStop  = builder.startStop  != null ? builder.startStop  : "";
        this.endStop    = builder.endStop    != null ? builder.endStop    : "";
        this.sectionMin = builder.sectionMin;
        this.polyline   = builder.polyline   != null
                ? Collections.unmodifiableList(new ArrayList<>(builder.polyline))
                : Collections.emptyList();
    }

    // ── Getter ────────────────────────────────────────────────────

    public String getType()                    { return type; }
    public String getLineName()                { return lineName; }
    public String getStartStop()               { return startStop; }
    public String getEndStop()                 { return endStop; }
    public int getSectionMin()                 { return sectionMin; }
    public List<List<Double>> getPolyline()    { return polyline; }

    // ── Builder ───────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String type;
        private String lineName;
        private String startStop;
        private String endStop;
        private int sectionMin;
        private List<List<Double>> polyline;

        private Builder() {}

        public Builder type(String type)                        { this.type = type;             return this; }
        public Builder lineName(String lineName)                { this.lineName = lineName;     return this; }
        public Builder startStop(String startStop)              { this.startStop = startStop;   return this; }
        public Builder endStop(String endStop)                  { this.endStop = endStop;       return this; }
        public Builder sectionMin(int sectionMin)               { this.sectionMin = sectionMin; return this; }
        public Builder polyline(List<List<Double>> polyline)    { this.polyline = polyline;     return this; }

        public TransitLegDto build() {
            if (type == null || type.isBlank()) {
                throw new IllegalStateException("TransitLegDto: type은 필수값입니다.");
            }
            return new TransitLegDto(this);
        }
    }

    // ── toString (디버깅용) ───────────────────────────────────────

    @Override
    public String toString() {
        return "TransitLegDto{" +
                "type='" + type + '\'' +
                ", lineName='" + lineName + '\'' +
                ", startStop='" + startStop + '\'' +
                ", endStop='" + endStop + '\'' +
                ", sectionMin=" + sectionMin +
                ", polyline.size=" + polyline.size() +
                '}';
    }
}
