# Order Fulfillment Workflow - Async Event-Driven Architecture

## Overview

This implementation provides a complete asynchronous, event-driven order fulfillment workflow using Apache Kafka. The workflow orchestrates communication between multiple microservices through event-based messaging.

## Workflow Flow

```
Order Creation → Inventory → Payment → Shipping → Notification → Delivery
     ↓              ↓          ↓         ↓           ↓              ↓
  PENDING    CONFIRMED  PAYMENT_   SHIPPED   [notification   DELIVERED
                       COMPLETED             sent to customer]
```

## Components

### 1. **OrderFulfillmentWorkflow** (RestController)

**Responsibility:** Order creation and initiation of async workflow

**Flow:**

- Receives `CreatedOrderEvent` from client
- Creates `Order` entity in database with `PENDING` status
- Publishes order to Kafka topic `order-placed`
- Returns `202 ACCEPTED` (async processing)

**Kafka Topics Published:**

- `order-placed` → Inventory Service

---

### 2. **OrderEventsListener** (Kafka Consumer)

**Responsibility:** Orchestrates the entire workflow by consuming events from downstream services

#### Event Handlers:

| Topic                 | Status            | Next Action       | Kafka Publish         |
| --------------------- | ----------------- | ----------------- | --------------------- |
| `inventory-reserved`  | CONFIRMED         | Send to payment   | `payment-required`    |
| `inventory-failed`    | INVENTORY_PENDING | Send notification | `notification-events` |
| `payment-completed`   | PAYMENT_COMPLETED | Send to shipping  | `shipping-required`   |
| `payment-failed`      | PAYMENT_FAILED    | Send notification | `notification-events` |
| `shipping-dispatched` | SHIPPED           | Send notification | `notification-events` |
| `shipping-failed`     | SHIPMENT_FAILED   | Send notification | `notification-events` |
| `delivery-confirmed`  | DELIVERED         | Send notification | `notification-events` |

---

## Order Statuses

```java
enum OrderStatus {
    PENDING,              // Initial state, order created
    CONFIRMED,            // Inventory reserved successfully
    PAYMENT_COMPLETED,    // Payment processed
    SHIPPED,              // Item dispatched from warehouse
    DELIVERED,            // Item delivered to customer
    CANCELLED,            // Order cancelled
    INVENTORY_PENDING,    // Waiting for inventory (failed once)
    PAYMENT_FAILED,       // Payment processing failed
    SHIPMENT_FAILED,      // Shipping failed
    FAILED                // General failure
}
```

---

## Kafka Topics

### Published by Order Service:

- **`order-placed`** → Inventory Service
  - Message: `OrderPlaceEvent`

### Consumed by Order Service:

- **`inventory-reserved`** ← Inventory Service
  - Message: `InventoryReservedEvent` (status: SUCCESS)
- **`inventory-failed`** ← Inventory Service
  - Message: `InventoryReservedEvent` (status: FAILED)
- **`payment-completed`** ← Payment Service
  - Message: `PaymentCompletedEvent` (status: SUCCESS)
- **`payment-failed`** ← Payment Service
  - Message: `PaymentCompletedEvent` (status: FAILED)
- **`shipping-dispatched`** ← Shipping Service
  - Message: `ShippingDispatchedEvent` (status: DISPATCHED)
- **`shipping-failed`** ← Shipping Service
  - Message: `ShippingDispatchedEvent` (status: FAILED)
- **`delivery-confirmed`** ← Shipping Service
  - Message: `ShippingDispatchedEvent` (status: DELIVERED)

### Published to:

- **`payment-required`** → Payment Service
  - Message: `PaymentCompletedEvent`
- **`shipping-required`** → Shipping Service
  - Message: `ShippingDispatchedEvent`
- **`notification-events`** → Notification Service
  - Message: `NotificationEvent`

---

## Event Classes

### 1. **OrderPlaceEvent**

```java
{
    orderNumber: Long,
    customerEmail: String,
    skuCodeQuantityMap: Map<String, Integer>,
    orderEvent: OrderEvent (ORDER_CREATED / ORDER_UPDATED),
    totalPrice: BigDecimal
}
```

