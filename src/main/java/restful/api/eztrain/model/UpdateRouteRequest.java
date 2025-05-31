package restful.api.eztrain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private Long id;

    private Long originId;

    private Long destId;
    
    private Double tripDistance;
    
    private Double tripDuration;

}
