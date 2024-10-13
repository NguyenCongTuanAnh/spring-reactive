package vn.fpt.springwebflux.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import vn.fpt.springwebflux.model.mysql.Refund;
import vn.fpt.springwebflux.model.mysql.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends R2dbcRepository<Transaction, Integer> {
    @Query("SELECT * FROM transaction WHERE status = 1 AND tran_id IN (:transIdList)")
    public Flux<Transaction> findAllByTranId(List<String> transIdList);
}
