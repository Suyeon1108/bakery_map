package bakery_map;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecommendedBakeryRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<RecommendedBakeryDTO> findAllRecommended() {
        String sql = """
            SELECT bakery_id, rank_no, combined_score
            FROM recommended_bakeries
            ORDER BY rank_no
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new RecommendedBakeryDTO(
                        rs.getLong("bakery_id"),
                        rs.getInt("rank_no"),
                        rs.getDouble("combined_score")
                )
        );
    }
}
