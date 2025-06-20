package restful.api.eztrain.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRoutePriceRequest {

    private String origin;

    private String destination;

    private String coachType;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

}
