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
public class UpdateCoachRequest {

    @NotBlank
    @JsonIgnore    
    private String id;

    private String coachName;

    private Integer coachNumber;
    
    private String coachType;

}
