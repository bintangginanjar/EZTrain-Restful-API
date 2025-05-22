package restful.api.eztrain.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchSeatRequest {

    private String seatNumber;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;
}
