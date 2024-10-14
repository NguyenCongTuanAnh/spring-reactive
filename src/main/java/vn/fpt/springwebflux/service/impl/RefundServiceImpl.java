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
import java.util.Comparator;
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
        return apiService.getDecl(DECL_CODE)
                .flatMap(decl -> {
                    if (DataUtils.isNullOrEmpty(decl)) {
                        return Mono.error(new BusinessException(ERROR_CODE_500, NOT_FOUND_DECL, null));
                    }
                    return parseAndProcessDecl(decl.getValue(), transactionReq, refundReq, isDelete);
                });
    }

    private Mono<BaseResponse> parseAndProcessDecl(String declValue, TransactionReq transactionReq, RefundReq refundReq, Boolean isDelete) {
        return DataUtils.parseStringToObject(declValue, DeclInputDTO.class)
                .flatMap(parsedDecl -> {
                    if (DataUtils.isNullOrEmpty(parsedDecl)){
                        return Mono.just(new BaseResponse(ERROR_CODE_500, PARSE_DATA_UNSUCCESSFULLY, null));
                    }
                    DeclOutputDTO declOutputDTO = createDeclOutputDTO(parsedDecl);
                    return processTransactionsAndRefunds(declOutputDTO, transactionReq, refundReq, isDelete);
                });
    }

    private DeclOutputDTO createDeclOutputDTO(DeclInputDTO parsedDecl) {
        return DeclOutputDTO.builder()
                .name(parsedDecl.getName())
                .sortOrder(parsedDecl.getSortOrder())
                .childCustomerList(new ArrayList<>(parsedDecl.getChildCustomer().values()))
                .classCustomerList(new ArrayList<>(parsedDecl.getClassCustomer().values()))
                .build();
    }

    private Mono<BaseResponse> processTransactionsAndRefunds(DeclOutputDTO declOutputDTO, TransactionReq transactionReq, RefundReq refundReq, Boolean isDelete) {
        return Mono.zip(
                transactionRepository.findAllByTranId(getTranIds(declOutputDTO.getChildCustomerList())).collectList(),
                refundRepository.findAllByTranId(getTranIds(declOutputDTO.getClassCustomerList())).collectList()
        ).flatMap(tuple -> handleRequests(transactionReq, refundReq, isDelete, declOutputDTO, tuple.getT1(), tuple.getT2()));
    }

    private <T> java.util.List<String> getTranIds(java.util.List<T> list) {
        return list.stream()
                .map(item -> {
                    if (item instanceof ChildCustomerMapDTO) return ((ChildCustomerMapDTO) item).getTranId();
                    else if (item instanceof ClassCustomerMapDTO) return ((ClassCustomerMapDTO) item).getTranId();
                    else return null;
                })
                .collect(Collectors.toList());
    }

    private Mono<BaseResponse> handleRequests(TransactionReq transactionReq, RefundReq refundReq, Boolean isDelete, DeclOutputDTO declOutputDTO, java.util.List<Transaction> transactions, java.util.List<Refund> refunds) {
        return handleTransaction(transactionReq, transactions)
                .thenReturn(handleRefund(refundReq, refunds))
                .thenReturn(updateCustomerDetails(declOutputDTO, isDelete, transactions, refunds))
                .thenReturn(new BaseResponse(ERROR_CODE_01, "Success", declOutputDTO));

    }

    private Mono<BaseResponse> handleTransaction(TransactionReq transactionReq, java.util.List<Transaction> transactions) {
        if (!transactionReq.isValidate()) {
            return Mono.just(  new BaseResponse(ERROR_CODE_01, SAVE_UNSUCCESSFULLY, null));
        }

        return findAndSaveTransaction(transactionReq, transactions);
    }

    private Mono<BaseResponse> findAndSaveTransaction(TransactionReq transactionReq, java.util.List<Transaction> transactions) {
      //validate input
        transactionReq.isValidate();
       //Neu ipput ko co truyen id - insert ban ghi moi
        if (DataUtils.isNullOrEmpty(transactionReq.getTransactionReqid())){
           Transaction transaction = new Transaction();
           transaction.setTranId(transactionReq.getTransactionReqTranId());
           transaction.setTitle(transactionReq.getTransactionReqTitle());
           transaction.setAmount(transactionReq.getTransactionReqAmount());
           transaction.setDescription(transactionReq.getTransactionReqDescription());
           transaction.setUpdatedAt(LocalDateTime.now());
           transaction.setStatus(ACTIVE_STATUS);
           return transactionRepository.save(transaction).map(tran -> {
               return new BaseResponse(ERROR_CODE_01, SAVE_UNSUCCESSFULLY, null);
           });
       }
        //neu iput cis truyen id -> neu khong tim thay transaction thi bao loi
        //neu tim thay thif update lai cac truong vao db
        return transactionRepository.findById(transactionReq.getTransactionReqid())
                .flatMap(transaction -> {
                    if (DataUtils.isNullOrEmpty(transaction)){
                        return Mono.error(new BusinessException(ERROR_CODE_500, NOT_FOUND_TRANSACTION,null));
                    }
                    transaction.setTranId(transactionReq.getTransactionReqTranId());
                    transaction.setTitle(transactionReq.getTransactionReqTitle());
                    transaction.setAmount(transactionReq.getTransactionReqAmount());
                    transaction.setDescription(transactionReq.getTransactionReqDescription());
                    transaction.setUpdatedAt(LocalDateTime.now());
                    transaction.setStatus(1);
                    if (!DataUtils.isNullOrEmpty(transactionReq.getTransactionReqid())){
                        transaction.setId(transactionReq.getTransactionReqid());
                        return transactionRepository.save(transaction).map(tran -> {
                            return new BaseResponse(ERROR_CODE_01, SAVE_UNSUCCESSFULLY, null);
                        });
                    }
                    return transactionRepository.save(transaction).map(tran -> {
                        return new BaseResponse(ERROR_CODE_01, SAVE_UNSUCCESSFULLY, null);
                    });
                });
    }
    private Mono<BaseResponse> handleRefund(RefundReq refundReq, java.util.List<Refund> refunds) {
        if (DataUtils.isNullOrEmpty(refundReq.getRefundReqTranId())) return Mono.empty();

        return findAndSaveRefund(refundReq, refunds)
                .flatMap(ref -> Mono.just(new BaseResponse(ERROR_CODE_01, SAVE_UNSUCCESSFULLY, null)));
    }

    private Mono<BaseResponse> findAndSaveRefund(RefundReq refundReq, java.util.List<Refund> refunds) {
        //validate input
        refundReq.isValidate();
        //Neu ipput ko co truyen id - insert ban ghi moi
        if (DataUtils.isNullOrEmpty(refundReq.getRefundReqId())){
            Refund refund = new Refund();
            refund.setTranId(refundReq.getRefundReqTranId());
            refund.setType(refundReq.getRefundReqType());
            refund.setNote(refundReq.getRefundReqNote());
            refund.setUpdatedAt(LocalDateTime.now());
            refund.setStatus(ACTIVE_STATUS);
            return refundRepository.save(refund).map(tran -> {
                return new BaseResponse(ERROR_CODE_01, SAVE_UNSUCCESSFULLY, null);
            });
        }
        return refundRepository.findById(refundReq.getRefundReqId())
                .flatMap(refund -> {
                    if (DataUtils.isNullOrEmpty(refund)){
                        return Mono.error(new BusinessException(ERROR_CODE_500, NOT_FOUND_TRANSACTION,null));
                    }
                    refund.setTranId(refundReq.getRefundReqTranId());
                    refund.setType(refundReq.getRefundReqType());
                    refund.setNote(refundReq.getRefundReqNote());
                    refund.setUpdatedAt(LocalDateTime.now());
                    refund.setStatus(1);
                    return refundRepository.save(refund).map(ref -> new BaseResponse(ERROR_CODE_01, SAVE_UNSUCCESSFULLY, null));
                });
    }
    private Mono<BaseResponse> updateCustomerDetails(DeclOutputDTO declOutputDTO, Boolean isDelete, java.util.List<Transaction> transactions, java.util.List<Refund> refunds) {
        declOutputDTO.getChildCustomerList().forEach(child -> updateChildCustomer(child, transactions, isDelete));
        declOutputDTO.getClassCustomerList().forEach(cls -> updateClassCustomer(cls, refunds, isDelete));

        sortCustomers(declOutputDTO);
        return Mono.just(new BaseResponse(ERROR_CODE_01, UPDATE_SUCCESSFULLY, null));
    }

    private void updateChildCustomer(ChildCustomerMapDTO child, java.util.List<Transaction> transactions, Boolean isDelete) {
        transactions.stream()
                .filter(transaction -> transaction.getTranId().equals(child.getTranId()))
                .findFirst()
                .ifPresent(transaction -> {
                    if (Boolean.TRUE.equals(isDelete)) {
                        transaction.setStatus(0);
                        transaction.setUpdatedAt(LocalDateTime.now());
                        transactionRepository.save(transaction).subscribe();
                    } else {
                        child.setAmount(transaction.getAmount());
                        child.setTitle(transaction.getTitle());
                        child.setDescription(transaction.getDescription());
                    }
                });
    }

    private void updateClassCustomer(ClassCustomerMapDTO cls, java.util.List<Refund> refunds, Boolean isDelete) {
        refunds.stream()
                .filter(refund -> refund.getTranId().equals(cls.getTranId()))
                .findFirst()
                .ifPresent(refund -> {
                    if (Boolean.TRUE.equals(isDelete)) {
                        refund.setStatus(0);
                        refund.setUpdatedAt(LocalDateTime.now());
                        refundRepository.save(refund).subscribe();
                    } else {
                        cls.setType(refund.getType());
                        cls.setNote(refund.getNote());
                    }
                });
    }

    private void sortCustomers(DeclOutputDTO declOutputDTO) {
        declOutputDTO.getChildCustomerList().sort(Comparator.comparing(ChildCustomerMapDTO::getSort));
        declOutputDTO.getClassCustomerList().sort(Comparator.comparing(ClassCustomerMapDTO::getSort).reversed());
    }
}
