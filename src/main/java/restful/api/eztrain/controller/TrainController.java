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

import restful.api.eztrain.model.PagingResponse;
import restful.api.eztrain.model.RegisterTrainRequest;
import restful.api.eztrain.model.SearchTrainRequest;
import restful.api.eztrain.model.TrainResponse;
import restful.api.eztrain.model.UpdateTrainRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.TrainService;

@RestController
public class TrainController {

    @Autowired
    TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/trains",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TrainResponse> register(Authentication authentication, @RequestBody RegisterTrainRequest request) {
        TrainResponse response = trainService.register(authentication, request);

        return WebResponse.<TrainResponse>builder()
                                        .status(true)
                                        .messages("train registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/trains/{trainId}",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public WebResponse<TrainResponse> get(Authentication authentication, 
                                        @PathVariable("trainId") String trainId) { 

        TrainResponse response = trainService.get(trainId);

        return WebResponse.<TrainResponse>builder()
                                            .status(true)
                                            .messages("Train fetching success")
                                            .data(response)
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/trains/{trainId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TrainResponse> update(Authentication authentication, 
                                            @RequestBody UpdateTrainRequest request,
                                            @PathVariable("trainId") String trainId) {

        request.setId(trainId);
        
        TrainResponse response = trainService.update(authentication, request, trainId);

        return WebResponse.<TrainResponse>builder()
                                        .status(true)
                                        .messages("Train update success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/trains/{trainId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                            @PathVariable("trainId") String trainId) {

        trainService.delete(trainId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("train delete success")                                        
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/trains",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<TrainResponse>> getAllTrain(
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<TrainResponse> response = trainService.getAllTrains(page, size);

        return WebResponse.<List<TrainResponse>>builder()
                                            .status(true)
                                            .messages("All trains successfully fetched")
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
        path = "/api/trains/search",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<TrainResponse>> search(Authentication authentication,
                                                @RequestParam(value = "name", required = false) String name,
                                                @RequestParam(value = "trainType", required = false) String trainType,
                                                @RequestParam(value = "operator", required = false) String operator,
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        SearchTrainRequest request = SearchTrainRequest.builder()
                                        .page(page)
                                        .size(size)
                                        .name(name)                                        
                                        .trainType(trainType)
                                        .operator(operator)
                                        .build();

        Page<TrainResponse> response = trainService.search(request);

        return WebResponse.<List<TrainResponse>>builder()
                                            .status(true)
                                            .messages("All trains successfully fetched")
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
        path = "/api/trains/{trainId}/coaches/{coachId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TrainResponse> assignCoach(Authentication authentication, 
                                            @PathVariable("trainId") String trainId,
                                            @PathVariable("coachId") String coachId) {        
        
        TrainResponse response = trainService.assignCoach(trainId, coachId);

        return WebResponse.<TrainResponse>builder()
                                        .status(true)
                                        .messages("Train assigning coach success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/trains/{trainId}/coaches/{coachId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TrainResponse> removeCoach(Authentication authentication, 
                                            @PathVariable("trainId") String trainId,
                                            @PathVariable("coachId") String coachId) {        
        
        TrainResponse response = trainService.removeCoach(trainId, coachId);

        return WebResponse.<TrainResponse>builder()
                                        .status(true)
                                        .messages("Train removing coach success")
                                        .data(response)
                                        .build();      
    }
}
