//package com.pranav.payment_service.elasticsearch;
//
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//
//import java.util.List;
//
//public interface PaymentElasticsearchRepository
//        extends ElasticsearchRepository<PaymentDocument, String> {
//
//    List<PaymentDocument> findByMerchantId(Long merchantId);
//
//    List<PaymentDocument> findByCustomerEmail(String customerEmail);
//
//    List<PaymentDocument> findByStatus(String status);
//
//    List<PaymentDocument> findByMerchantIdAndStatus(Long merchantId,
//                                                    String status);
//
//    List<PaymentDocument> findByPaymentReference(String paymentReference);
//}