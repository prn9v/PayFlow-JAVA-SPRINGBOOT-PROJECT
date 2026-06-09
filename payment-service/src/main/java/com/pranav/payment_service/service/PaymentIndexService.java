//package com.pranav.payment_service.service;
//
//import com.pranav.payment_service.elasticsearch.PaymentDocument;
//import com.pranav.payment_service.elasticsearch.PaymentElasticsearchRepository;
//import com.pranav.payment_service.entity.Payment;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentIndexService {
//
//    private final PaymentElasticsearchRepository elasticsearchRepository;
//
//    // ─── Index or update a payment ────────────────────────────────────────────
//
//    @Async
//    public void indexPayment(Payment payment) {
//        try {
//            PaymentDocument doc = toDocument(payment);
//            elasticsearchRepository.save(doc);
//            log.info("Indexed payment: {} to Elasticsearch",
//                    payment.getPaymentReference());
//        } catch (Exception e) {
//            // Never fail the main flow if ES indexing fails
//            log.error("Failed to index payment {}: {}",
//                    payment.getId(), e.getMessage());
//        }
//    }
//
//    // ─── Search methods ───────────────────────────────────────────────────────
//
//    public List<PaymentDocument> searchByMerchantId(Long merchantId) {
//        return elasticsearchRepository.findByMerchantId(merchantId);
//    }
//
//    public List<PaymentDocument> searchByCustomerEmail(String email) {
//        return elasticsearchRepository.findByCustomerEmail(email);
//    }
//
//    public List<PaymentDocument> searchByStatus(String status) {
//        return elasticsearchRepository.findByStatus(status);
//    }
//
//    public List<PaymentDocument> searchByMerchantAndStatus(Long merchantId,
//                                                           String status) {
//        return elasticsearchRepository.findByMerchantIdAndStatus(
//                merchantId, status);
//    }
//
//    public List<PaymentDocument> searchByReference(String reference) {
//        return elasticsearchRepository.findByPaymentReference(reference);
//    }
//
//    // ─── Mapper ───────────────────────────────────────────────────────────────
//
//    private PaymentDocument toDocument(Payment payment) {
//        return PaymentDocument.builder()
//                .id(payment.getId().toString())
//                .merchantId(payment.getMerchantId())
//                .paymentReference(payment.getPaymentReference())
//                .merchantOrderId(payment.getMerchantOrderId())
//                .amount(payment.getAmount())
//                .currency(payment.getCurrency())
//                .status(payment.getStatus().name())
//                .paymentMethod(payment.getPaymentMethod() != null
//                        ? payment.getPaymentMethod().name() : null)
//                .customerName(payment.getCustomerName())
//                .customerEmail(payment.getCustomerEmail())
//                .customerPhone(payment.getCustomerPhone())
//                .description(payment.getDescription())
//                .razorpayOrderId(payment.getRazorpayOrderId())
//                .razorpayPaymentId(payment.getRazorpayPaymentId())
//                .createdAt(payment.getCreatedAt())
//                .updatedAt(payment.getUpdatedAt())
//                .build();
//    }
//}