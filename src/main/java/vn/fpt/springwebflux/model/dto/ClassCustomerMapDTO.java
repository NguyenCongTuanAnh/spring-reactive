package vn.fpt.springwebflux.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassCustomerMapDTO {
    private String name;
    private String email;
    private Integer age;
    private String sort;
    private Integer type;
    private String note;
    private String tranId;
}
