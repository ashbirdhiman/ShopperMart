package com.shoppermart.orderservice.Fullfillment.event;

import java.math.BigDecimal;

/**
 * PaymentCompletedEvent
 * Represents payment processing result from payment service
 * Can be used for both success (payment-completed) and failure (payment-failed) scenarios
 */
public class PaymentCompletedEvent {
    
    private Long orderNumber;
    private String customerEmail;
    private BigDecimal amount;
    private String transactionId;
    private String paymentMethod;
    private String status; // "SUCCESS" or "FAILED"
    private String failureReason;

    public PaymentCompletedEvent() {}

    public PaymentCompletedEvent(Long orderNumber, String customerEmail, BigDecimal amount) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.status = "SUCCESS";
    }

    public PaymentCompletedEvent(Long orderNumber, String customerEmail, BigDecimal amount, 
                                 String transactionId, String paymentMethod) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.status = "SUCCESS";
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @Override
    public String toString() {
        return "PaymentCompletedEvent{" +
                "orderNumber=" + orderNumber +
                ", customerEmail='" + customerEmail + '\'' +
                ", amount=" + amount +
                ", transactionId='" + transactionId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
