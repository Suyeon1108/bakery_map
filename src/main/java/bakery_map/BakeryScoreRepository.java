package bakery_map;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BakeryScoreRepository extends JpaRepository<BakeryScore, Integer> {
	BakeryScore findByBakery(Bakery bakery);
}
