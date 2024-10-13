package vn.fpt.springwebflux.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclInputDTO {
    private String name;
    private String sortOrder;
    private Map<String, ChildCustomerMapDTO> childCustomer;
    private Map<String, ClassCustomerMapDTO> classCustomer;
}
