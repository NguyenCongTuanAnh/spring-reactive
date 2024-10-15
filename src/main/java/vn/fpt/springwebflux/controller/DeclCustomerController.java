package vn.fpt.springwebflux.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.model.request.RefundReq;
import vn.fpt.springwebflux.model.request.TransactionReq;
import vn.fpt.springwebflux.model.response.BaseResponse;
import vn.fpt.springwebflux.service.ApiService;
import vn.fpt.springwebflux.service.RefundService;
import vn.fpt.springwebflux.service.TransService;

import static vn.fpt.springwebflux.constant.ErrorCodeConstant.ERROR_CODE_01;

@RestController
@RequiredArgsConstructor
public class DeclCustomerController {
    private final ApiService apiService;
    private final RefundService refundService;
    private final TransService transService;

    @GetMapping("/get-decl-cus")
    public Mono<BaseResponse> getDeclCus() {
        return apiService.getDecl("1").map(decl ->
                new BaseResponse(ERROR_CODE_01, "Thành công", decl)
        );
    }

    @GetMapping("/trans")
    public Mono<BaseResponse> findTran() {
        return transService.findAll();
    }

    @GetMapping("/refund")
    public Mono<BaseResponse> findAll() {
        return refundService.findAll();
    }

    @PostMapping("/getListDeclCustomerV1")
    public Mono<BaseResponse> getListDeclCustomerV1(TransactionReq transactionReq, RefundReq refundReq, Boolean isDelete) {
        return refundService.getListDeclCustomerV1(transactionReq, refundReq, isDelete);
    }
}
