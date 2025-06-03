package restful.api.eztrain.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import restful.api.eztrain.model.PagingResponse;
import restful.api.eztrain.model.RegisterRoutePriceRequest;
import restful.api.eztrain.model.RoutePriceResponse;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.RoutePriceService;

@RestController
public class RoutePriceController {

    @Autowired
    RoutePriceService routePriceService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/routeprices",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RoutePriceResponse> register(Authentication authentication, @RequestBody RegisterRoutePriceRequest request) {
        RoutePriceResponse response = routePriceService.register(authentication, request);

        return WebResponse.<RoutePriceResponse>builder()
                                        .status(true)
                                        .messages("Route price registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/routeprices/{routePriceId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public WebResponse<RoutePriceResponse> get(Authentication authentication, 
                                        @PathVariable("routePriceId") Long routePriceId) { 

        RoutePriceResponse response = routePriceService.get(routePriceId);

        return WebResponse.<RoutePriceResponse>builder()
                                            .status(true)
                                            .messages("Route price fetching success")
                                            .data(response)
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/routeprices",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<RoutePriceResponse>> getAllRoute(
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<RoutePriceResponse> response = routePriceService.getAllRoutePrices(page, size);

        return WebResponse.<List<RoutePriceResponse>>builder()
                                            .status(true)
                                            .messages("All route prices successfully fetched")
                                            .data(response.getContent())
                                            .paging(PagingResponse.builder()
                                                    .currentPage(response.getNumber())
                                                    .totalPage(response.getTotalPages())
                                                    .size(response.getSize())
                                                    .build())
                                            .build();
    }

}
