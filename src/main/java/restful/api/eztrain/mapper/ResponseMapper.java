package restful.api.eztrain.mapper;

import java.util.List;
import java.util.stream.Collectors;

import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.StationResponse;
import restful.api.eztrain.model.TokenResponse;
import restful.api.eztrain.model.UserResponse;

public class ResponseMapper {

    public static UserResponse ToUserResponseMapper(UserEntity user) {        
        List<String> roles = user.getRoles().stream().map(p -> p.getName()).toList();

        return UserResponse.builder()                
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())   
                .isVerified(user.getIsVerified())
                .role(roles)
                .build();
    }

    public static TokenResponse ToTokenResponseMapper(UserEntity user, String token, List<String> roles) {
        return TokenResponse.builder()
                .email(user.getEmail())
                .token(token)
                .roles(roles)
                .build();

    }

    public static StationResponse ToStationResponseMapper(StationEntity station) {
        return StationResponse.builder()
                .id(station.getId())                
                .code(station.getCode())                
                .name(station.getName())
                .city(station.getCity())
                .province(station.getProvince())
                .build();
    }

    public static List<StationResponse> ToStationResponseListMapper(List<StationEntity> stations) {
        return stations.stream()
                            .map(
                                p -> new StationResponse(
                                    p.getId(),      
                                    p.getCode(),                
                                    p.getName(),
                                    p.getCity(),
                                    p.getProvince()
                                )).collect(Collectors.toList());
    }

}
