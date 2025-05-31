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
import restful.api.eztrain.model.RegisterSeatRequest;
import restful.api.eztrain.model.SearchSeatRequest;
import restful.api.eztrain.model.SeatResponse;
import restful.api.eztrain.model.UpdateSeatRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.service.SeatService;

@RestController
public class SeatController {

    @Autowired
    SeatService seatService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/seats",
        consumes = MediaType.APPLICATION_JSON_VALUE,        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<SeatResponse> register(Authentication authentication, @RequestBody RegisterSeatRequest request) {
        SeatResponse response = seatService.register(authentication, request);

        return WebResponse.<SeatResponse>builder()
                                        .status(true)
                                        .messages("Seat registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/seats/{seatId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public WebResponse<SeatResponse> get(Authentication authentication, 
                                        @PathVariable("seatId") Long seatId) { 

        SeatResponse response = seatService.get(seatId);

        return WebResponse.<SeatResponse>builder()
                                            .status(true)
                                            .messages("Seat fetching success")
                                            .data(response)
                                            .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/seats/{seatId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<SeatResponse> update(Authentication authentication, 
                                            @RequestBody UpdateSeatRequest request,
                                            @PathVariable("seatId") Long seatId) {

        request.setId(seatId);
        
        SeatResponse response = seatService.update(authentication, request, seatId);

        return WebResponse.<SeatResponse>builder()
                                        .status(true)
                                        .messages("Seat update success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/seats/{seatId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                            @PathVariable("seatId") Long seatId) {

        seatService.delete(seatId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Seat delete success")                                        
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/seats",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<SeatResponse>> getAlltrain(
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<SeatResponse> response = seatService.getAllSeats(page, size);

        return WebResponse.<List<SeatResponse>>builder()
                                            .status(true)
                                            .messages("All seats successfully fetched")
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
        path = "/api/seats/search",        
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<SeatResponse>> search(Authentication authentication,
                                                @RequestParam(value = "seatNumber", required = false) String seatNumber,                                                
                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {

        SearchSeatRequest request = SearchSeatRequest.builder()
                                        .page(page)
                                        .size(size)                                                                            
                                        .seatNumber(seatNumber)                                    
                                        .build();

        Page<SeatResponse> response = seatService.search(request);

        return WebResponse.<List<SeatResponse>>builder()
                                            .status(true)
                                            .messages("All seats successfully fetched")
                                            .data(response.getContent())
                                            .paging(PagingResponse.builder()
                                                    .currentPage(response.getNumber())
                                                    .totalPage(response.getTotalPages())
                                                    .size(response.getSize())
                                                    .build())
                                            .build();
    }

}
