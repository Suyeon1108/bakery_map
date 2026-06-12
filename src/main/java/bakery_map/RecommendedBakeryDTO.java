package bakery_map;

public record RecommendedBakeryDTO(
        Long bakeryId,
        Integer rankNo,
        Double combinedScore
) {
}
