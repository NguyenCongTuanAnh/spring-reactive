package vn.fpt.springwebflux.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.model.request.RefundReq;
import vn.fpt.springwebflux.model.request.TransactionReq;
import vn.fpt.springwebflux.model.response.BaseResponse;
import vn.fpt.springwebflux.service.ApiService;
import vn.fpt.springwebflux.service.RefundService;

@RestController
@RequiredArgsConstructor
public class DeclCustomerController {
    private final ApiService apiService;
    private final RefundService refundService;

    @GetMapping("/get-decl-cus")
    public Mono<BaseResponse> getDeclCus() {
        return apiService.getDecl("1").map(decl ->
                new BaseResponse(1, "Thành công", decl)
        );
    }

    @GetMapping("/findAll")
    public Mono<BaseResponse> findAll() {
        return refundService.findAll();
    }

    @GetMapping("/getListDeclCustomerV1")
    public Mono<BaseResponse> getListDeclCustomerV1(TransactionReq transactionReq, RefundReq refundReq, Boolean isDelete) {
        return refundService.getListDeclCustomerV1(transactionReq, refundReq, isDelete);
    }
}
