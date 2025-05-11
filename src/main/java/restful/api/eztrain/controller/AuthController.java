package restful.api.eztrain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import restful.api.eztrain.model.ForgotPasswordRequest;
import restful.api.eztrain.model.ForgotPasswordResponse;
import restful.api.eztrain.model.LoginUserRequest;
import restful.api.eztrain.model.ResetPasswordRequest;
import restful.api.eztrain.model.TokenResponse;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.AuthService;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(
        path = "/api/auth/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TokenResponse> login(@RequestBody LoginUserRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        
        return WebResponse.<TokenResponse>builder()
                                        .status(true)
                                        .messages("Login success")
                                        .data(tokenResponse)
                                        .build();
    }

    @PostMapping(
        path = "/api/auth/forgot-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = authService.forgotPassword(request);
        
        return WebResponse.<ForgotPasswordResponse>builder()
                                        .status(true)
                                        .messages("Login success")
                                        .data(response)
                                        .build();
    }

    @PostMapping(
        path = "/api/auth/reset-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        
        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Password reset succesfully")                                        
                                        .build();
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/auth/logout",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        authService.logout(authentication, request, response);
        
        return WebResponse.<String>builder()
                                            .status(true)
                                            .messages("User logout successfully")                                            
                                            .build();
    }

}
