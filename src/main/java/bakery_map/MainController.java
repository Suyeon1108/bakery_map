package bakery_map;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import bakery_map.bakerymap.CourseBakeryRepository;

@RestController
@RequestMapping("/bakery")
@CrossOrigin(origins = "*")
public class MainController {

    private final BakeryRepository bakeryRepo;
    private final BakeryScoreRepository scoreRepo;
    private final BakeryFlagRepository flagRepo;
    private final CourseInfoRepository courseRepo;
    private final CourseBakeryRepository courseBakeryRepo;
    private final JdbcTemplate jdbcTemplate;

public MainController(
        BakeryRepository b,
        BakeryScoreRepository s,
        BakeryFlagRepository f,
        CourseInfoRepository c,
        CourseBakeryRepository cb,
        JdbcTemplate jdbcTemplate) {

        this.bakeryRepo = b;
        this.scoreRepo = s;
        this.flagRepo = f;
        this.courseRepo = c;
        this.courseBakeryRepo = cb;
        this.jdbcTemplate = jdbcTemplate;
    }
    // ===================== 저장 API =====================
    @PostMapping("/save")
    @Transactional
    public String save(@RequestBody Map<String, Object> data) {

    System.out.println("===== 요청 데이터 =====");
    System.out.println(data);
    System.out.println("UD = " + data.get("ud"));
    System.out.println("ESG = " + data.get("esg"));

        Bakery bakery = new Bakery();
        bakery.setName((String) data.get("name"));
        bakery.setAddress((String) data.get("address"));
        bakery.setDescription((String) data.get("description"));
        bakery.setLng(Double.valueOf(data.get("lng").toString()));
        bakery.setLat(Double.valueOf(data.get("lat").toString()));

        Bakery saved = bakeryRepo.save(bakery);

	Map<String, Object> udFlags = new HashMap<>();
	Map<String, Object> esgFlags = new HashMap<>();

	Object udObjRaw = data.get("ud_flags");
	Object esgObjRaw = data.get("esg_flags");

	if (udObjRaw instanceof Map) {
	    udFlags = (Map<String, Object>) udObjRaw;
	} else {
	    System.out.println("ud_flags 타입 이상: " + udObjRaw);
	}

	if (esgObjRaw instanceof Map) {
	    esgFlags = (Map<String, Object>) esgObjRaw;
	} else {
	    System.out.println("esg_flags 타입 이상: " + esgObjRaw);
	}

        // score
        BakeryScore score = new BakeryScore();
        score.setBakery(saved);

	Object udObj = data.get("ud");
	Object esgObj = data.get("esg");

	score.setUd_score(udObj != null ? Double.valueOf(udObj.toString()) : 0.0);
	score.setEsg_score(esgObj != null ? Double.valueOf(esgObj.toString()) : 0.0);

        scoreRepo.save(score);

        // flag
        BakeryFlag flag = new BakeryFlag();
        flag.setBakeryId(saved.getId());

        // UD
	flag.setWheelchair(getInt(udFlags, "wheelchair"));
	flag.setRamp(getInt(udFlags, "ramp"));
	flag.setBarrier_free(getInt(udFlags, "barrier_free"));
	flag.setDisabled(getInt(udFlags, "disabled"));
	flag.setBraille(getInt(udFlags, "braille"));
	flag.setElevator(getInt(udFlags, "elevator"));
	flag.setStroller(getInt(udFlags, "stroller"));
	flag.setNursing_room(getInt(udFlags, "nursing_room"));
	flag.setKids_zone(getInt(udFlags, "kids_zone"));
	flag.setParking(getInt(udFlags, "parking"));
	flag.setRestroom(getInt(udFlags, "restroom"));
	flag.setSeating(getInt(udFlags, "seating"));

	// ESG
	flag.setAnimal_welfare(getInt(esgFlags, "animal_welfare"));
	flag.setSponsorship(getInt(esgFlags, "sponsorship"));
	flag.setScholarship(getInt(esgFlags, "scholarship"));

	flag.setWhole_wheat(getInt(esgFlags, "whole_wheat"));
	flag.setGluten_free(getInt(esgFlags, "gluten_free"));
	flag.setSugar_free(getInt(esgFlags, "sugar_free"));
	flag.setLow_sugar(getInt(esgFlags, "low_sugar"));

	flag.setEco_bag(getInt(esgFlags, "eco_bag"));
	flag.setEco_packaging(getInt(esgFlags, "eco_packaging"));
	flag.setPaper_bag(getInt(esgFlags, "paper_bag"));
	flag.setPaper_packaging(getInt(esgFlags, "paper_packaging"));
	flag.setRecycling(getInt(esgFlags, "recycling"));
	flag.setZero_waste(getInt(esgFlags, "zero_waste"));

	flag.setDonation(getInt(esgFlags, "donation"));
	flag.setSocial_contribution(getInt(esgFlags, "social_contribution"));
	flag.setSocial_enterprise(getInt(esgFlags, "social_enterprise"));

	flag.setFair_trade(getInt(esgFlags, "fair_trade"));

	flag.setPesticide_free(getInt(esgFlags, "pesticide_free"));
	flag.setOrganic(getInt(esgFlags, "organic"));
	flag.setAntibiotic_free(getInt(esgFlags, "antibiotic_free"));

	flag.setLocal_wheat(getInt(esgFlags, "local_wheat"));

	flag.setVegan(getInt(esgFlags, "vegan"));
	flag.setAdditive_free(getInt(esgFlags, "additive_free"));
	flag.setPreservative_free(getInt(esgFlags, "preservative_free"));

	flag.setNatural_fermentation(getInt(esgFlags, "natural_fermentation"));

	flag.setBrown_rice(getInt(esgFlags, "brown_rice"));
	flag.setSprouted(getInt(esgFlags, "sprouted"));
	flag.setBlack_rice(getInt(esgFlags, "black_rice"));
	flag.setBlack_barley(getInt(esgFlags, "black_barley"));
	flag.setOats(getInt(esgFlags, "oats"));
	flag.setMixed_grains(getInt(esgFlags, "mixed_grains"));

        flagRepo.save(flag);

        jdbcTemplate.execute("CALL refresh_course_bakery()");

        return "bakery OK";
    }

// ===================== 추천 API =====================
@GetMapping("/course/recommend")
public List<CourseInfo> recommend(
        @RequestParam(required = false) Integer parking,
        @RequestParam(required = false) Integer barrier_free,
        @RequestParam(required = false) Integer ramp,
        @RequestParam(required = false) Integer braille,
        @RequestParam(required = false) Integer stroller,
        @RequestParam(required = false) Integer nursing_room,
        @RequestParam(required = false) Integer kids_zone,
        @RequestParam(required = false) Integer wheelchair,
        @RequestParam(required = false) Integer disabled,
        @RequestParam(required = false) Integer restroom,
        @RequestParam(required = false) Integer seating,
        @RequestParam(required = false) Integer elevator,

        @RequestParam(required = false) Integer animal_welfare,
        @RequestParam(required = false) Integer gluten_free,
        @RequestParam(required = false) Integer sprouted,
        @RequestParam(required = false) Integer vegan,
        @RequestParam(required = false) Integer low_sugar,
        @RequestParam(required = false) Integer sugar_free,
        @RequestParam(required = false) Integer natural_fermentation,
        @RequestParam(required = false) Integer preservative_free
) {
    List<BakeryFlag> flags = flagRepo.findAll();

    Map<Integer, Integer> bakeryMatchScore = new HashMap<>();

    for (BakeryFlag f : flags) {
        int score = 0;

        if (parking != null && parking.equals(f.getParking())) score++;
        if (barrier_free != null && barrier_free.equals(f.getBarrier_free())) score++;
        if (ramp != null && ramp.equals(f.getRamp())) score++;
        if (braille != null && braille.equals(f.getBraille())) score++;
        if (stroller != null && stroller.equals(f.getStroller())) score++;
        if (nursing_room != null && nursing_room.equals(f.getNursing_room())) score++;
        if (kids_zone != null && kids_zone.equals(f.getKids_zone())) score++;
        if (wheelchair != null && wheelchair.equals(f.getWheelchair())) score++;
        if (disabled != null && disabled.equals(f.getDisabled())) score++;
        if (restroom != null && restroom.equals(f.getRestroom())) score++;
        if (seating != null && seating.equals(f.getSeating())) score++;
        if (elevator != null && elevator.equals(f.getElevator())) score++;

        if (animal_welfare != null && animal_welfare.equals(f.getAnimal_welfare())) score++;
        if (gluten_free != null && gluten_free.equals(f.getGluten_free())) score++;
        if (sprouted != null && sprouted.equals(f.getSprouted())) score++;
        if (vegan != null && vegan.equals(f.getVegan())) score++;
        if (low_sugar != null && low_sugar.equals(f.getLow_sugar())) score++;
        if (sugar_free != null && sugar_free.equals(f.getSugar_free())) score++;
        if (natural_fermentation != null && natural_fermentation.equals(f.getNatural_fermentation())) score++;
        if (preservative_free != null && preservative_free.equals(f.getPreservative_free())) score++;

        if (score > 0) {
            bakeryMatchScore.put(f.getBakeryId(), score);
        }
    }

    List<CourseBakery> courseBakeries = courseBakeryRepo.findAll();

    Map<Integer, Integer> courseScoreMap = new HashMap<>();

    for (CourseBakery cb : courseBakeries) {
        Integer score = bakeryMatchScore.get(cb.getBakeryId());

        if (score != null) {
            courseScoreMap.put(
                    cb.getCourseId(),
                    courseScoreMap.getOrDefault(cb.getCourseId(), 0) + score
            );
        }
    }

    List<Integer> sortedCourseIds = courseScoreMap.entrySet()
            .stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .map(Map.Entry::getKey)
            .limit(5)
            .toList();

    List<CourseInfo> result = new ArrayList<>();

    for (Integer courseId : sortedCourseIds) {
        courseRepo.findById(courseId).ifPresent(result::add);
    }

    return result;
}

