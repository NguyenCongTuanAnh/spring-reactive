package vn.fpt.springwebflux.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.exception.BusinessException;
import vn.fpt.springwebflux.utils.DataUtils;

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
    public boolean isValidate(){
        if(DataUtils.isNullOrEmpty(refundReqTranId)){
            Mono.error(new BusinessException(ERROR_CODE_01, "Truong refundReqTranId khong hop le!",null)).subscribe();
        }
        if(DataUtils.isNullOrEmpty(refundReqType)){
            Mono.error(new BusinessException(ERROR_CODE_01, "Truong refundReqType khong hop le!",null)).subscribe();
        }
        if(DataUtils.isNullOrEmpty(refundReqNote)){
            Mono.error(new BusinessException(ERROR_CODE_01, "Truong refundReqNote khong hop le!",null)).subscribe();
        }
        return true;
    }
}
