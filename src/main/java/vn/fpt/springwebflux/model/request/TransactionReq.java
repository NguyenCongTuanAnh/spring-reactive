package vn.fpt.springwebflux.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
