package restful.api.eztrain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteResponse {

    private Long id;

    private Long originId;

    private String origin;
    
    private Long destId;

    private String destination;
    
    private Double tripDistance;
    
    private Double tripDuration;

}
