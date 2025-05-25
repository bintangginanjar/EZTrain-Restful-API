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
public class RegisterCoachRequest {

    @NotBlank
    private String coachName;

    @NotNull
    private Integer coachNumber;
    
    @NotNull
    private Long coachTypeId;    

}
