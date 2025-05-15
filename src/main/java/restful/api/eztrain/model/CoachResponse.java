package restful.api.eztrain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoachResponse {
    
    private Long id;

    private Integer coachNumber;
    
    private String coachType;

}
