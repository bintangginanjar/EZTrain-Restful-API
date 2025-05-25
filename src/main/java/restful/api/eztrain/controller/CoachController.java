package restful.api.eztrain.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.PagingResponse;
import restful.api.eztrain.model.RegisterCoachRequest;
import restful.api.eztrain.model.SearchCoachRequest;
import restful.api.eztrain.model.UpdateCoachRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.CoachService;

@RestController
public class CoachController {

    @Autowired
    CoachService coachService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/coaches",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CoachResponse> register(Authentication authentication, @RequestBody RegisterCoachRequest request) {
        CoachResponse response = coachService.register(authentication, request);

        return WebResponse.<CoachResponse>builder()
                                        .status(true)
                                        .messages("Coach registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/coaches/{coachId}",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public WebResponse<CoachResponse> get(Authentication authentication, 
                                        @PathVariable("coachId") Long coachId) { 

        CoachResponse response = coachService.get(coachId);

        return WebResponse.<CoachResponse>builder()
                                            .status(true)
                                            .messages("Coach fetching success")
                                            .data(response)
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/coaches/{coachId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CoachResponse> update(Authentication authentication, 
                                            @RequestBody UpdateCoachRequest request,
                                            @PathVariable("coachId") Long coachId) {

        request.setId(coachId);
        
        CoachResponse response = coachService.update(authentication, request, coachId);

        return WebResponse.<CoachResponse>builder()
                                        .status(true)
                                        .messages("Coach update success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/coaches/{coachId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                            @PathVariable("coachId") Long coachId) {

        coachService.delete(coachId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Coach delete success")                                        
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/coaches",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CoachResponse>> getAlltrain(
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<CoachResponse> response = coachService.getAllCoaches(page, size);

        return WebResponse.<List<CoachResponse>>builder()
                                            .status(true)
                                            .messages("All coaches successfully fetched")
                                            .data(response.getContent())
                                            .paging(PagingResponse.builder()
                                                    .currentPage(response.getNumber())
                                                    .totalPage(response.getTotalPages())
                                                    .size(response.getSize())
                                                    .build())
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/coaches/search",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CoachResponse>> search(Authentication authentication,
                                                @RequestParam(value = "coachName", required = false) String coachName,                                                
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        SearchCoachRequest request = SearchCoachRequest.builder()
                                        .page(page)
                                        .size(size)                                                                            
                                        .coachName(coachName)                                    
                                        .build();

        Page<CoachResponse> response = coachService.search(request);

        return WebResponse.<List<CoachResponse>>builder()
                                            .status(true)
                                            .messages("All coaches successfully fetched")
                                            .data(response.getContent())
                                            .paging(PagingResponse.builder()
                                                    .currentPage(response.getNumber())
                                                    .totalPage(response.getTotalPages())
                                                    .size(response.getSize())
                                                    .build())
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/coaches/{coachId}/seats/{seatId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CoachResponse> assignSeat(Authentication authentication,                                             
                                            @PathVariable("coachId") Long coachId,
                                            @PathVariable("seatId") Long seatId) {        
        
        CoachResponse response = coachService.assignSeat(coachId, seatId);

        return WebResponse.<CoachResponse>builder()
                                        .status(true)
                                        .messages("Coach assigning seat success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/coaches/{coachId}/seats/{seatId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CoachResponse> removeSeat(Authentication authentication, 
                                            @PathVariable("coachId") Long coachId,
                                            @PathVariable("seatId") Long seatId) {        
        
        CoachResponse response = coachService.removeSeat(coachId, seatId);

        return WebResponse.<CoachResponse>builder()
                                        .status(true)
                                        .messages("Coach removing seat success")
                                        .data(response)
                                        .build();      
    }

}
