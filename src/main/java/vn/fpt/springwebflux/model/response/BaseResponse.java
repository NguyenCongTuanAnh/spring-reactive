package vn.fpt.springwebflux.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {
    private Long errorCode;
    private String errorMessage;
    private Object data;
}
