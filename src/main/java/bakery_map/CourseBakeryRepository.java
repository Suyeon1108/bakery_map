import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseBakeryRepository extends JpaRepository<CourseBakery, Integer> {

    /**
     * 특정 코스의 빵집 목록을 방문 순서(visit_no) 오름차순으로 조회
     * CourseBakery → Bakery JOIN으로 Bakery 엔티티만 반환
     */
    @Query("SELECT b FROM CourseBakery cb JOIN cb.bakery b " +
           "WHERE cb.course.id = :courseId " +
           "ORDER BY cb.visitNo ASC")
    List<Bakery> findBakeriesByCourseIdOrderByVisitNo(@Param("courseId") Integer courseId);
}
