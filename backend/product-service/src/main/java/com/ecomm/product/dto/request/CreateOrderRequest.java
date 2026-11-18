package com.ecomm.product.dto.request;

import java.math.BigDecimal;

public class CreateOrderRequest {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer itemsCount;
    private BigDecimal totalPrice;
    private String razorpayPaymentId;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String customerName, String customerEmail, String customerPhone, Integer itemsCount, BigDecimal totalPrice, String razorpayPaymentId) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.itemsCount = itemsCount;
        this.totalPrice = totalPrice;
        this.razorpayPaymentId = razorpayPaymentId;
    }

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

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
}
