package restful.api.eztrain.model;

import jakarta.validation.constraints.NotBlank;
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
    
    @NotBlank
    private String strOriginId;

    @NotBlank
    private String strDestId;
    
    @NotNull
    private Double tripDistance;

    @NotNull
    private Integer tripDuration;

}
