package bakery_map;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BakeryFlagRepository extends JpaRepository<BakeryFlag, Integer> {
    BakeryFlag findByBakeryId(Integer bakeryId);
}
