package restful.api.eztrain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutePriceResponse {

    private Long id;

    private Double price;

    private Long coachTypeId;

    private String coachType;

    private Long routeId;

    private String origin;

    private String destination;

}
