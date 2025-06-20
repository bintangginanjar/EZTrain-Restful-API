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
import restful.api.eztrain.entity.CoachTypeEntity;
import restful.api.eztrain.entity.SeatEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.RegisterCoachRequest;
import restful.api.eztrain.model.SearchCoachRequest;
import restful.api.eztrain.model.UpdateCoachRequest;
import restful.api.eztrain.repository.CoachRepository;
import restful.api.eztrain.repository.CoachTypeRepository;
import restful.api.eztrain.repository.SeatRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
public class CoachService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private CoachTypeRepository coachTypeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public CoachResponse register(Authentication authentication, RegisterCoachRequest request) {
        validationService.validate(request);
        
        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));                

        if (coachRepository.findByCoachName(request.getCoachName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach already registered");
        }        
        
        CoachTypeEntity coachType = coachTypeRepository.findById(request.getCoachTypeId())
                                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach type not found"));

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(request.getCoachName());
        coach.setCoachNumber(request.getCoachNumber());
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);

        try {
            coachRepository.save(coach);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Register coach failed");
        }

        return ResponseMapper.ToCoachResponseMapper(coach);
        
    }

    @Transactional(readOnly = true)
    public CoachResponse get(Long coachId) {             
        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));                ;

        return ResponseMapper.ToCoachResponseMapper(coach);
    }

    @Transactional(readOnly = true)
    public Page<CoachResponse> getAllCoaches(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CoachEntity> coaches = coachRepository.findAll(pageable);

        List<CoachResponse> coachResponses = coaches
                                            .getContent()
                                            .stream()
                                            .map(p -> ResponseMapper.ToCoachResponseMapper(p))
                                            .collect(Collectors.toList()); 

        return new PageImpl<>(coachResponses, pageable, coaches.getTotalElements());
    }    

    @Transactional
    public CoachResponse update(Authentication authentication, UpdateCoachRequest request, Long coachId) {    
        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));       
        
        CoachTypeEntity coachType = coachTypeRepository.findById(request.getCoachTypeId())
                                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach type not found"));

        if (Objects.nonNull(request.getCoachName())) {
            coach.setCoachName(request.getCoachName());
        }
        
        if (Objects.nonNull(request.getCoachTypeId())) {
            coach.setCoachTypeEntity(coachType);
        }
        
        if (Objects.nonNull(request.getCoachNumber())) {
            coach.setCoachNumber(request.getCoachNumber());
        }

        coach.setUserEntity(user);

        try {
            coachRepository.save(coach);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update coach failed");
        }

        return ResponseMapper.ToCoachResponseMapper(coach);
    }

    @Transactional
    public void delete(long coachId) {    
        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));

        try {
            coachRepository.delete(coach);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete coach failed");
        }
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Page<CoachResponse> search(SearchCoachRequest request) {
        Specification<CoachEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();                    

            if (Objects.nonNull(request.getCoachName())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("coachName"), "%"+request.getCoachName()+"%")
                ));
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<CoachEntity> coaches = coachRepository.findAll(specification, pageable);        
        List<CoachResponse> coachResponses = coaches
                                                    .getContent()
                                                    .stream()
                                                    .map(coach -> ResponseMapper.ToCoachResponseMapper(coach))
                                                    .collect(Collectors.toList());

        return new PageImpl<>(coachResponses, pageable, coaches.getTotalElements());
    }

    @Transactional
    public CoachResponse assignSeat(Long coachId, Long seatId) {            
        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));

        SeatEntity seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        coach.getSeats().add(seat);

        try {
            coachRepository.save(coach);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assigning seat to coach failed");
        }

        return ResponseMapper.ToCoachResponseMapper(coach);
    }

    @Transactional
    public CoachResponse removeSeat(Long coachId, Long seatId) {        
        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));

        SeatEntity seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        coach.getSeats().remove(seat);

        return ResponseMapper.ToCoachResponseMapper(coach);
    }

}
