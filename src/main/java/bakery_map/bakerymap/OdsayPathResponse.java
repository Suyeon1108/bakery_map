package bakery_map.bakerymap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OdsayPathResponse(
        @JsonProperty("result") OdsayResult result,
        @JsonProperty("error")  OdsayErrorResponse error
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayResult(
            @JsonProperty("path") List<OdsayPath> path
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayPath(
            @JsonProperty("info")    OdsayPathInfo info,
            @JsonProperty("subPath") List<OdsaySubPath> subPath
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayPathInfo(
            @JsonProperty("totalTime")          int totalTime,
            @JsonProperty("totalDistance")      int totalDistance,
            @JsonProperty("totalWalk")          int totalWalk,
            @JsonProperty("busTransitCount")    int busTransitCount,
            @JsonProperty("subwayTransitCount") int subwayTransitCount,
            @JsonProperty("mapObj")             String mapObj
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsaySubPath(
            @JsonProperty("trafficType") int trafficType,
            @JsonProperty("sectionTime") int sectionTime,
            @JsonProperty("startName")   String startName,
            @JsonProperty("endName")     String endName,
            @JsonProperty("startX")      double startX,
            @JsonProperty("startY")      double startY,
            @JsonProperty("endX")        double endX,
            @JsonProperty("endY")        double endY,
            @JsonProperty("lane")        List<OdsayLane> lane
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayLane(
            @JsonProperty("name")  String name,
            @JsonProperty("busNo") String busNo
    ) {}
}
