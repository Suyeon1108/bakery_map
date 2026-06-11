package bakery_map;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BakeryRepository extends JpaRepository<Bakery, Integer> {

    // 🔥 IN 조회 (핵심)
    List<Bakery> findByIdIn(List<Integer> ids);

}
