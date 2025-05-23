package restful.api.eztrain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.RegisterStationRequest;
import restful.api.eztrain.model.SearchStationRequest;
import restful.api.eztrain.model.StationResponse;
import restful.api.eztrain.model.UpdateStationRequest;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
public class StationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public StationResponse register(Authentication authentication, RegisterStationRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));        

        if (stationRepository.findByCode(request.getCode()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Station already registered");
        }

        StationEntity station = new StationEntity();
        station.setCode(request.getCode());
        station.setName(request.getName());
        station.setCity(request.getCity());
        station.setProvince(request.getProvince());
        station.setIsActive(true);
        station.setUserEntity(user);

        try {
            stationRepository.save(station);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Register station failed");
        }

        return ResponseMapper.ToStationResponseMapper(station);
    }

    @Transactional(readOnly = true)
    public StationResponse get(String strStationId) {
        Long stationId;

        try {
            stationId = Long.parseLong(strStationId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        StationEntity station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));

        return ResponseMapper.ToStationResponseMapper(station);
    }

    @Transactional(readOnly = true)
    public Page<StationResponse> getAllStations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StationEntity> stations = stationRepository.findAll(pageable);

        List<StationResponse> stationResponses = stations
                                            .getContent()
                                            .stream()
                                            .map(p -> ResponseMapper.ToStationResponseMapper(p))
                                            .collect(Collectors.toList()); 

        return new PageImpl<>(stationResponses, pageable, stations.getTotalElements());
    }

    @Transactional
    public StationResponse update(Authentication authentication, UpdateStationRequest request, String strStationId) {
        Long stationId;

        try {
            stationId = Long.parseLong(strStationId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        StationEntity station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));

        if (Objects.nonNull(request.getCode())) {
            station.setCode(request.getCode());
        }

        if (Objects.nonNull(request.getName())) {
            station.setName(request.getName());
        }

        if (Objects.nonNull(request.getCity())) {
            station.setCity(request.getCity());
        }

        if (Objects.nonNull(request.getProvince())) {
            station.setProvince(request.getProvince());
        }

        station.setUserEntity(user);

        try {
            stationRepository.save(station);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update station failed");
        }        

        return ResponseMapper.ToStationResponseMapper(station);
    }

    @Transactional
    public void delete(String strStationId) {
        Long stationId;

        try {
            stationId = Long.parseLong(strStationId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        StationEntity station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));
        
        try {
            stationRepository.delete(station);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete station failed");
        }        
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Page<StationResponse> search(SearchStationRequest request) {        
        Specification<StationEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();            

            if (Objects.nonNull(request.getCode())) {
                predicates.add(builder.or(
                    builder.like(root.get("code"), "%"+request.getCode()+"%")                    
                ));
            }
            
            if (Objects.nonNull(request.getName())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("name"), "%"+request.getName()+"%")
                ));
            }

            if (Objects.nonNull(request.getCity())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("city"), "%"+request.getCity()+"%")
                ));
            }

            if (Objects.nonNull(request.getProvince())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("province"), "%"+request.getProvince()+"%")
                ));
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<StationEntity> stations = stationRepository.findAll(specification, pageable);
        List<StationResponse> stationResponses = stations
                                                    .getContent()
                                                    .stream()
                                                    .map(station -> ResponseMapper.ToStationResponseMapper(station))
                                                    .collect(Collectors.toList());

        return new PageImpl<>(stationResponses, pageable, stations.getTotalElements());
    }
}
