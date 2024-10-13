package vn.fpt.springwebflux.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
