package restful.api.eztrain.mapper;

import java.util.List;

import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.UserResponse;

public class ResponseMapper {

    public static UserResponse ToUserResponseMapper(UserEntity user) {        
        List<String> roles = user.getRoles().stream().map(p -> p.getName()).toList();

        return UserResponse.builder()                
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())   
                .isVerified(user.isVerified())
                .role(roles)
                .build();
    }

}
