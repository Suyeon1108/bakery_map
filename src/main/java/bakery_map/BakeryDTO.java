package bakery_map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;

@JsonPropertyOrder({
    "id",
    "name",
    "address",
    "description",
    "lng",
    "lat",
    "ud_score",
    "esg_score",
    "ud_flags",
    "esg_flags"
})

public class BakeryDTO {

    public Integer id;
    public String name;
    public String address;
    public String description;

    public Double lat;
    public Double lng;

    public Double ud_score;
    public Double esg_score;

    // 핵심 추가
    public Map<String, Object> ud_flags;
    public Map<String, Object> esg_flags;
}
