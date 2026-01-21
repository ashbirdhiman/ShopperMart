package com.shoppermart.notificationservice.order;

public class OrderPlaceEvent {

    private Long orderNumber;
    private String customerEmail;

    public OrderPlaceEvent() {
    }

    public OrderPlaceEvent(Long orderNumber, String customerID) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerID;
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
}
