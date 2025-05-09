package restful.api.eztrain.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.RegisterUserRequest;
import restful.api.eztrain.model.UpdateUserRequest;
import restful.api.eztrain.model.UserResponse;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
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
        user.setIsVerified(false);
        user.setIsActive(false);

        userRepository.save(user);        

        return ResponseMapper.ToUserResponseMapper(user);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Authentication authentication) {        

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

        if (Objects.nonNull(request.getIsVerified())) {
            user.setIsVerified(request.getIsVerified());
        }

        if (Objects.nonNull(request.getIsActive())) {
            user.setIsActive(request.getIsActive());
        }

        userRepository.save(user);        

        return ResponseMapper.ToUserResponseMapper(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {   

        List<UserEntity> users = userRepository.findAll();

        return ResponseMapper.ToUserResponseListMapper(users);
    }
}
