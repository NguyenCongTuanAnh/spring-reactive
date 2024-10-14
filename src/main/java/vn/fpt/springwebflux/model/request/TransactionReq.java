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
public class TransactionReq {
    //transaction
    private Integer transactionReqid;
    private String transactionReqTranId;
    private String transactionReqDescription;
    private String transactionReqTitle;
    private Integer transactionReqAmount;
    public boolean isValidate(){
        if(DataUtils.isNullOrEmpty(transactionReqTranId)){
            Mono.error(new BusinessException(ERROR_CODE_01, "Truong transactionReqTranId khong hop le!",null)).subscribe();
        }
        if(DataUtils.isNullOrEmpty(transactionReqDescription)){
            Mono.error(new BusinessException(ERROR_CODE_01, "Truong transactionReqDescription khong hop le!",null)).subscribe();
        }
        if(DataUtils.isNullOrEmpty(transactionReqTitle)){
            Mono.error(new BusinessException(ERROR_CODE_01, "Truong transactionReqTitle khong hop le!",null)).subscribe();
        }
        if(DataUtils.isNullOrEmpty(transactionReqAmount)){
            Mono.error(new BusinessException(ERROR_CODE_01, "Truong transactionReqAmount khong hop le!",null)).subscribe();
        }
        return true;
    }
}
