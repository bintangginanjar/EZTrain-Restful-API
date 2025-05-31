package restful.api.eztrain.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.ForgotPasswordRequest;
import restful.api.eztrain.model.ForgotPasswordResponse;
import restful.api.eztrain.model.LoginUserRequest;
import restful.api.eztrain.model.ResetPasswordRequest;
import restful.api.eztrain.model.TokenResponse;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.CustomUserDetailService;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailService userDetailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityConstants securityConstants;

    @Autowired
    ValidationService validationService;

    @Transactional
    public TokenResponse login(LoginUserRequest request) {
        try {
            Authentication authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                                    request.getEmail(), request.getPassword())
                                );
                                            
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userDetailService.loadUserByUsername(request.getEmail());

            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            
            UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            String token = jwtUtil.generateToken(authentication);

            user.setToken(token);
            user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
            //user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
            
            userRepository.save(user);
            
            return ResponseMapper.ToTokenResponseMapper(user, token, roles);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password");
        }        
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(request.getEmail())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String token = UUID.randomUUID().toString();

        user.setToken(token);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        //user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User request for password reset failed");
        }
        
        return ResponseMapper.ToForgotPasswordResponseMapper(request.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validationService.validate(request);
        
        UserEntity user = userRepository.findFirstByEmailAndToken(request.getEmail(), request.getToken())
                                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setToken(null);
        user.setTokenExpiredAt(null);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User password reset failed");
        }        

    }

    @Transactional
    public void logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        
        if (authentication != null && authentication.isAuthenticated()) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username not found");
        }

    }

}
