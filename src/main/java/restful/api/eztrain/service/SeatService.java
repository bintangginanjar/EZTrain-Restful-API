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
import restful.api.eztrain.entity.SeatEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.RegisterSeatRequest;
import restful.api.eztrain.model.SearchSeatRequest;
import restful.api.eztrain.model.SeatResponse;
import restful.api.eztrain.model.UpdateSeatRequest;
import restful.api.eztrain.repository.SeatRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
public class SeatService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public SeatResponse register(Authentication authentication, RegisterSeatRequest request) {
        validationService.validate(request);
        
        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (seatRepository.findBySeatNumber(request.getSeatNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat already registered");
        }

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber(request.getSeatNumber());
        seat.setUserEntity(user);        

        try {
            seatRepository.save(seat);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Register seat failed");
        }

        return ResponseMapper.ToSeatResponseMapper(seat);
    }

    @Transactional(readOnly = true)
    public SeatResponse get(String strSeatId) {
        Long seatId;

        try {        
            seatId = Long.parseLong(strSeatId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        SeatEntity seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        return ResponseMapper.ToSeatResponseMapper(seat);  
    }

    @Transactional(readOnly = true)
    public Page<SeatResponse> getAllSeats(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SeatEntity> seats = seatRepository.findAll(pageable);

        List<SeatResponse> seatResponses = seats
                                            .getContent()
                                            .stream()
                                            .map(p -> ResponseMapper.ToSeatResponseMapper(p))
                                            .collect(Collectors.toList()); 

        return new PageImpl<>(seatResponses, pageable, seats.getTotalElements());
    } 
    
    @Transactional
    public SeatResponse update(Authentication authentication, UpdateSeatRequest request, String strSeatId) {
        Long seatId;        

        try {        
            seatId = Long.parseLong(strSeatId);            
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SeatEntity seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));
                            
        if (Objects.nonNull(request.getSeatNumber())) {
            seat.setSeatNumber(request.getSeatNumber());
        }      
        
        seat.setUserEntity(user);

        try {
            seatRepository.save(seat);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update seat failed");
        }

        return ResponseMapper.ToSeatResponseMapper(seat);
    }

    @Transactional
    public void delete(String strSeatId) {
        Long seatId;        

        try {        
            seatId = Long.parseLong(strSeatId);            
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        SeatEntity seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        try {
            seatRepository.delete(seat);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete seat failed");
        }
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Page<SeatResponse> search(SearchSeatRequest request) {
        Specification<SeatEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();                    

            if (Objects.nonNull(request.getSeatNumber())) {
                predicates.add(builder.or(                    
                    builder.like(root.get("seatNumber"), "%"+request.getSeatNumber()+"%")
                ));
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<SeatEntity> seats = seatRepository.findAll(specification, pageable);        
        List<SeatResponse> seatResponses = seats
                                                .getContent()
                                                .stream()
                                                .map(seat -> ResponseMapper.ToSeatResponseMapper(seat))
                                                .collect(Collectors.toList());

        return new PageImpl<>(seatResponses, pageable, seats.getTotalElements());
    }
}
