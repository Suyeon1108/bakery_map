package bakery_map;

import jakarta.persistence.*;

@Entity
@Table(name = "bakery_flag")
public class BakeryFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bakery_id")
    private Integer bakeryId;

    // ===================== UD =====================
    private Integer wheelchair;
    private Integer ramp;
    private Integer barrier_free;
    private Integer disabled;
    private Integer braille;
    private Integer elevator;
    private Integer stroller;
    private Integer nursing_room;
    private Integer kids_zone;
    private Integer parking;
    private Integer restroom;
    private Integer seating;

    // ===================== ESG (전체 확장) =====================
    private Integer animal_welfare;
    private Integer sponsorship;
    private Integer scholarship;

    private Integer whole_wheat;
    private Integer gluten_free;
    private Integer sugar_free;
    private Integer low_sugar;

    private Integer eco_bag;
    private Integer eco_packaging;
    private Integer paper_bag;
    private Integer paper_packaging;
    private Integer recycling;
    private Integer zero_waste;

    private Integer donation;
    private Integer social_contribution;
    private Integer social_enterprise;

    private Integer fair_trade;

    private Integer pesticide_free;
    private Integer organic;
    private Integer antibiotic_free;

    private Integer local_wheat;

    private Integer vegan;
    private Integer additive_free;
    private Integer preservative_free;

    private Integer natural_fermentation;

    private Integer brown_rice;
    private Integer sprouted;
    private Integer black_rice;
    private Integer black_barley;
    private Integer oats;
    private Integer mixed_grains;

    // ===================== Getter / Setter =====================

    public Integer getBakeryId() { return bakeryId; }
    public void setBakeryId(Integer v) { this.bakeryId = v; }

    // UD
    public Integer getWheelchair() { return wheelchair; }
    public void setWheelchair(Integer v) { this.wheelchair = v; }

    public Integer getRamp() { return ramp; }
    public void setRamp(Integer v) { this.ramp = v; }

    public Integer getBarrier_free() { return barrier_free; }
    public void setBarrier_free(Integer v) { this.barrier_free = v; }

    public Integer getDisabled() { return disabled; }
    public void setDisabled(Integer v) { this.disabled = v; }

    public Integer getBraille() { return braille; }
    public void setBraille(Integer v) { this.braille = v; }

    public Integer getElevator() { return elevator; }
    public void setElevator(Integer v) { this.elevator = v; }

    public Integer getStroller() { return stroller; }
    public void setStroller(Integer v) { this.stroller = v; }

    public Integer getNursing_room() { return nursing_room; }
    public void setNursing_room(Integer v) { this.nursing_room = v; }

    public Integer getKids_zone() { return kids_zone; }
    public void setKids_zone(Integer v) { this.kids_zone = v; }

    public Integer getParking() { return parking; }
    public void setParking(Integer v) { this.parking = v; }

    public Integer getRestroom() { return restroom; }
    public void setRestroom(Integer v) { this.restroom = v; }

    public Integer getSeating() { return seating; }
    public void setSeating(Integer v) { this.seating = v; }

    // ESG
    public Integer getAnimal_welfare() { return animal_welfare; }
    public void setAnimal_welfare(Integer v) { this.animal_welfare = v; }

    public Integer getSponsorship() { return sponsorship; }
    public void setSponsorship(Integer v) { this.sponsorship = v; }

    public Integer getScholarship() { return scholarship; }
    public void setScholarship(Integer v) { this.scholarship = v; }

    public Integer getWhole_wheat() { return whole_wheat; }
    public void setWhole_wheat(Integer v) { this.whole_wheat = v; }

    public Integer getGluten_free() { return gluten_free; }
    public void setGluten_free(Integer v) { this.gluten_free = v; }

    public Integer getSugar_free() { return sugar_free; }
    public void setSugar_free(Integer v) { this.sugar_free = v; }

    public Integer getLow_sugar() { return low_sugar; }
    public void setLow_sugar(Integer v) { this.low_sugar = v; }

    public Integer getEco_bag() { return eco_bag; }
    public void setEco_bag(Integer v) { this.eco_bag = v; }

    public Integer getEco_packaging() { return eco_packaging; }
    public void setEco_packaging(Integer v) { this.eco_packaging = v; }

    public Integer getPaper_bag() { return paper_bag; }
    public void setPaper_bag(Integer v) { this.paper_bag = v; }

    public Integer getPaper_packaging() { return paper_packaging; }
    public void setPaper_packaging(Integer v) { this.paper_packaging = v; }

    public Integer getRecycling() { return recycling; }
    public void setRecycling(Integer v) { this.recycling = v; }

    public Integer getZero_waste() { return zero_waste; }
    public void setZero_waste(Integer v) { this.zero_waste = v; }

    public Integer getDonation() { return donation; }
    public void setDonation(Integer v) { this.donation = v; }

    public Integer getSocial_contribution() { return social_contribution; }
    public void setSocial_contribution(Integer v) { this.social_contribution = v; }

    public Integer getSocial_enterprise() { return social_enterprise; }
    public void setSocial_enterprise(Integer v) { this.social_enterprise = v; }

    public Integer getFair_trade() { return fair_trade; }
    public void setFair_trade(Integer v) { this.fair_trade = v; }

    public Integer getPesticide_free() { return pesticide_free; }
    public void setPesticide_free(Integer v) { this.pesticide_free = v; }

    public Integer getOrganic() { return organic; }
    public void setOrganic(Integer v) { this.organic = v; }

    public Integer getAntibiotic_free() { return antibiotic_free; }
    public void setAntibiotic_free(Integer v) { this.antibiotic_free = v; }

    public Integer getLocal_wheat() { return local_wheat; }
    public void setLocal_wheat(Integer v) { this.local_wheat = v; }

    public Integer getVegan() { return vegan; }
    public void setVegan(Integer v) { this.vegan = v; }

    public Integer getAdditive_free() { return additive_free; }
    public void setAdditive_free(Integer v) { this.additive_free = v; }

    public Integer getPreservative_free() { return preservative_free; }
    public void setPreservative_free(Integer v) { this.preservative_free = v; }

    public Integer getNatural_fermentation() { return natural_fermentation; }
    public void setNatural_fermentation(Integer v) { this.natural_fermentation = v; }

    public Integer getBrown_rice() { return brown_rice; }
    public void setBrown_rice(Integer v) { this.brown_rice = v; }

    public Integer getSprouted() { return sprouted; }
    public void setSprouted(Integer v) { this.sprouted = v; }

    public Integer getBlack_rice() { return black_rice; }
    public void setBlack_rice(Integer v) { this.black_rice = v; }

    public Integer getBlack_barley() { return black_barley; }
    public void setBlack_barley(Integer v) { this.black_barley = v; }

    public Integer getOats() { return oats; }
    public void setOats(Integer v) { this.oats = v; }

    public Integer getMixed_grains() { return mixed_grains; }
    public void setMixed_grains(Integer v) { this.mixed_grains = v; }
}
