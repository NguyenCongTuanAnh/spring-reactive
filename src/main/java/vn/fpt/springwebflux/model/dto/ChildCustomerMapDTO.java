package vn.fpt.springwebflux.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildCustomerMapDTO {
    private String name;
    private String email;
    private Integer age;
    private String sort;
    private Integer amount;
    private String title;
    private String description;
    private String tranId;
}
