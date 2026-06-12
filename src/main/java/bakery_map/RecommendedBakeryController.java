package bakery_map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bakery")
public class RecommendedBakeryController {

    private final RecommendedBakeryRepository recommendedBakeryRepository;

    @GetMapping("/recommended")
    public List<RecommendedBakeryDTO> getRecommendedBakeries() {
        return recommendedBakeryRepository.findAllRecommended();
    }
}
