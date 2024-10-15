package vn.fpt.springwebflux.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.exception.BusinessException;
import vn.fpt.springwebflux.model.response.BaseResponse;
import vn.fpt.springwebflux.utils.DataUtils;

import static vn.fpt.springwebflux.constant.CommonConstant.*;
import static vn.fpt.springwebflux.constant.ErrorCodeConstant.ERROR_CODE_01;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundReq {
    //refund
    private Integer refundReqId;
    private Integer refundReqType;
    private String refundReqNote;
    private String refundReqTranId;

    public void isValidate() {
        if (DataUtils.isNullOrEmpty(refundReqTranId)) {
            throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_TRANID, null);
        }
        if (DataUtils.isNullOrEmpty(refundReqType)) {
            throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_TYPE, null);

        }
        if (DataUtils.isNullOrEmpty(refundReqNote)) {
            throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_NOTE, null);
        }
    }

    public boolean isNull() {
        if (DataUtils.isNullOrEmpty(refundReqTranId)) {
//            throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_TRANID, null);
//            Mono.just(new BaseResponse(ERROR_CODE_01, TRANSACTION_REQ_TRANID, null)).subscribe();
            return true;
        }
        if (DataUtils.isNullOrEmpty(refundReqType)) {
//            throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_TYPE, null);
//            Mono.just(new BaseResponse(ERROR_CODE_01, TRANSACTION_REQ_TYPE, null)).subscribe();
            return true;
        }
        if (DataUtils.isNullOrEmpty(refundReqNote)) {
//            throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_NOTE, null);
            return true;
        }
        return false;
    }
}
