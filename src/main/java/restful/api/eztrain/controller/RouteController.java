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
import restful.api.eztrain.model.RegisterRouteRequest;
import restful.api.eztrain.model.RouteResponse;
import restful.api.eztrain.model.SearchRouteRequest;
import restful.api.eztrain.model.UpdateRouteRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.RouteService;

@RestController
public class RouteController {

    @Autowired
    RouteService routeService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/routes",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RouteResponse> register(Authentication authentication, @RequestBody RegisterRouteRequest request) {
        RouteResponse response = routeService.register(authentication, request);

        return WebResponse.<RouteResponse>builder()
                                        .status(true)
                                        .messages("Route registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/routes/origin/{originId}/destination/{destId}",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public WebResponse<RouteResponse> get(Authentication authentication, 
                                        @PathVariable("originId") String originId,
                                        @PathVariable("destId") String destId) { 

        RouteResponse response = routeService.get(originId, destId);

        return WebResponse.<RouteResponse>builder()
                                            .status(true)
                                            .messages("Route fetching success")
                                            .data(response)
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/routes",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<RouteResponse>> getAllRoute(
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<RouteResponse> response = routeService.getAllRoutes(page, size);

        return WebResponse.<List<RouteResponse>>builder()
                                            .status(true)
                                            .messages("All routes successfully fetched")
                                            .data(response.getContent())
                                            .paging(PagingResponse.builder()
                                                    .currentPage(response.getNumber())
                                                    .totalPage(response.getTotalPages())
                                                    .size(response.getSize())
                                                    .build())
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/routes/{routeId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RouteResponse> update(Authentication authentication, 
                                            @RequestBody UpdateRouteRequest request,
                                            @PathVariable("routeId") String routeId) {

        request.setId(routeId);
        
        RouteResponse response = routeService.update(authentication, request, routeId);

        return WebResponse.<RouteResponse>builder()
                                        .status(true)
                                        .messages("Route update success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/routes/{routeId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                            @PathVariable("routeId") String routeId) {

        routeService.delete(routeId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Route delete success")                                        
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/routes/search",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<RouteResponse>> search(Authentication authentication,
                                                @RequestParam(value = "originCode", required = false) String originCode,
                                                @RequestParam(value = "destCode", required = false) String destCode,                                                
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        SearchRouteRequest request = SearchRouteRequest.builder()
                                        .page(page)
                                        .size(size)
                                        .origin(originCode)
                                        .destination(destCode)                                        
                                        .build();

        Page<RouteResponse> response = routeService.search(request);

        return WebResponse.<List<RouteResponse>>builder()
                                            .status(true)
                                            .messages("All routes successfully fetched")
                                            .data(response.getContent())
                                            .paging(PagingResponse.builder()
                                                    .currentPage(response.getNumber())
                                                    .totalPage(response.getTotalPages())
                                                    .size(response.getSize())
                                                    .build())
                                            .build();
    }
}
