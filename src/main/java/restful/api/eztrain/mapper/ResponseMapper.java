package restful.api.eztrain.mapper;

import java.util.List;
import java.util.stream.Collectors;

import restful.api.eztrain.entity.CoachEntity;
import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.RoutePriceEntity;
import restful.api.eztrain.entity.SeatEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.TrainEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.ForgotPasswordResponse;
import restful.api.eztrain.model.RoutePriceResponse;
import restful.api.eztrain.model.RouteResponse;
import restful.api.eztrain.model.SeatResponse;
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

    public static SeatResponse ToSeatResponseMapper(SeatEntity seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .build();
    }

    public static RouteResponse ToRouteResponseMapper(RouteEntity route) {
        return RouteResponse.builder()
                .id(route.getId())
                .originId(route.getOrigin().getId())
                .origin(route.getOrigin().getName())
                .destId(route.getDestination().getId())
                .destination(route.getDestination().getName())                
                .tripDistance(route.getTripDistance())                
                .tripDuration(route.getTripDuration())
                .build();
    }

    public static RoutePriceResponse ToRoutePriceResponseMapper(RoutePriceEntity routePrice) {
        return RoutePriceResponse.builder()
                .id(routePrice.getId())
                .price(routePrice.getPrice())
                .coachTypeId(routePrice.getCoachTypeEntity().getId())
                .coachType(routePrice.getCoachTypeEntity().getName())
                .routeId(routePrice.getRouteEntity().getId())
                .origin(routePrice.getRouteEntity().getOrigin().getName())
                .destination(routePrice.getRouteEntity().getDestination().getName())                
                .build();
        }
}
