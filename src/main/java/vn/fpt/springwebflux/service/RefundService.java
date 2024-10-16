package vn.fpt.springwebflux.service;

import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.model.request.RefundReq;
import vn.fpt.springwebflux.model.request.TransactionReq;
import vn.fpt.springwebflux.model.response.BaseResponse;


public interface RefundService {
    Mono<BaseResponse> findAll();

    Mono<BaseResponse> getListDeclCustomerV1(TransactionReq transactionReq, RefundReq refundReq, Boolean isDelete);
}
