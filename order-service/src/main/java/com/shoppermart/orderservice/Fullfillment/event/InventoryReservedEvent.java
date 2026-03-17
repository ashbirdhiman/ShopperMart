package com.shoppermart.orderservice.Fullfillment.event;

import java.math.BigDecimal;

/**
 * InventoryReservedEvent
 * Represents inventory reservation result from inventory service
 * Used for both successful reservation and failure scenarios
 */
public class InventoryReservedEvent {

    private Long orderNumber;
    private String customerEmail;
    private BigDecimal totalPrice;
    private String status; // "RESERVED" or "FAILED"
    private String failureReason;

    public InventoryReservedEvent() {}

    public InventoryReservedEvent(Long orderNumber, String customerEmail, BigDecimal totalPrice) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
        this.status = "RESERVED";
    }

    public InventoryReservedEvent(Long orderNumber, String customerEmail, BigDecimal totalPrice, String failureReason) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
        this.status = "FAILED";
        this.failureReason = failureReason;
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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
        return "InventoryReservedEvent{" +
                "orderNumber=" + orderNumber +
                ", customerEmail='" + customerEmail + '\'' +
                ", status='" + status + '\'' +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}

