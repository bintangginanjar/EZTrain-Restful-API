package restful.api.eztrain.model;

import java.util.List;

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

    private String coachName;

    private Integer coachNumber;
    
    private Long coachTypeId;

    private String coachTypeName;

    private List<String> seats;

}
