package restful.api.eztrain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import restful.api.eztrain.entity.CoachEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.RegisterCoachRequest;
import restful.api.eztrain.repository.CoachRepository;
import restful.api.eztrain.repository.TrainRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
public class CoachService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public CoachResponse register(Authentication authentication, RegisterCoachRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));                

        if (coachRepository.findByCoachType(request.getCoachType()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach already registered");
        }

        CoachEntity coach = new CoachEntity();
        coach.setCoachNumber(request.getCoachNumber());
        coach.setCoachType(request.getCoachType());
        coach.setUserEntity(user);

        try {
            coachRepository.save(coach);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Register coach failed");
        }

        return ResponseMapper.ToCoachResponseMapper(coach);
        
    }

    @Transactional(readOnly = true)
    public CoachResponse get(Authentication authentication, String strCoachId) {        
        Long coachId;

        try {        
            coachId = Long.parseLong(strCoachId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }        

        CoachEntity coach = coachRepository.findById(coachId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));                ;

        return ResponseMapper.ToCoachResponseMapper(coach);
    }

}
