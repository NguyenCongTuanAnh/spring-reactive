package vn.fpt.springwebflux.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "refund")
public class Refund {
    @Id
    private Integer id;
    private String tranId;
    private Integer type;
    private String Note;
    @Column(value = "created_at")
    private LocalDateTime createdAt;
    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
    private Integer status;
}
