package com.shoppermart.orderservice.Fullfillment.event;

import java.time.LocalDateTime;

/**
 * ShippingDispatchedEvent
 * Represents shipping status from shipping service
 * Used for dispatch confirmation, failure, and delivery confirmation
 */
public class ShippingDispatchedEvent {
    
    private Long orderNumber;
    private String customerEmail;
    private String trackingNumber;
    private String carrier;
    private String status; // "DISPATCHED", "IN_TRANSIT", "DELIVERED", "FAILED"
    private String failureReason;
    private LocalDateTime dispatchedAt;
    private LocalDateTime estimatedDelivery;

    public ShippingDispatchedEvent() {}

    public ShippingDispatchedEvent(Long orderNumber, String customerEmail) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.status = "DISPATCHED";
    }

    public ShippingDispatchedEvent(Long orderNumber, String customerEmail, String trackingNumber, String carrier) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.status = "DISPATCHED";
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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
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

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(LocalDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    @Override
    public String toString() {
        return "ShippingDispatchedEvent{" +
                "orderNumber=" + orderNumber +
                ", customerEmail='" + customerEmail + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", carrier='" + carrier + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
