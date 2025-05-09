package restful.api.eztrain.service;

import java.util.Collections;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.RegisterUserRequest;
import restful.api.eztrain.model.UpdateUserRequest;
import restful.api.eztrain.model.UserResponse;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private ValidationService validationService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, 
                        PasswordEncoder passwordEncoder, ValidationService validationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        validationService.validate(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        RoleEntity role = roleRepository.findByName(request.getRole())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roles not found"));

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());       
        user.setPassword(passwordEncoder.encode(request.getPassword()));        
        user.setRoles(Collections.singletonList(role)); 
        user.setVerified(false);
        user.setActive(false);

        userRepository.save(user);        

        return ResponseMapper.ToUserResponseMapper(user);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Authentication authentication) {

        log.info("CURRENT NAME {}", authentication.getName());

        UserEntity user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));                    

        return ResponseMapper.ToUserResponseMapper(user);
    }

    @Transactional
    public UserResponse update(Authentication authentication, UpdateUserRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
        if (Objects.nonNull(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (Objects.nonNull(request.getFullName())) {
            user.setFullName(request.getFullName());
        }

        if (Objects.nonNull(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (Objects.nonNull(request.isVerified())) {
            user.setVerified(request.isVerified());
        }

        if (Objects.nonNull(request.isActive())) {
            user.setActive(request.isActive());
        }

        userRepository.save(user);        

        return ResponseMapper.ToUserResponseMapper(user);

    }
}
