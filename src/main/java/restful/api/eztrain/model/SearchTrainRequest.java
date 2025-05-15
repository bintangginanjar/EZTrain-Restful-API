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
public class SearchTrainRequest {

    private String name;

    private String trainType;
    
    private String operator;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

}
