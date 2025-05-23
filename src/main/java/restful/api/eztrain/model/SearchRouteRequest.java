package restful.api.eztrain.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRouteRequest {
    
    private String origin;

    private String destination;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

}
