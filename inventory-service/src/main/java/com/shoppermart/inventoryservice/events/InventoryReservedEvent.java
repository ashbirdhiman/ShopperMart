package com.shoppermart.inventoryservice.events;

import java.math.BigDecimal;
import java.util.Map;

public class InventoryReservedEvent {


    private Long orderNumber;
    private String customerEmail;

    private BigDecimal totalPrice;

    public InventoryReservedEvent(Long orderNumber, String customerEmail, BigDecimal totalPrice) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
    }


    public InventoryReservedEvent() {
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
}