    // ===================== 유틸 =====================
    private Integer getInt(Map<String, Object> map, String key) {

        if (map == null) return 0;

        Object value = map.get(key);

        if (value == null) return 0;

        return Integer.valueOf(value.toString());
    }


    @PostMapping("/course/save")
    public String saveCourse(@RequestBody Map<String, Object> data) {
	CourseInfo c = new CourseInfo();
	c.setName((String) data.get("name"));
	c.setDescription((String) data.get("description"));
	courseRepo.save(c);
        return "course bakery OK";
    }


  // 조회 API (JOIN)
    @GetMapping("/list")
    public List<BakeryDTO> list() {
        List<Bakery> bakeries = bakeryRepo.findAll();
        List<BakeryDTO> result = new ArrayList<>();

    for (Bakery b : bakeries) {
        BakeryDTO dto = new BakeryDTO();
        dto.id = b.getId();
        dto.name = b.getName(); dto.address = b.getAddress();
        dto.description = b.getDescription();
        dto.lng = b.getLng();
        dto.lat = b.getLat();
        BakeryScore score = scoreRepo.findByBakery(b);

        if (score != null) {
            dto.ud_score = score.getUd_score();
            dto.esg_score = score.getEsg_score();
        }

        BakeryFlag flag = flagRepo.findByBakeryId(b.getId());
	if (flag != null) {
	    Map<String, Object> ud = new HashMap<>();
	    ud.put("wheelchair", flag.getWheelchair());
	    ud.put("ramp", flag.getRamp());
	    ud.put("barrier_free", flag.getBarrier_free());
	    ud.put("disabled", flag.getDisabled());
	    ud.put("braille", flag.getBraille());
	    ud.put("elevator", flag.getElevator());
	    ud.put("stroller", flag.getStroller());
	    ud.put("nursing_room", flag.getNursing_room());
	    ud.put("kids_zone", flag.getKids_zone());
	    ud.put("parking", flag.getParking());
	    ud.put("restroom", flag.getRestroom());
	    ud.put("seating", flag.getSeating());

	    Map<String, Object> esg = new HashMap<>();
	    esg.put("eco_packaging", flag.getEco_packaging());
	    esg.put("animal_welfare", flag.getAnimal_welfare());
	    esg.put("vegan", flag.getVegan());
	    esg.put("organic", flag.getOrganic());
	    esg.put("recycling", flag.getRecycling());
	    esg.put("fair_trade", flag.getFair_trade());
	    esg.put("zero_waste", flag.getZero_waste());
	    esg.put("donation", flag.getDonation());
	    esg.put("local_wheat", flag.getLocal_wheat());
	    esg.put("gluten_free", flag.getGluten_free());
	    esg.put("sponsorship", flag.getSponsorship());
	    esg.put("scholarship", flag.getScholarship());
	    esg.put("whole_wheat", flag.getWhole_wheat());
	    esg.put("eco_bag", flag.getEco_bag());
	    esg.put("brown_rice", flag.getBrown_rice());
	    esg.put("sprouted", flag.getSprouted());
	    esg.put("black_rice", flag.getBlack_rice());
	    esg.put("additive_free", flag.getAdditive_free());
	    esg.put("black_barley", flag.getBlack_barley());
	    esg.put("oats", flag.getOats());
	    esg.put("paper_bag", flag.getPaper_bag());
	    esg.put("recycling", flag.getRecycling());
	    esg.put("social_contribution", flag.getSocial_contribution());
	    esg.put("sugar_free", flag.getSugar_free());
	    esg.put("natural_fermentation", flag.getNatural_fermentation());
	    esg.put("paper_packaging", flag.getPaper_packaging());
	    esg.put("fair_trade", flag.getFair_trade());
	    esg.put("pesticide_free", flag.getPesticide_free());
	    esg.put("organic", flag.getOrganic());
	    esg.put("zero_waste", flag.getZero_waste());
	    esg.put("antibiotic_free", flag.getAntibiotic_free());
	    esg.put("local_wheat", flag.getLocal_wheat());
	    esg.put("vegan", flag.getVegan());
	    esg.put("additive_free", flag.getAdditive_free());
	    esg.put("low_sugar", flag.getLow_sugar());
	    esg.put("social_enterprise", flag.getSocial_enterprise());
	    esg.put("preservative_free", flag.getPreservative_free());
	    esg.put("mixed_grains", flag.getMixed_grains());

	    dto.ud_flags = ud;
	    dto.esg_flags = esg;
	}
        result.add(dto);
        }
        return result;
    }

