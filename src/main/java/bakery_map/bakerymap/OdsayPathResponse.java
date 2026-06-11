package bakery_map.bakerymap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * ODsay searchPubTransPathT API 응답 매핑 Record
 *
 * 실제 ODsay 응답 구조:
 * {
 *   "result": {
 *     "path": [
 *       {
 *         "info": {
 *           "totalTime": 45,
 *           "totalDistance": 12000,
 *           "totalWalk": 500,
 *           "busTransitCount": 1,
 *           "subwayTransitCount": 0,
 *           "mapObj": "21:11:0@21:12:0"
 *         },
 *         "subPath": [
 *           {
 *             "trafficType": 2,
 *             "sectionTime": 30,
 *             "startName": "강남역",
 *             "endName": "신촌역",
 *             "lane": [{ "busNo": "147" }]
 *           },
 *           ...
 *         ]
 *       }
 *     ]
 *   }
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OdsayPathResponse(
        OdsayResult result,
        List<OdsayErrorResponse> error
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayResult(
            List<OdsayPath> path
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayPath(
            OdsayPathInfo info,
            List<OdsaySubPath> subPath
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayPathInfo(
        @JsonProperty("totalTime")          int totalTime,      // 총 소요 시간 (분)
        @JsonProperty("totalDistance")      int totalDistance,  // 총 거리 (미터)
        @JsonProperty("totalWalk")          int totalWalk,      // 총 도보 거리 (미터)
        @JsonProperty("busTransitCount")    int busTransitCount,        // 버스 환승 횟수
        @JsonProperty("subwayTransitCount") int subwayTransitCount,     // 지하철 환승 횟수
        @JsonProperty("mapObj")             String mapObj       // loadLane 호출에 사용하는 식별자
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsaySubPath(
            int trafficType,        // 1=지하철, 2=버스, 3=도보
            int sectionTime,        // 구간 소요 시간 (분)
            String startName,       // 승차 정류소명
            String endName,         // 하차 정류소명
            List<OdsayLane> lane    // 노선 정보 (도보이면 null)
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OdsayLane(
            String name,   // 지하철 노선명
            String busNo   // 버스 번호
    ) {}
}
