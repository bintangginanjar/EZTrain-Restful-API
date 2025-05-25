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
public class UpdateCoachRequest {

    @NotNull
    @JsonIgnore    
    private Long id;

    private String coachName;

    private Integer coachNumber;
    
    private Long coachTypeId;

}
