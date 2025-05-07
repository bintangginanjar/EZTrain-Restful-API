package restful.api.eztrain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebResponse<T> {

    private Boolean status;

    private String messages;

    private String errors;    

    private T data;

    private PagingResponse paging;

}
