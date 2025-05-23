package restful.api.eztrain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StationResponse {

    private Long id;
    
    private String code;
    
    private String name;

    private String city;

    private String province;

    private Boolean isActive;

}
