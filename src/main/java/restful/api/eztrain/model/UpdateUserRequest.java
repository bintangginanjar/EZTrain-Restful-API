package restful.api.eztrain.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(max = 128)
    private String password;

    private String fullName;

    private String phoneNumber;

    private boolean isVerified;

    private boolean isActive;

}
