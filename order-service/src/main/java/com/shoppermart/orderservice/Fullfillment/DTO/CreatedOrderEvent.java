package com.shoppermart.orderservice.Fullfillment.DTO;

import com.shoppermart.orderservice.Model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class CreatedOrderEvent {

    private Long orderId;

    private String customerId;

    private String userEmail;

    private Map<String, Integer> skuCodeQuantityMap;

    private BigDecimal totalPrice;

    private OrderStatus status;






    private LocalDateTime createdAt;

    public CreatedOrderEvent(Long orderId, String customerId, String userEmail, Map<String, Integer> skuCodeQuantityMap, BigDecimal totalPrice, OrderStatus status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.userEmail = userEmail;
        this.skuCodeQuantityMap = skuCodeQuantityMap;
        this.totalPrice = totalPrice;
        this.status = status;


        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Map<String, Integer> getSkuCodeQuantityMap() {
        return skuCodeQuantityMap;
    }

    public void setSkuCodeQuantityMap(Map<String, Integer> skuCodeQuantityMap) {
        this.skuCodeQuantityMap = skuCodeQuantityMap;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
