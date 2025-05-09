package restful.api.eztrain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import restful.api.eztrain.model.RegisterUserRequest;
import restful.api.eztrain.model.UpdateUserRequest;
import restful.api.eztrain.model.UserResponse;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.UserService;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
        path = "/api/users",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> create(@RequestBody RegisterUserRequest request) {
        UserResponse response = userService.register(request);

        return WebResponse.<UserResponse>builder()
                                        .status(true)
                                        .messages("User registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/users",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public WebResponse<UserResponse> get(Authentication authentication) {        
        UserResponse response = userService.get(authentication);

        return WebResponse.<UserResponse>builder()
                                            .status(true)
                                            .messages("User fetching success")
                                            .data(response)
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/users",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> update(Authentication authentication, @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.update(authentication, request);

        return WebResponse.<UserResponse>builder()
                                            .status(true)
                                            .messages("User successfully updated")
                                            .data(response)
                                            .build();
    }

}
