package com.shoppermart.orderservice.Fullfillment.event;

import com.shoppermart.orderservice.Model.OrderEvent;

import java.math.BigDecimal;
import java.util.Map;

public class OrderPlaceEvent {

   private Long orderNumber;
   private String customerEmail;

   private Map<String, Integer> skuCodeQuantityMap;
   
   private Map<String, Integer> previousQuantityMap;

   private OrderEvent orderEvent;

   private BigDecimal totalPrice;

    public OrderPlaceEvent(Long orderNumber, String customerEmail, Map<String, Integer> skuCodeQuantityMap, OrderEvent orderEvent, BigDecimal totalPrice) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.skuCodeQuantityMap = skuCodeQuantityMap;
        this.orderEvent = orderEvent;
        this.totalPrice = totalPrice;
        this.previousQuantityMap = null;
    }
    
    public OrderPlaceEvent(Long orderNumber, String customerEmail, Map<String, Integer> skuCodeQuantityMap, Map<String, Integer> previousQuantityMap, OrderEvent orderEvent, BigDecimal totalPrice) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.skuCodeQuantityMap = skuCodeQuantityMap;
        this.previousQuantityMap = previousQuantityMap;
        this.orderEvent = orderEvent;
        this.totalPrice = totalPrice;
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

    public Map<String, Integer> getSkuCodeQuantityMap() {
        return skuCodeQuantityMap;
    }

    public void setSkuCodeQuantityMap(Map<String, Integer> skuCodeQuantityMap) {
        this.skuCodeQuantityMap = skuCodeQuantityMap;
    }
    
    public Map<String, Integer> getPreviousQuantityMap() {
        return previousQuantityMap;
    }

    public void setPreviousQuantityMap(Map<String, Integer> previousQuantityMap) {
        this.previousQuantityMap = previousQuantityMap;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderEvent getOrderEvent() {
        return orderEvent;
    }

    public void setOrderEvent(OrderEvent orderEvent) {
        this.orderEvent = orderEvent;
    }
}
