package com.shoppermart.orderservice.Fullfillment.event;

import java.time.LocalDateTime;

/**
 * NotificationEvent
 * Represents notification to be sent to the customer
 * Published by OrderEventsListener to notification service
 */
public class NotificationEvent {
    
    private Long orderNumber;
    private String customerEmail;
    private String subject;
    private String message;
    private String notificationType; // "EMAIL", "SMS", "PUSH"
    private LocalDateTime createdAt;

    public NotificationEvent() {}

    public NotificationEvent(Long orderNumber, String customerEmail, String subject, String message) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.subject = subject;
        this.message = message;
        this.notificationType = "EMAIL";
        this.createdAt = LocalDateTime.now();
    }

    public NotificationEvent(Long orderNumber, String customerEmail, String subject, 
                            String message, String notificationType) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.subject = subject;
        this.message = message;
        this.notificationType = notificationType;
        this.createdAt = LocalDateTime.now();
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "NotificationEvent{" +
                "orderNumber=" + orderNumber +
                ", customerEmail='" + customerEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", notificationType='" + notificationType + '\'' +
                '}';
    }
}
