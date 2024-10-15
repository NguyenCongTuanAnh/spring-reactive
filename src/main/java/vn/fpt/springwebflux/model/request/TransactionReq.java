package vn.fpt.springwebflux.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.fpt.springwebflux.exception.BusinessException;
import vn.fpt.springwebflux.utils.DataUtils;

import static vn.fpt.springwebflux.constant.CommonConstant.*;
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

    public void isValidate() {
        if (DataUtils.isNullOrEmpty(transactionReqTranId)) {
            if (DataUtils.isNullOrEmpty(transactionReqTranId)) {
                throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_TRANID, null);
            }
            if (DataUtils.isNullOrEmpty(transactionReqDescription)) {
                throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_TRANID, null);
            }
            if (DataUtils.isNullOrEmpty(transactionReqTitle)) {
                throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_TITLE, null);
            }
            if (DataUtils.isNullOrEmpty(transactionReqAmount)) {
                throw new BusinessException(ERROR_CODE_01, TRANSACTION_REQ_AMOUNT, null);
            }
        }
    }

    public boolean isNull() {
        if (DataUtils.isNullOrEmpty(transactionReqTranId)) {
            return true;
        }
        if (DataUtils.isNullOrEmpty(transactionReqDescription)) {
            return true;
        }
        if (DataUtils.isNullOrEmpty(transactionReqTitle)) {
            return true;
        }
        if (DataUtils.isNullOrEmpty(transactionReqAmount)) {
            return true;
        }
        return false;
    }
}
