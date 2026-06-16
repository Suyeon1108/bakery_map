package bakery_map.bakerymap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OdsayLaneResponse(
        OdsayLaneResult result,
	OdsayErrorResponse error
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayLaneResult(
            List<OdsayLane> lane
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayLane(
            List<OdsaySection> section
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsaySection(
            List<OdsayGraphPos> graphPos
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayGraphPos(
            double x,  // 경도 (longitude)
            double y   // 위도 (latitude)
    ) {}
}

