package com.shoppermart.orderservice.Model;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", length = 36, nullable = false)
    private String customerId;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "sku_code")
    @Column(name = "quantity", columnDefinition = "INT DEFAULT 1", nullable = false)
    @Schema(description = "Map of SKU codes to quantities. Key: SKU code (e.g., 'SKU-LAPTOP-001'), Value: quantity ordered", 
            example = "{\"SKU-LAPTOP-001\": 2, \"SKU-MOUSE-001\": 5}")
    private Map<String, Integer> skuCodeQuantityMap;
    
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Order() {}

    public Order(Long id, String customerId, Map<String, Integer> skuCodeQuantityMap, BigDecimal totalPrice, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.skuCodeQuantityMap = skuCodeQuantityMap;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Order(String customerId, Map<String, Integer> skuCodeQuantityMap, BigDecimal totalPrice, OrderStatus status) {
        this.customerId = customerId;
        this.skuCodeQuantityMap = skuCodeQuantityMap;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", skuCodeQuantityMap=" + skuCodeQuantityMap +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (id != null ? !id.equals(order.id) : order.id != null) return false;
        if (customerId != null ? !customerId.equals(order.customerId) : order.customerId != null) return false;

        if (!Objects.equals(skuCodeQuantityMap, order.skuCodeQuantityMap)) return false;
        if (totalPrice != null ? !totalPrice.equals(order.totalPrice) : order.totalPrice != null) return false;
        if (status != order.status) return false;
        return createdAt != null ? createdAt.equals(order.createdAt) : order.createdAt == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (skuCodeQuantityMap != null ? skuCodeQuantityMap.hashCode() : 0);
        result = 31 * result + (totalPrice != null ? totalPrice.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String customerId;
        private Map<String, Integer> skuCodeQuantityMap;
        private BigDecimal totalPrice;
        private OrderStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder skuCodeQuantityMap(Map<String, Integer> skuCodeQuantityMap) {
            this.skuCodeQuantityMap = skuCodeQuantityMap;
            return this;
        }

        public Builder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Order build() {
            return new Order(id, customerId, skuCodeQuantityMap, totalPrice, status, createdAt);
        }
    }
}
