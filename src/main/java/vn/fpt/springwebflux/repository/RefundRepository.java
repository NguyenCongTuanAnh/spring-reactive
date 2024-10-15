package vn.fpt.springwebflux.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import vn.fpt.springwebflux.model.mysql.Refund;

import java.util.List;

@Repository
public interface RefundRepository extends R2dbcRepository<Refund, Integer> {
    @Query("SELECT * FROM refund WHERE status = 1 AND tran_id IN (:transIdList)")
    public Flux<Refund> findAllByTranId(List<String> transIdList);
}