### 2. **InventoryReservedEvent**

```java
{
    orderNumber: Long,
    customerEmail: String,
    totalPrice: BigDecimal,
    status: String ("RESERVED" / "FAILED"),
    failureReason: String (optional)
}
```

### 3. **PaymentCompletedEvent**

```java
{
    orderNumber: Long,
    customerEmail: String,
    amount: BigDecimal,
    transactionId: String,
    paymentMethod: String,
    status: String ("SUCCESS" / "FAILED"),
    failureReason: String (optional)
}
```

### 4. **ShippingDispatchedEvent**

```java
{
    orderNumber: Long,
    customerEmail: String,
    trackingNumber: String,
    carrier: String,
    status: String ("DISPATCHED" / "IN_TRANSIT" / "DELIVERED" / "FAILED"),
    failureReason: String (optional),
    dispatchedAt: LocalDateTime,
    estimatedDelivery: LocalDateTime
}
```

### 5. **NotificationEvent**

```java
{
    orderNumber: Long,
    customerEmail: String,
    subject: String,
    message: String,
    notificationType: String ("EMAIL" / "SMS" / "PUSH"),
    createdAt: LocalDateTime
}
```

---

## Error Handling & Compensation

### Inventory Failure

- Order status → `INVENTORY_PENDING`
- Customer notified
- **Compensation:** Item remains reserved (can be retried or manually reviewed)

### Payment Failure

- Order status → `PAYMENT_FAILED`
- Customer notified
- **Compensation:** Trigger inventory service to release reserved items

### Shipping Failure

- Order status → `SHIPMENT_FAILED`
- Customer notified
- **Compensation:** Trigger inventory to re-reserve items for another shipment attempt

---

## Implementation Highlights

✅ **Asynchronous Processing** - Non-blocking workflow using Kafka
✅ **Status Tracking** - Clear order status at each stage
✅ **Error Handling** - Comprehensive exception handling with logging
✅ **Event Orchestration** - Proper sequencing of services
✅ **Customer Notifications** - Automatic notifications at key milestones
✅ **Idempotency** - Designed to handle duplicate events (use orderNumber as idempotency key)
✅ **Logging** - Detailed audit trail for debugging and monitoring

---

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

---

## Configuration Required

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: order-service
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    listener:
      ack-mode: manual_immediate
```

---

## Testing the Workflow

### 1. Create Order

```bash
curl -X POST http://localhost:8080/v1/createOrder \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "totalPrice": 99.99,
    "skuCodeQuantityMap": {
      "SKU-001": 2,
      "SKU-002": 1
    }
  }'
```

Response: `202 ACCEPTED`

```json
{
  "orderId": 1,
  "status": "PENDING"
}
```

### 2. Monitor Order Status

```bash
curl http://localhost:8080/v1/orders/1
```

---

## Monitoring & Observability

Monitor Kafka topics:

```bash
kafka-console-consumer.sh --topic inventory-reserved --from-beginning
kafka-console-consumer.sh --topic payment-completed --from-beginning
kafka-console-consumer.sh --topic shipping-dispatched --from-beginning
```

Check logs:

```
INFO  OrderFulfillmentWorkflow: Order created successfully with ID: 1
INFO  OrderEventsListener: Inventory Reserved Event received for order: 1
INFO  OrderEventsListener: Order sent to payment service for processing
INFO  OrderEventsListener: Order status updated to PAYMENT_COMPLETED for order: 1
INFO  OrderEventsListener: Order sent to shipping service for dispatch
```

---

## Future Enhancements

- [ ] Implement saga pattern with compensating transactions
- [ ] Add retry logic with exponential backoff
- [ ] Implement dead letter queue (DLQ) for failed events
- [ ] Add distributed tracing (Spring Cloud Sleuth)
- [ ] Implement order cancellation workflow
- [ ] Add analytics and metrics collection
- [ ] Implement customer notification preferences
- [ ] Add order partial shipment handling
