package com.ecomm.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderDto {
    private String id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer itemsCount;
    private BigDecimal totalPrice;
    private String paymentStatus;
    private String paymentMethod;
    private String razorpayPaymentId;
    private String fulfilmentStatus;
    private String carrier;
    private String trackingNumber;
    private String deliveryOtp;
    private Boolean otpVerified;
    private Instant createdAt;

    public OrderDto() {}

    public OrderDto(String id, String orderNumber, String customerName, String customerEmail, String customerPhone,
                    Integer itemsCount, BigDecimal totalPrice, String paymentStatus, String paymentMethod,
                    String razorpayPaymentId, String fulfilmentStatus, String carrier,
                    String trackingNumber, String deliveryOtp, Boolean otpVerified, Instant createdAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.itemsCount = itemsCount;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.razorpayPaymentId = razorpayPaymentId;
        this.fulfilmentStatus = fulfilmentStatus;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.deliveryOtp = deliveryOtp;
        this.otpVerified = otpVerified;
        this.createdAt = createdAt;
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
}
