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
public class RegisterRoutePriceRequest {

    @NotNull
    private Long coachTypeId;

    @NotNull
    private Long routeId;    

    @NotNull
    private Double price;

}
