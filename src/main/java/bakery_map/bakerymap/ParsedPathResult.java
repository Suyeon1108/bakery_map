package bakery_map.bakerymap;

import java.util.List;

public record ParsedPathResult(
        String mapObj,
        int totalTimeSec,
        int totalDistanceM,
        int transferCount,
        int totalWalkMin,
        List<TransitLegDto> legs
) {}