    @GetMapping("/course/list")
    public List<CourseInfo> getCourses() {
        return courseRepo.findAll();
    }

    @GetMapping("/course/{id}")
    public List<Map<String, Object>>
        getCourseDetail(@PathVariable Integer id) {
	    CourseInfo course = courseRepo.findById(id).orElse(null);
            // 1. 코스에 포함된 빵집 순서대로 가져오기
            List<CourseBakery> courseBakeries = courseBakeryRepo.findByCourseIdOrderBySequence(id);
//	    List<CourseBakery> courseBakeries = courseBakeryRepo.findCourseBakeries(id);
            List<Map<String, Object>> result = new ArrayList<>();

            // 2. bakery_info 조회해서 좌표 포함 반환
            for (CourseBakery cb : courseBakeries) {
            Bakery b = bakeryRepo.findById(cb.getBakeryId()).orElse(null);

            if (b != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", b.getId());
                map.put("name", b.getName());
                map.put("lat", b.getLat());
                map.put("lng", b.getLng());
                if (course != null) {
                    map.put("courseName", course.getName());
                    map.put("courseDescription", course.getDescription());
                }
	        result.add(map);
            }
        }
        return result;
    }

    @PostMapping("/course/bakery/save")
    public String saveCourseBakery(@RequestBody Map<String, Object> data) {
        CourseBakery cb = new CourseBakery();
        cb.setCourseId(Integer.valueOf(data.get("courseId").toString()));
        cb.setBakeryId(Integer.valueOf(data.get("bakeryId").toString()));
        cb.setSequence(Integer.valueOf(data.get("sequence").toString()));
        courseBakeryRepo.save(cb);
        return "course_bakery OK";
    }


}
