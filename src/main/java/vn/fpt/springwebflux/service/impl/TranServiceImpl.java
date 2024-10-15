package vn.fpt.springwebflux.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.model.response.BaseResponse;
import vn.fpt.springwebflux.repository.TransactionRepository;
import vn.fpt.springwebflux.service.TransService;

import static vn.fpt.springwebflux.constant.ErrorCodeConstant.ERROR_CODE_01;

@Service
@RequiredArgsConstructor
public class TranServiceImpl implements TransService {
    private final TransactionRepository transactionRepository;

    @Override
    public Mono<BaseResponse> findAll() {
        return transactionRepository.findAll()
                .collectList()
                .map(refunds -> new BaseResponse(ERROR_CODE_01, "", refunds))
                .defaultIfEmpty(new BaseResponse(ERROR_CODE_01, "No refunds found", null));
    }

}
