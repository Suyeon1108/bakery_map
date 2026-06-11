package bakery_map;

import jakarta.persistence.*;

@Entity
@Table(name = "bakery_score")
public class BakeryScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double ud_score;
    private Double esg_score;

    @ManyToOne
    @JoinColumn(name = "bakery_id")
    private Bakery bakery;

    // getter/setter
    public Double getUd_score() { return ud_score; }
    public void setUd_score(Double ud_score) { this.ud_score = ud_score; }

    public Double getEsg_score() { return esg_score; }
    public void setEsg_score(Double esg_score) { this.esg_score = esg_score; }

    public void setBakery(Bakery bakery) { this.bakery = bakery; }
}
