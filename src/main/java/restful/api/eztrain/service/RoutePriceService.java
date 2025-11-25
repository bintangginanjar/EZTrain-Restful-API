package restful.api.eztrain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import restful.api.eztrain.entity.CoachTypeEntity;
import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.RoutePriceEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.RegisterRoutePriceRequest;
import restful.api.eztrain.model.RoutePriceResponse;
import restful.api.eztrain.model.SearchRoutePriceRequest;
import restful.api.eztrain.model.UpdateRoutePriceRequest;
import restful.api.eztrain.repository.CoachTypeRepository;
import restful.api.eztrain.repository.RoutePriceRepository;
import restful.api.eztrain.repository.RouteRepository;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
public class RoutePriceService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    CoachTypeRepository coachTypeRepository;

    @Autowired
    RoutePriceRepository routePriceRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    ValidationService validationService;

    @Transactional
    public RoutePriceResponse register(Authentication authentication, RegisterRoutePriceRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));        

        RouteEntity route = routeRepository.findById(request.getRouteId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

        CoachTypeEntity coachType = coachTypeRepository.findById(request.getCoachTypeId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach type not found"));

        if (routePriceRepository.findByRouteEntityAndCoachTypeEntity(route, coachType).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route price already registered");
        }

        RoutePriceEntity routePrice = new RoutePriceEntity();
        routePrice.setPrice(request.getPrice());
        routePrice.setCoachTypeEntity(coachType);
        routePrice.setRouteEntity(route);
        routePrice.setUserEntity(user);

        try {
            routePriceRepository.save(routePrice);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Register route price failed");
        }

        return ResponseMapper.ToRoutePriceResponseMapper(routePrice);
    }

    @Transactional(readOnly = true)
    public RoutePriceResponse get(Long routePriceId) {
        RoutePriceEntity routePrice = routePriceRepository.findById(routePriceId)
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route price not found"));

        return ResponseMapper.ToRoutePriceResponseMapper(routePrice);                                        
    }

    @Transactional
    public Page<RoutePriceResponse> getAllRoutePrices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoutePriceEntity> routePrices = routePriceRepository.findAll(pageable);

        List<RoutePriceResponse> routePriceResponses = routePrices
                                            .getContent()
                                            .stream()
                                            .map(p -> ResponseMapper.ToRoutePriceResponseMapper(p))
                                            .collect(Collectors.toList()); 

        return new PageImpl<>(routePriceResponses, pageable, routePrices.getTotalElements());
    }

    @Transactional
    public RoutePriceResponse update(Authentication authentication, UpdateRoutePriceRequest request, Long routePriceId) {
        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                            
        RoutePriceEntity routePrice = routePriceRepository.findById(routePriceId)
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route price not found"));                                            

        if (Objects.nonNull(request.getCoachTypeId())) {
            CoachTypeEntity coachType = coachTypeRepository.findById(request.getCoachTypeId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach type not found"));

            routePrice.setCoachTypeEntity(coachType);
        }
        
        if (Objects.nonNull(request.getRouteId())) {
            RouteEntity route = routeRepository.findById(request.getRouteId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

            routePrice.setRouteEntity(route);
        }

        if (Objects.nonNull(request.getPrice())) {
            routePrice.setPrice(request.getPrice());
        }

        routePrice.setUserEntity(user);

        try {
            routePriceRepository.save(routePrice);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update route price failed");
        }

        return ResponseMapper.ToRoutePriceResponseMapper(routePrice);
    } 

    @Transactional
    public void delete(Long routePriceId) {
        RoutePriceEntity routePrice = routePriceRepository.findById(routePriceId)
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route price not found"));

        try {
            routePriceRepository.delete(routePrice);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete route prices failed");
        }                                        
    }

    @Transactional(readOnly = true)
    public Page<RoutePriceResponse> search(SearchRoutePriceRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());        
        Optional<StationEntity> origin = stationRepository.findByCode(request.getOrigin());        
        Optional<StationEntity> destination = stationRepository.findByCode(request.getDestination());
        Optional<RouteEntity> route = routeRepository.findByOriginAndDestination(origin.get(), destination.get());   
        Optional<CoachTypeEntity> coachType = coachTypeRepository.findByName(request.getCoachType());       
    
        Page<RoutePriceEntity> routePrices = routePriceRepository.findByRouteEntityOrCoachTypeEntity(route.get(), coachType.get(), pageable);
        List<RoutePriceResponse> responses = routePrices
                                                    .getContent()
                                                    .stream()
                                                    .map(p -> ResponseMapper.ToRoutePriceResponseMapper(p))
                                                    .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, routePrices.getTotalElements());
    }
}
