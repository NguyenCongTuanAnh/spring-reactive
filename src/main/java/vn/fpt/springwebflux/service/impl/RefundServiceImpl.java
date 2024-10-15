package vn.fpt.springwebflux.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.exception.BusinessException;
import vn.fpt.springwebflux.model.dto.ChildCustomerMapDTO;
import vn.fpt.springwebflux.model.dto.ClassCustomerMapDTO;
import vn.fpt.springwebflux.model.dto.DeclInputDTO;
import vn.fpt.springwebflux.model.dto.DeclOutputDTO;
import vn.fpt.springwebflux.model.mysql.Refund;
import vn.fpt.springwebflux.model.mysql.Transaction;
import vn.fpt.springwebflux.model.request.RefundReq;
import vn.fpt.springwebflux.model.request.TransactionReq;
import vn.fpt.springwebflux.model.response.BaseResponse;
import vn.fpt.springwebflux.repository.RefundRepository;
import vn.fpt.springwebflux.repository.TransactionRepository;
import vn.fpt.springwebflux.service.ApiService;
import vn.fpt.springwebflux.service.RefundService;
import vn.fpt.springwebflux.utils.DataUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static vn.fpt.springwebflux.constant.CommonConstant.*;
import static vn.fpt.springwebflux.constant.ErrorCodeConstant.*;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {
    private final RefundRepository refundRepository;
    private final TransactionRepository transactionRepository;
    private final ApiService apiService;

    @Override
    public Mono<BaseResponse> findAll() {
        return refundRepository.findAll()
                .collectList()
                .map(refunds -> new BaseResponse(ERROR_CODE_01, "", refunds))
                .defaultIfEmpty(new BaseResponse(ERROR_CODE_01, "No refunds found", null));
    }

    @Override
    public Mono<BaseResponse> getListDeclCustomerV1(TransactionReq transactionReq, RefundReq refundReq, Boolean isDelete) {
        return apiService.getDecl(DECL_CODE)
                .flatMap(decl -> {
                    if (DataUtils.isNullOrEmpty(decl)) {
                        return Mono.error(new BusinessException(ERROR_CODE_500, NOT_FOUND_DECL, null));
                    }
                    //            chuyển đổi kiểu dự liệu map -> list
                    return DataUtils.parseStringToObject(decl.getValue(), DeclInputDTO.class);
                })
                .flatMap(parseDecl -> {
                    DeclOutputDTO eachDecl = DeclOutputDTO.builder()
                            .name(parseDecl.getName())
                            .sortOrder(parseDecl.getSortOrder())
                            .build();
//                    List<ChildCustomerMapDTO> classCustomerMapDTOList = new ArrayList<>(parseDecl.getChildCustomer().values());
//                    List<ClassCustomerMapDTO> childCustomerMapDTOList = new ArrayList<>(parseDecl.getClassCustomer().values());
                    return Mono.zip(transactionRepository.findAllByTranId((eachDecl.getChildCustomerList().stream().map(ChildCustomerMapDTO::getTranId).collect(Collectors.toList()))).collectList(),
                                    refundRepository.findAllByTranId((eachDecl.getClassCustomerList().stream().map(ClassCustomerMapDTO::getTranId).collect(Collectors.toList()))).collectList())
                            .flatMap(tuple -> {
                                //
                                if (!transactionReq.isNull()) {
                                    transactionReq.isValidate();
                                    return handleSaveTran(transactionReq, tuple.getT1());
                                }
                                if (!refundReq.isNull()) {
                                    refundReq.isValidate();
                                    return handleSaveRefund(refundReq, tuple.getT2());
                                }
                                if(isDelete){
                                    handleDelele(transactionReq, refundReq, tuple.getT1(), tuple.getT2());
                                }
                                eachDecl.setChildCustomerList(handleChildCus(eachDecl.getChildCustomerList(), tuple.getT1()));
                                eachDecl.setClassCustomerList(handleClassCus(eachDecl.getClassCustomerList(), tuple.getT2()));

                                //xắp xếp danh sách childCustomer tang dan
                                eachDecl.getChildCustomerList().sort(Comparator.comparing(ChildCustomerMapDTO::getSort));
                                //xắp xếp danh sách clasCustomer giam dan
                                eachDecl.getClassCustomerList().sort(Comparator.comparing(ClassCustomerMapDTO::getSort).reversed());
                                return Mono.just(new BaseResponse(ERROR_CODE_00, DEFAULT_SUCCESSFULLY, eachDecl));
                            });
                });
    }
    public Mono<BaseResponse> handleDelele (TransactionReq transactionReq, RefundReq refundReq, List<Transaction> transactionList, List<Refund> refundList){

            if(DataUtils.isNullOrEmpty(transactionReq.getTransactionReqid()) && DataUtils.isNullOrEmpty(refundReq.getRefundReqId())){
                if (DataUtils.isNullOrEmpty(transactionReq.getTransactionReqid())){
                    return Mono.just(new BaseResponse(ERROR_CODE_01, REFUND_REQ_ID_NOT_VALID, null));
                }
                return Mono.just(new BaseResponse(ERROR_CODE_01, TRANSACTION_REQ_ID_NOT_VALID, null));
            }
            if (!DataUtils.isNullOrEmpty(transactionReq.getTransactionReqid())){
               Transaction transaction =  transactionList.stream()
                        .filter(tran -> tran.getId().equals(transactionReq.getTransactionReqid()))
                        .findFirst().orElse(null);
                       if (DataUtils.isNullOrEmpty(transaction)){
                           transaction.setStatus(0);
                           transaction.setUpdatedAt(LocalDateTime.now());
                           return transactionRepository.save(transaction).flatMap(tran -> Mono.just(new BaseResponse(ERROR_CODE_00, DELETE_SUCCESSFULLY, null)));
                       }
                       return transactionRepository.findById(transactionReq.getTransactionReqid()).flatMap(tran -> {
                          if (DataUtils.isNullOrEmpty(tran)){
                              return Mono.just(new BaseResponse(ERROR_CODE_01, TRANSACTION_REQ_ID_NOT_EXSITED, null));
                          }
                           tran.setStatus(0);
                           tran.setUpdatedAt(LocalDateTime.now());
                           return transactionRepository.save(transaction).flatMap(tranDel -> Mono.just(new BaseResponse(ERROR_CODE_00, DELETE_SUCCESSFULLY, null)));
                       });
            }
            if (!DataUtils.isNullOrEmpty(refundReq.getRefundReqId())){
                Refund refund =  refundList.stream()
                        .filter(ref -> ref.getId().equals(refundReq.getRefundReqId()))
                        .findFirst().orElse(null);
                if (DataUtils.isNullOrEmpty(refund)){
                    refund.setStatus(0);
                    refund.setUpdatedAt(LocalDateTime.now());
                    return refundRepository.save(refund).flatMap(tran -> Mono.just(new BaseResponse(ERROR_CODE_00, DELETE_SUCCESSFULLY, null)));
                }
                return refundRepository.findById(refundReq.getRefundReqId()).flatMap(ref -> {
                    if (DataUtils.isNullOrEmpty(ref)){
                        return Mono.just(new BaseResponse(ERROR_CODE_01, REFUND_REQ_ID_NOT_EXSITED, null));
                    }
                    ref.setStatus(0);
                    ref.setUpdatedAt(LocalDateTime.now());
                    return refundRepository.save(ref).flatMap(tranDel -> Mono.just(new BaseResponse(ERROR_CODE_00, DELETE_SUCCESSFULLY, null)));
                });
            }
    }
    public List<ChildCustomerMapDTO> handleChildCus(List<ChildCustomerMapDTO> childCustomerMapDTOList, List<Transaction> transactionList) {
        childCustomerMapDTOList.forEach(childCus -> {
            Transaction transactionOptional = transactionList.stream().filter(transaction -> transaction.getTranId().equals(childCus.getTranId())).findFirst().orElse(null);
            //set lai cac truong amount, title, description (GET)
            childCus.setAmount(transactionOptional.getAmount());
            childCus.setTitle(transactionOptional.getTitle());
            childCus.setDescription(transactionOptional.getDescription());
        });
        return childCustomerMapDTOList;
    }
    public List<ClassCustomerMapDTO> handleClassCus(List<ClassCustomerMapDTO> classCustomerMapDTOList, List<Refund> refundList) {
        classCustomerMapDTOList.forEach(classCus -> {
            Refund refund = refundList.stream().filter(transaction -> transaction.getTranId().equals(classCus.getTranId())).findFirst().orElse(null);
            //set lai cac truong amount, title, description (GET)
            classCus.setType(refund.getType());
            classCus.setNote(refund.getNote());
        });
        return classCustomerMapDTOList;
    }

    public void handleDelRefund(Refund refund) {
        refund.setStatus(0);
        refund.setUpdatedAt(LocalDateTime.now());
        refundRepository.save(refund).subscribe(a -> {
        });
    }

    public Mono<BaseResponse> handleSaveRefund(RefundReq refundReq, List<Refund> refundList) {
        if (DataUtils.isNullOrEmpty(refundReq.getRefundReqId())) {
            //neu id == null -> insert ban ghi moi
            Refund refund = new Refund(null, refundReq.getRefundReqTranId(), refundReq.getRefundReqType(), refundReq.getRefundReqNote(), LocalDateTime.now(), LocalDateTime.now(), ACTIVE_STATUS);
            return refundRepository.save(refund)
                    .flatMap(ref -> Mono.just(new BaseResponse(ERROR_CODE_00, SAVE_SUCCESSFULLY, null)));
        }
        //check finby truoc do tim thay ban ghi do chua neu roi thi update neu chua co thi findby lai vao dn
        Refund optionalRefund = refundList.stream().filter(ref -> ref.getId().equals(refundReq.getRefundReqId())).findFirst().orElse(null);
        //check trong list da find truoc do
        //neu co thif ko phai find laij
        if (DataUtils.isNullOrEmpty(optionalRefund)) {
            optionalRefund.setTranId(refundReq.getRefundReqTranId());
            optionalRefund.setType(refundReq.getRefundReqType());
            optionalRefund.setNote(refundReq.getRefundReqNote());
            optionalRefund.setUpdatedAt(LocalDateTime.now());
            return refundRepository.save(optionalRefund)
                    .flatMap(ref -> Mono.just(new BaseResponse(ERROR_CODE_00, UPDATE_SUCCESSFULLY, null)));
        }
        return refundRepository.findById(refundReq.getRefundReqId()).flatMap(checkRefund -> {
            if (!DataUtils.isNullOrEmpty(checkRefund)) {
                //update lai refund vowis id tu input
                checkRefund.setTranId(refundReq.getRefundReqTranId());
                checkRefund.setType(refundReq.getRefundReqType());
                checkRefund.setNote(refundReq.getRefundReqNote());
                checkRefund.setUpdatedAt(LocalDateTime.now());
                return refundRepository.save(checkRefund)
                        .flatMap(a -> Mono.just(new BaseResponse(ERROR_CODE_00, UPDATE_SUCCESSFULLY, null)));
            }
            return Mono.just(new BaseResponse(ERROR_CODE_01, UPDATE_UNSUCCESSFULLY, null));
        });
    }

    public Mono<BaseResponse> handleSaveTran(TransactionReq transactionReq, List<Transaction> transactionList) {
        //validate
        //neu id == null -> insert ban ghi moi
        if (DataUtils.isNullOrEmpty(transactionReq.getTransactionReqid())) {
            Transaction transaction = new Transaction(null, transactionReq.getTransactionReqTranId(), transactionReq.getTransactionReqTitle(), transactionReq.getTransactionReqAmount(), transactionReq.getTransactionReqDescription(), LocalDateTime.now(), LocalDateTime.now(), ACTIVE_STATUS);
            return transactionRepository.save(transaction)
                    .flatMap(tran -> Mono.just(new BaseResponse(ERROR_CODE_00, SAVE_SUCCESSFULLY, null)));
        }
        //neu id != null -> update ban ghi do
        //check finby truoc do tim thay ban ghi do chua neu roi thi update neu chua co thi findby lai vao dn
        Transaction optionalTransaction = transactionList.stream().filter(tran -> tran.getId().equals(transactionReq.getTransactionReqid())).findFirst().orElse(null);
        if (DataUtils.isNullOrEmpty(optionalTransaction)) {
            //set laij cac truong can update
            optionalTransaction.setTranId(transactionReq.getTransactionReqTranId());
            optionalTransaction.setTitle(transactionReq.getTransactionReqTitle());
            optionalTransaction.setAmount(transactionReq.getTransactionReqAmount());
            optionalTransaction.setDescription(transactionReq.getTransactionReqDescription());
            optionalTransaction.setUpdatedAt( LocalDateTime.now());
            return transactionRepository.save(optionalTransaction)
                    .flatMap(tran -> Mono.just(new BaseResponse(ERROR_CODE_00, UPDATE_SUCCESSFULLY, null)));
        }
        // finby theo truon id -> neu co update laij ban ghi theo cac ttruong tu input,
        //neu ko thoong bao update ko thanh cong
        return transactionRepository.findById(transactionReq.getTransactionReqid())
                .flatMap(checkTransaction -> {
                    if (!DataUtils.isNullOrEmpty(checkTransaction)) {
                        checkTransaction.setTranId(transactionReq.getTransactionReqTranId());
                        checkTransaction.setTitle(transactionReq.getTransactionReqTitle());
                        checkTransaction.setAmount(transactionReq.getTransactionReqAmount());
                        checkTransaction.setDescription(transactionReq.getTransactionReqDescription());
                        checkTransaction.setUpdatedAt( LocalDateTime.now());
                        return transactionRepository.save(checkTransaction)
                                .flatMap(tran -> Mono.just(new BaseResponse(ERROR_CODE_00, UPDATE_SUCCESSFULLY, null)));
                    }
                    return Mono.just(new BaseResponse(ERROR_CODE_01, UPDATE_UNSUCCESSFULLY, null));
                });
    }
}
