package com.ecomm.product.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false, length = 128)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 128)
    private String customerEmail;

    @Column(name = "customer_phone", length = 32)
    private String customerPhone = "+91 98765 43210";

    @Column(name = "items_count", nullable = false)
    private Integer itemsCount = 1;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "payment_status", nullable = false, length = 32)
    private String paymentStatus = "PAID";

    @Column(name = "payment_method", nullable = false, length = 32)
    private String paymentMethod = "RAZORPAY";

    @Column(name = "razorpay_payment_id", length = 64)
    private String razorpayPaymentId;

    @Column(name = "fulfilment_status", nullable = false, length = 32)
    private String fulfilmentStatus = "PROCESSING";

    @Column(nullable = false, length = 64)
    private String carrier = "FedEx Express";

    @Column(name = "tracking_number", nullable = false, length = 64)
    private String trackingNumber = "TRK-98231";

    @Column(name = "delivery_otp", length = 16)
    private String deliveryOtp;

    @Column(name = "otp_verified")
    private Boolean otpVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Order() {}

    public Order(String id, String orderNumber, String customerName, String customerEmail, String customerPhone,
                 Integer itemsCount, BigDecimal totalPrice, String paymentStatus, String paymentMethod,
                 String razorpayPaymentId, String fulfilmentStatus, String carrier,
                 String trackingNumber, String deliveryOtp, Boolean otpVerified,
                 Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone != null ? customerPhone : "+91 98765 43210";
        this.itemsCount = itemsCount != null ? itemsCount : 1;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus != null ? paymentStatus : "PAID";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "RAZORPAY";
        this.razorpayPaymentId = razorpayPaymentId;
        this.fulfilmentStatus = fulfilmentStatus != null ? fulfilmentStatus : "PROCESSING";
        this.carrier = carrier != null ? carrier : "FedEx Express";
        this.trackingNumber = trackingNumber != null ? trackingNumber : "TRK-98231";
        this.deliveryOtp = deliveryOtp;
        this.otpVerified = otpVerified != null ? otpVerified : false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Integer getItemsCount() { return itemsCount; }
    public void setItemsCount(Integer itemsCount) { this.itemsCount = itemsCount; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getFulfilmentStatus() { return fulfilmentStatus; }
    public void setFulfilmentStatus(String fulfilmentStatus) { this.fulfilmentStatus = fulfilmentStatus; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getDeliveryOtp() { return deliveryOtp; }
    public void setDeliveryOtp(String deliveryOtp) { this.deliveryOtp = deliveryOtp; }

    public Boolean getOtpVerified() { return otpVerified; }
    public void setOtpVerified(Boolean otpVerified) { this.otpVerified = otpVerified; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
