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
import restful.api.eztrain.entity.CoachEntity;
import restful.api.eztrain.entity.TrainEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.RegisterTrainRequest;
import restful.api.eztrain.model.SearchTrainRequest;
import restful.api.eztrain.model.TrainResponse;
import restful.api.eztrain.model.UpdateTrainRequest;
import restful.api.eztrain.repository.CoachRepository;
import restful.api.eztrain.repository.TrainRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
public class TrainService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public TrainResponse register(Authentication authentication, RegisterTrainRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));        

        if (trainRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Train already registered");
        }

        TrainEntity train = new TrainEntity();
        train.setName(request.getName());
        train.setTrainType(request.getTrainType());
        train.setOperator(request.getOperator());
        train.setIsActive(true);
        train.setUserEntity(user);

        try {
            trainRepository.save(train);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Register train failed");
        }   

        return ResponseMapper.ToTrainResponseMapper(train);
    }

    @Transactional(readOnly = true)
    public TrainResponse get(Long trainId) {                
        TrainEntity train = trainRepository.findById(trainId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));

        return ResponseMapper.ToTrainResponseMapper(train);
    }

    @Transactional(readOnly = true)
    public Page<TrainResponse> getAllTrains(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainEntity> trains = trainRepository.findAll(pageable);

        List<TrainResponse> trainResponses = trains
                                            .getContent()
                                            .stream()
                                            .map(p -> ResponseMapper.ToTrainResponseMapper(p))
                                            .collect(Collectors.toList()); 

        return new PageImpl<>(trainResponses, pageable, trains.getTotalElements());
    }

    @Transactional
    public TrainResponse update(Authentication authentication, UpdateTrainRequest request, Long trainId) {
        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TrainEntity train = trainRepository.findById(trainId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));

        if (Objects.nonNull(request.getName())) {
            train.setName(request.getName());
        }

        if (Objects.nonNull(request.getTrainType())) {
            train.setTrainType(request.getTrainType());
        }

        if (Objects.nonNull(request.getOperator())) {
            train.setOperator(request.getOperator());
        }

        if (Objects.nonNull(request.getIsActive())) {
            train.setIsActive(request.getIsActive());
        }

        train.setUserEntity(user);

        try {
            trainRepository.save(train);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update train failed");
        }        

        return ResponseMapper.ToTrainResponseMapper(train);
    }

    @Transactional
    public void delete(Long trainId) {        
        TrainEntity train = trainRepository.findById(trainId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));

        try {
            trainRepository.delete(train);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete train failed");
        }
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Page<TrainResponse> search(SearchTrainRequest request) {
        Specification<TrainEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();            
            
            if (Objects.nonNull(request.getName())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("name"), "%"+request.getName()+"%")
                ));
            }

            if (Objects.nonNull(request.getTrainType())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("trainType"), "%"+request.getTrainType()+"%")
                ));
            }

            if (Objects.nonNull(request.getOperator())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("operator"), "%"+request.getOperator()+"%")
                ));
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<TrainEntity> trains = trainRepository.findAll(specification, pageable);
        List<TrainResponse> trainResponses = trains
                                                    .getContent()
                                                    .stream()
                                                    .map(train -> ResponseMapper.ToTrainResponseMapper(train))
                                                    .collect(Collectors.toList());

        return new PageImpl<>(trainResponses, pageable, trains.getTotalElements());
    }

    @Transactional
    public TrainResponse assignCoach(Long trainId, Long coachId) {        
        TrainEntity train = trainRepository.findById(trainId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));

        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));

        train.getCoaches().add(coach);

        try {
            trainRepository.save(train);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assigning coach to train failed");
        }

        return ResponseMapper.ToTrainResponseMapper(train);
    }

    @Transactional
    public TrainResponse removeCoach(Long trainId, Long coachId) {                    
        TrainEntity train = trainRepository.findById(trainId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));

        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));             
        
        train.getCoaches().remove(coach);        

        return ResponseMapper.ToTrainResponseMapper(train);
    }
}
