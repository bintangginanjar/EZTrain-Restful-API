package restful.api.eztrain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTrainRequest {

    @NotBlank
    @JsonIgnore    
    private String id;

    private String name;

    private String trainType;
    
    private String operator;

    private Boolean isActive;

}
