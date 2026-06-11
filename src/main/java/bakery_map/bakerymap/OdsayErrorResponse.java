package bakery_map.bakerymap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OdsayErrorResponse(
        int code,
        @JsonProperty("msg") String message
) {}
