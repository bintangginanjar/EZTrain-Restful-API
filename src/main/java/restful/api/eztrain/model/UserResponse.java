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
public class UserResponse {

    private String email;

    private String fullName;

    private String phoneNumber;

    private boolean isVerified;

    private boolean isActive;

    private List<String> role;    
        
}
