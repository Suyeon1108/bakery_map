package bakery_map;

import jakarta.persistence.*;

@Entity
@Table(name = "course_bakery")
public class CourseBakery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "bakery_id")
    private Integer bakeryId;

    @Column(name = "visit_no")
    private Integer sequence;

    // getter / setter

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getBakeryId() { return bakeryId; }
    public void setBakeryId(Integer bakeryId) { this.bakeryId = bakeryId; }

    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }
}
