package bakery_map.bakerymap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransitLegDto {

    private final String type;
    private final String lineName;
    private final String startStop;
    private final String endStop;
    private final int sectionMin;
    private final List<List<Double>> polyline;
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;

    private TransitLegDto(Builder builder) {
        this.type       = builder.type;
        this.lineName   = builder.lineName  != null ? builder.lineName  : "";
        this.startStop  = builder.startStop != null ? builder.startStop : "";
        this.endStop    = builder.endStop   != null ? builder.endStop   : "";
        this.sectionMin = builder.sectionMin;
        this.polyline   = builder.polyline  != null
                ? Collections.unmodifiableList(new ArrayList<>(builder.polyline))
                : Collections.emptyList();
        this.startX = builder.startX;
        this.startY = builder.startY;
        this.endX   = builder.endX;
        this.endY   = builder.endY;
    }

    public String getType()                 { return type; }
    public String getLineName()             { return lineName; }
    public String getStartStop()            { return startStop; }
    public String getEndStop()              { return endStop; }
    public int getSectionMin()              { return sectionMin; }
    public List<List<Double>> getPolyline() { return polyline; }
    public double getStartX()               { return startX; }
    public double getStartY()               { return startY; }
    public double getEndX()                 { return endX; }
    public double getEndY()                 { return endY; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String type;
        private String lineName;
        private String startStop;
        private String endStop;
        private int sectionMin;
        private List<List<Double>> polyline;
        private double startX;
        private double startY;
        private double endX;
        private double endY;

        private Builder() {}

        public Builder type(String type)                     { this.type = type;             return this; }
        public Builder lineName(String lineName)             { this.lineName = lineName;     return this; }
        public Builder startStop(String startStop)           { this.startStop = startStop;   return this; }
        public Builder endStop(String endStop)               { this.endStop = endStop;       return this; }
        public Builder sectionMin(int sectionMin)            { this.sectionMin = sectionMin; return this; }
        public Builder polyline(List<List<Double>> polyline) { this.polyline = polyline;     return this; }
        public Builder startX(double startX)                 { this.startX = startX;         return this; }
        public Builder startY(double startY)                 { this.startY = startY;         return this; }
        public Builder endX(double endX)                     { this.endX = endX;             return this; }
        public Builder endY(double endY)                     { this.endY = endY;             return this; }

        public TransitLegDto build() {
            if (type == null || type.isBlank()) {
                throw new IllegalStateException("TransitLegDto: type은 필수값입니다.");
            }
            return new TransitLegDto(this);
        }
    }

    @Override
    public String toString() {
        return "TransitLegDto{type='" + type + "', lineName='" + lineName +
                "', startStop='" + startStop + "', endStop='" + endStop +
                "', sectionMin=" + sectionMin + ", polyline.size=" + polyline.size() +
                ", start=(" + startY + "," + startX + "), end=(" + endY + "," + endX + ")}";
    }
}
