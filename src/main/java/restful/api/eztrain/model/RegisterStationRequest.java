package restful.api.eztrain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterStationRequest {

    @NotBlank
    private String code;
    
    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String province;

}
