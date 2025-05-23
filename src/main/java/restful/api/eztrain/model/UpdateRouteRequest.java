package restful.api.eztrain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRouteRequest {

    @JsonIgnore
    @NotBlank
    private String id;

    private String strOriginId;

    private String strDestId;
    
    private Double tripDistance;
    
    private Integer tripDuration;

}
