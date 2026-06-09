//package com.pranav.payment_service.elasticsearch;
//
//import lombok.*;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Document(indexName = "payments")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class PaymentDocument {
//
//    @Id
//    private String id;                  // UUID as String
//
//    @Field(type = FieldType.Long)
//    private Long merchantId;
//
//    @Field(type = FieldType.Keyword)
//    private String paymentReference;
//
//    @Field(type = FieldType.Keyword)
//    private String merchantOrderId;
//
//    @Field(type = FieldType.Double)
//    private BigDecimal amount;
//
//    @Field(type = FieldType.Keyword)
//    private String currency;
//
//    @Field(type = FieldType.Keyword)
//    private String status;
//
//    @Field(type = FieldType.Keyword)
//    private String paymentMethod;
//
//    @Field(type = FieldType.Text,
//            analyzer = "standard")
//    private String customerName;
//
//    @Field(type = FieldType.Keyword)
//    private String customerEmail;
//
//    @Field(type = FieldType.Keyword)
//    private String customerPhone;
//
//    @Field(type = FieldType.Text)
//    private String description;
//
//    @Field(type = FieldType.Keyword)
//    private String razorpayOrderId;
//
//    @Field(type = FieldType.Keyword)
//    private String razorpayPaymentId;
//
//    @Field(type = FieldType.Date,
//            format = {},
//            pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
//    private LocalDateTime createdAt;
//
//    @Field(type = FieldType.Date,
//            format = {},
//            pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
//    private LocalDateTime updatedAt;
//}