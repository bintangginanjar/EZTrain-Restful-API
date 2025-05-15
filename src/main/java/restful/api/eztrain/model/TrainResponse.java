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
public class TrainResponse {

    private Long id;

    private String name;

    private String trainType;
    
    private String operator;

    private Boolean isActive;

    private List<String> coach;

}
