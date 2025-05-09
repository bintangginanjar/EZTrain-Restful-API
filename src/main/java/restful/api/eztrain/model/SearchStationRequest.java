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
public class SearchStationRequest {

    private String code;
    
    private String name;

    private String city;

    private String province;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

}
