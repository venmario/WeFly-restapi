package com.example.wefly_app.service;

import com.example.wefly_app.request.transaction.MidtransResponseModel;
import com.example.wefly_app.request.transaction.PaymentRegisterModel;
import com.example.wefly_app.request.transaction.TransactionSaveModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface TransactionService {
    Map<Object, Object> save(TransactionSaveModel request) throws IOException;
    Map<Object,Object> delete(Long request);
    Map<Object, Object> getById(Long request);
    Map<Object, Object> getAll(int page, int size, String orderBy, String orderType
            , String startDate, String endDate, String paymentStatus,
                               String exceptionStatus);
//    Map<Object, Object> getAllBank(int page, int size, String orderBy, String orderType);
    Map<Object, Object> midtransGetResponse(MidtransResponseModel orderId);
    Resource getPaymentProof (Long transactionId);
//    Map<Object, Object> savePayment(PaymentRegisterModel request);
//    Map<Object, Object> savePaymentProof(MultipartFile file, Long paymentId) throws IOException;
//    Resource getPaymentProof(Long paymentId);

}
