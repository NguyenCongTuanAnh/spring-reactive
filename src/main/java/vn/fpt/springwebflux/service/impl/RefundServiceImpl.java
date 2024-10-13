package vn.fpt.springwebflux.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static vn.fpt.springwebflux.constant.CommonConstant.*;
import static vn.fpt.springwebflux.constant.ErrorCodeConstant.ERROR_CODE_01;
import static vn.fpt.springwebflux.constant.ErrorCodeConstant.ERROR_CODE_500;

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
        return apiService.getDecl(DECL_CODE).flatMap(decl -> {
            if (DataUtils.isNullOrEmpty(decl)) {
                return Mono.error(new BusinessException(ERROR_CODE_500, NOT_FOUND_DECL, null));
            }
            Mono<DeclInputDTO> parsedDecl = DataUtils.parseStringToObject(decl.getValue(), DeclInputDTO.class);
//            chuyển đổi kiểu dự liệu map -> list
            return parsedDecl.flatMap(parseDecl -> {
                Mono<DeclOutputDTO> declOutputDTO = Mono.just(DeclOutputDTO.builder()
                        .name(parseDecl.getName())
                        .sortOrder(parseDecl.getSortOrder())
                        .childCustomerList(new ArrayList<ChildCustomerMapDTO>(parseDecl.getChildCustomer().values()))
                        .classCustomerList(new ArrayList<ClassCustomerMapDTO>(parseDecl.getClassCustomer().values()))
                        .build());

                return declOutputDTO.flatMap(eachDecl -> {
                    return Mono.zip(
                            transactionRepository.findAllByTranId((eachDecl.getChildCustomerList().stream().map(ChildCustomerMapDTO::getTranId).collect(Collectors.toList()))).collectList(),
                            refundRepository.findAllByTranId((eachDecl.getClassCustomerList().stream().map(ClassCustomerMapDTO::getTranId).collect(Collectors.toList()))).collectList()
                    ).flatMap(tuple -> {
                        if (!DataUtils.isNullOrEmpty(transactionReq)) {
                            //neu id == null -> insert ban ghi moi
                            if (DataUtils.isNullOrEmpty(transactionReq.getTransactionReqid())) {
                                saveTran(new Transaction(null, transactionReq.getTransactionReqTranId(), transactionReq.getTransactionReqTitle(), transactionReq.getTransactionReqAmount(), transactionReq.getTransactionReqDescription(), LocalDateTime.now(), LocalDateTime.now(), 1));
                                return Mono.just(new BaseResponse(ERROR_CODE_01, SAVE_SUCCESSFULLY, null));
                            } else {
                                //neu id != null -> update ban ghi do
                                //check finby truoc do tim thay ban ghi do chua neu roi thi update neu chua co thi findby lai vao dn
                                Optional<Transaction> optionalTransaction = tuple.getT1().stream().filter(tran -> tran.getId().equals(transactionReq.getTransactionReqid())).findFirst();
                                if (optionalTransaction.isPresent()) {
                                    saveTran(new Transaction(optionalTransaction.get().getId(), transactionReq.getTransactionReqTranId(), transactionReq.getTransactionReqTitle(), transactionReq.getTransactionReqAmount(), transactionReq.getTransactionReqDescription(), LocalDateTime.now(), LocalDateTime.now(), 1));
                                    return Mono.just(new BaseResponse(ERROR_CODE_01, UPDATE_SUCCESSFULLY, null));
                                } else {
                                    return transactionRepository.findById(transactionReq.getTransactionReqid()).flatMap(checkTransaction -> {
                                        if (DataUtils.isNullOrEmpty(checkTransaction)) {
                                            return transactionRepository.save(new Transaction(optionalTransaction.get().getId(), transactionReq.getTransactionReqTranId(), transactionReq.getTransactionReqTitle(), transactionReq.getTransactionReqAmount(), transactionReq.getTransactionReqDescription(), LocalDateTime.now(), LocalDateTime.now(), 1)).flatMap(tran -> {
                                                return Mono.just(new BaseResponse(ERROR_CODE_01, UPDATE_SUCCESSFULLY, null));
                                            });
                                        } else {
                                            return Mono.just(new BaseResponse(ERROR_CODE_500, UPDATE_UNSUCCESSFULLY, null));
                                        }
                                    });
                                }
                            }
                        }
                        if (!DataUtils.isNullOrEmpty(refundReq)) {
                            //neu id == null -> insert ban ghi moi
                            if (DataUtils.isNullOrEmpty(refundReq.getRefundReqId())) {
                                return refundRepository.save(new Refund(null, refundReq.getRefundReqTranId(), refundReq.getRefundReqType(), refundReq.getRefundReqNote(), LocalDateTime.now(), LocalDateTime.now(), 1)).flatMap(ref -> {
                                    return Mono.just(new BaseResponse(ERROR_CODE_01, SAVE_SUCCESSFULLY, null));
                                });
                            } else {
                                //check finby truoc do tim thay ban ghi do chua neu roi thi update neu chua co thi findby lai vao dn
                                Optional<Refund> optionalRefund = tuple.getT2().stream().filter(ref -> ref.getId().equals(refundReq.getRefundReqId())).findFirst();
                                if (optionalRefund.isPresent()) {
                                    return refundRepository.save(
                                            new Refund(optionalRefund.get().getId(), refundReq.getRefundReqTranId(), refundReq.getRefundReqType(), refundReq.getRefundReqNote(), LocalDateTime.now(), LocalDateTime.now(), 1)
                                    ).flatMap(refund -> {
                                        return Mono.just(new BaseResponse(ERROR_CODE_01, UPDATE_SUCCESSFULLY, null));
                                    });
                                } else {
                                    return refundRepository.findById(refundReq.getRefundReqId()).flatMap(checkRefund -> {
                                        if (DataUtils.isNullOrEmpty(checkRefund)) {
                                            return refundRepository.save(
                                                    new Refund(refundReq.getRefundReqId(), refundReq.getRefundReqTranId(), refundReq.getRefundReqType(), refundReq.getRefundReqNote(), LocalDateTime.now(), LocalDateTime.now(), 1)
                                            ).flatMap(a -> {
                                                return Mono.just(new BaseResponse(ERROR_CODE_01, UPDATE_SUCCESSFULLY, null));
                                            });
                                        }
                                        return Mono.just(new BaseResponse(ERROR_CODE_500, UPDATE_UNSUCCESSFULLY, null));
                                    });
                                }
                            }
                        }
                        if (DataUtils.isNullOrEmpty(tuple.getT1())) {
                            return Mono.just(new BaseResponse(ERROR_CODE_500, "Thành cong", null));
                        }
                        if (DataUtils.isNullOrEmpty(tuple.getT2())) {
                            return Mono.just(new BaseResponse(ERROR_CODE_01, "Không có hoan tra nào hop le", null));
                        }
                        eachDecl.getChildCustomerList().forEach(childCus -> {
                            Optional<Transaction> transactionOptional = tuple.getT1().stream().filter(transaction -> transaction.getTranId().equals(childCus.getTranId())).findFirst();
                            if (transactionOptional.isPresent()) {
                                Transaction transaction = transactionOptional.get();
                                //Truong hop isDelete = true -> xoa cac ban ghi (DELETE)
                                if (!DataUtils.isNullOrEmpty(isDelete) && isDelete) {
                                    transaction.setStatus(0);
                                    transaction.setUpdatedAt(LocalDateTime.now());
                                    transactionRepository.save(transaction).subscribe(a -> {
                                    });
                                }
                                //set lai cac truong amount, title, description (GET)
                                childCus.setAmount(transaction.getAmount());
                                childCus.setTitle(transaction.getTitle());
                                childCus.setDescription(transaction.getDescription());
                            }
                        });
                        eachDecl.getClassCustomerList().forEach(classCus -> {
                            Optional<Refund> refundOptional = tuple.getT2().stream().filter(refund -> refund.getTranId().equals(classCus.getTranId())).findFirst();
                            if (refundOptional.isPresent()) {
                                Refund refund = refundOptional.get();
                                if (!DataUtils.isNullOrEmpty(isDelete) && isDelete) {
                                    refund.setStatus(0);
                                    refund.setUpdatedAt(LocalDateTime.now());
                                    saveRef(refund);
                                }
                                //set lai cac truong type, note
                                classCus.setType(refund.getType());
                                classCus.setNote(refund.getNote());
                            }
                        });
                        if (isDelete) {
                            return Mono.just(new BaseResponse(ERROR_CODE_01, "Xoá Thành công", null));
                        }
                        return Mono.just(new BaseResponse(ERROR_CODE_01, "TC", eachDecl));
                    });
                });
            });
        });
    }

    public void saveTran(Transaction transaction) {
        transactionRepository.save(transaction).subscribe(tran -> {
        });
    }

    public void saveRef(Refund refund) {
        refundRepository.save(refund).subscribe(ref -> {
        });
    }
}
