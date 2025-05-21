package restful.api.eztrain.mapper;

import java.util.List;
import java.util.stream.Collectors;

import restful.api.eztrain.entity.CoachEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.TrainEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.ForgotPasswordResponse;
import restful.api.eztrain.model.StationResponse;
import restful.api.eztrain.model.TokenResponse;
import restful.api.eztrain.model.TrainResponse;
import restful.api.eztrain.model.UserResponse;

public class ResponseMapper {

    public static UserResponse ToUserResponseMapper(UserEntity user) {        
        List<String> roles = user.getRoles().stream().map(p -> p.getName()).toList();

        return UserResponse.builder()                
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())   
                .isVerified(user.getIsVerified())
                .isActive(user.getIsActive())
                .role(roles)
                .build();
    }

    public static List<UserResponse> ToUserResponseListMapper(List<UserEntity> users) {
        return users.stream()
                            .map(
                                p -> new UserResponse(
                                    p.getEmail(),
                                    p.getFullName(),
                                    p.getPhoneNumber(),   
                                    p.getIsVerified(),
                                    p.getIsActive(),
                                    p.getRoles().stream().map(s -> s.getName()).toList()
                                )).collect(Collectors.toList());
    }

    public static TokenResponse ToTokenResponseMapper(UserEntity user, String token, List<String> roles) {
        return TokenResponse.builder()
                .email(user.getEmail())
                .token(token)
                .roles(roles)
                .build();

    }

    public static ForgotPasswordResponse ToForgotPasswordResponseMapper(String email, String token) {
        return ForgotPasswordResponse.builder()
                .email(email)
                .token(token)
                .build();
    }

    public static StationResponse ToStationResponseMapper(StationEntity station) {
        return StationResponse.builder()
                .id(station.getId())                
                .code(station.getCode())                
                .name(station.getName())
                .city(station.getCity())
                .province(station.getProvince())
                .isActive(station.getIsActive())
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
                                    p.getProvince(),
                                    p.getIsActive()
                                )).collect(Collectors.toList());
    }

    public static TrainResponse ToTrainResponseMapper(TrainEntity train) {
        List<String> coaches = train.getCoaches().stream().map(p -> p.getCoachName()).toList();

        return TrainResponse.builder()
                .id(train.getId())                                
                .name(train.getName())
                .trainType(train.getTrainType())
                .operator(train.getOperator())
                .isActive(train.getIsActive())
                .coaches(coaches)
                .build();
    }

    public static List<TrainResponse> ToTrainResponseListMapper(List<TrainEntity> trains) {
        return trains.stream()
                            .map(
                                p -> new TrainResponse(
                                    p.getId(),                                                         
                                    p.getName(),
                                    p.getTrainType(),
                                    p.getOperator(),
                                    p.getIsActive(),
                                    p.getCoaches().stream().map(s -> s.getCoachName()).toList()
                                )).collect(Collectors.toList());
    }

    public static CoachResponse ToCoachResponseMapper(CoachEntity coach) {
        List<String> seats = coach.getSeats().stream().map(p -> p.getSeatNumber()).toList();

        return CoachResponse.builder()
                .id(coach.getId())
                .coachName(coach.getCoachName())
                .coachNumber(coach.getCoachNumber())
                .coachTypeId(coach.getCoachTypeEntity().getId())
                .coachTypeName(coach.getCoachTypeEntity().getName())
                .seats(seats)
                .build();
    }
}
