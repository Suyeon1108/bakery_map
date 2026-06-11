package bakery_map.bakerymap;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import bakery_map.CourseBakery;

public interface CourseBakeryRepository extends JpaRepository<CourseBakery, Integer> {

    // 코스에 포함된 빵집 순서대로 조회
    List<CourseBakery> findByCourseIdOrderBySequence(Integer courseId);

}
