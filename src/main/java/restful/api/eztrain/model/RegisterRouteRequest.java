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
public class RegisterRouteRequest {
    
    @NotNull
    private Long originId;

    @NotNull
    private Long destId;
    
    @NotNull
    private Double tripDistance;

    @NotNull
    private Double tripDuration;

}
