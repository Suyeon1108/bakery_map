package bakery_map.bakerymap;

import java.util.List;

/**
 * 대중교통 경로 상세 정보 DTO
 * SegmentRouteDto.transit 필드로 포함됨
 */
public class TransitDetailDto {

    private int totalWalkMin;
    private int transferCount;
    private List<TransitLegDto> legs;

    // ── 기본 생성자 ───────────────────────────────────────
    public TransitDetailDto() {}

    // ── 전체 생성자 ───────────────────────────────────────
    public TransitDetailDto(int totalWalkMin, int transferCount, List<TransitLegDto> legs) {
        this.totalWalkMin  = totalWalkMin;
        this.transferCount = transferCount;
        this.legs          = legs;
    }

    // ── Getter ────────────────────────────────────────────
    public int getTotalWalkMin()          { return totalWalkMin; }
    public int getTransferCount()         { return transferCount; }
    public List<TransitLegDto> getLegs()  { return legs; }

    // ── Setter ────────────────────────────────────────────
    public void setTotalWalkMin(int totalWalkMin)          { this.totalWalkMin = totalWalkMin; }
    public void setTransferCount(int transferCount)        { this.transferCount = transferCount; }
    public void setLegs(List<TransitLegDto> legs)          { this.legs = legs; }

    // ── Builder ───────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int totalWalkMin;
        private int transferCount;
        private List<TransitLegDto> legs;

        public Builder totalWalkMin(int totalWalkMin)        { this.totalWalkMin = totalWalkMin;   return this; }
        public Builder transferCount(int transferCount)      { this.transferCount = transferCount; return this; }
        public Builder legs(List<TransitLegDto> legs)        { this.legs = legs;                  return this; }

        public TransitDetailDto build() {
            return new TransitDetailDto(totalWalkMin, transferCount, legs);
        }
    }
}
