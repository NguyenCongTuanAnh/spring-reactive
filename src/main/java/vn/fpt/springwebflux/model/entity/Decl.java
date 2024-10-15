package vn.fpt.springwebflux.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Decl {
    private String id;
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ssZ")
    private LocalDateTime createdAt;
    private String code;
    private String value;
}
