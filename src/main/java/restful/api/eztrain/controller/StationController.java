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
import restful.api.eztrain.model.RegisterStationRequest;
import restful.api.eztrain.model.SearchStationRequest;
import restful.api.eztrain.model.StationResponse;
import restful.api.eztrain.model.UpdateStationRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.StationService;

@RestController
public class StationController {

    @Autowired
    StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/stations",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<StationResponse> register(Authentication authentication, @RequestBody RegisterStationRequest request) {
        StationResponse response = stationService.register(authentication, request);

        return WebResponse.<StationResponse>builder()
                                        .status(true)
                                        .messages("Station registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/stations/{stationId}",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public WebResponse<StationResponse> get(Authentication authentication, 
                                        @PathVariable("stationId") String stationId) { 

        StationResponse response = stationService.get(stationId);

        return WebResponse.<StationResponse>builder()
                                            .status(true)
                                            .messages("Station fetching success")
                                            .data(response)
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/stations/{stationId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<StationResponse> update(Authentication authentication, 
                                            @RequestBody UpdateStationRequest request,
                                            @PathVariable("stationId") String stationId) {

        request.setId(stationId);
        
        StationResponse response = stationService.update(authentication, request, stationId);

        return WebResponse.<StationResponse>builder()
                                        .status(true)
                                        .messages("Station update success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/stations/{stationId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                            @PathVariable("stationId") String stationId) {

        stationService.delete(stationId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Station delete success")                                        
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/stations",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<StationResponse>> getAllStation(
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<StationResponse> response = stationService.getAllStations(page, size);

        return WebResponse.<List<StationResponse>>builder()
                                            .status(true)
                                            .messages("All stations successfully fetched")
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
        path = "/api/stations/search",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<StationResponse>> search(Authentication authentication,
                                                        @RequestParam(value = "code", required = false) String code, 
                                                        @RequestParam(value = "name", required = false) String name,
                                                        @RequestParam(value = "city", required = false) String city,
                                                        @RequestParam(value = "province", required = false) String province,
                                                        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                        @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        SearchStationRequest request = SearchStationRequest.builder()
                                        .page(page)
                                        .size(size)
                                        .code(code)
                                        .name(name)
                                        .city(city)
                                        .province(province)
                                        .build();

        Page<StationResponse> response = stationService.search(request);
        
        return WebResponse.<List<StationResponse>>builder()
                            .status(true)
                            .messages("Station search executed successfully")
                            .errors(null)
                            .data(response.getContent())
                            .paging(PagingResponse.builder()
                                .currentPage(response.getNumber())
                                .totalPage(response.getTotalPages())
                                .size(response.getSize())
                                .build())
                            .build();
    }
}
