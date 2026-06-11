package bakery_map.bakerymap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * ODsay loadLane API 응답 매핑 Record
 *
 * 실제 ODsay loadLane 응답 구조:
 * {
 *   "result": {
 *     "lane": [
 *       {
 *         "section": [
 *           {
 *             "graphPos": [
 *               { "x": 127.123, "y": 37.456 },
 *               ...
 *             ]
 *           }
 *         ]
 *       }
 *     ]
 *   }
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OdsayLaneResponse(
        OdsayLaneResult result,
	List<OdsayErrorResponse> error
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
