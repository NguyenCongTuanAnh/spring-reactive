package vn.fpt.springwebflux.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclOutputDTO {
    private String name;
    private String sortOrder;
    private List<ChildCustomerMapDTO> childCustomerList;
    private List<ClassCustomerMapDTO> classCustomerList;
}
