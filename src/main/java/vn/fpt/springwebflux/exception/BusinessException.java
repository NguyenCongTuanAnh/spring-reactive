package vn.fpt.springwebflux.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends RuntimeException{
    private Long errorCode;
    private String message;
    private Object data;
}
