# Order Fulfillment Workflow - Architecture Diagram

## Complete Async Event Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                          CLIENT REQUEST                                              │
│                   POST /v1/createOrder/                                             │
│            {orderId, customerId, totalPrice, skuMap}                                │
└───────────────────────────────┬─────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │   ORDER FULFILLMENT  │
                    │      WORKFLOW        │
                    │  (RestController)    │
                    └──────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   1. Create        2. Persist       3. Publish
      Order           to DB            Event
    (PENDING)         (Order)    (order-placed)
                                        │
                                        ▼
                          ┌─────────────────────────┐
                          │  KAFKA BROKER           │
                          │  Topic: order-placed    │
                          └─────────────────────────┘
                                        │
        ┌───────────────────────────────┼───────────────────────────┐
        │                               │                           │
        ▼                               ▼                           ▼
   [INVENTORY SERVICE]          [INVENTORY SERVICE]        [Order Service Listener]
   Reserve SKUs                 Failed Reservation         (OrderEventsListener)
        │                               │                           │
        ├─ ✓ Reserved                  ├─ ✗ Failed                ├─ Consumes:
        │  (emit event)               │  (emit event)            │  • inventory-reserved
        │                              │                          │  • inventory-failed
        ▼                              ▼                          │  • payment-completed
   inventory-reserved          inventory-failed                   │  • payment-failed
        │                              │                          │  • shipping-dispatched
        │                              │                          │  • shipping-failed
        ▼                              ▼                          │  • delivery-confirmed
   CONFIRMED                  INVENTORY_PENDING                   │
   (Status Update)           (Status Update)                      └─ Updates Order Status
        │                              │                          └─ Publishes Next Event
        │                              └──────────┬────────────────┘
        │                                         │
        ▼                                         ▼
┌──────────────────┐                    ┌──────────────────┐
│ PAYMENT SERVICE  │                    │ NOTIFICATION     │
│ Process Payment  │                    │   SERVICE        │
└──────────────────┘                    │ Send Customer    │
        │                                │   Alerts         │
        ├─ ✓ Paid                       └──────────────────┘
        │  (emit: payment-completed)
        │
        ├─ ✗ Failed
        │  (emit: payment-failed)
        │
        ▼
   PAYMENT_COMPLETED / PAYMENT_FAILED
        │
        ▼
┌──────────────────┐
│ SHIPPING SERVICE │
│ Dispatch Order   │
└──────────────────┘
        │
        ├─ ✓ Dispatched
        │  (emit: shipping-dispatched)
        │
        ├─ ✗ Failed
        │  (emit: shipping-failed)
        │
        ▼
   SHIPPED / SHIPMENT_FAILED
        │
        ▼
┌──────────────────┐
│ DELIVERY SERVICE │
│ Confirm Delivery │
└──────────────────┘
        │
        ├─ ✓ Delivered
        │  (emit: delivery-confirmed)
        │
        ▼
     DELIVERED
        │
        ▼
    ┌──────────────────┐
    │ NOTIFICATION     │
    │   SERVICE        │
    │ Order Delivered  │
    └──────────────────┘
```

---

## Service Communication Matrix

```
┌─────────────┬──────────────────┬────────────────────────┬────────────────────┐
│   Source    │     Event        │    Kafka Topic         │    Consumer        │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Order Svc   │ OrderPlaceEvent  │ order-placed           │ Inventory Svc      │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Inventory   │ InventoryReserved│ inventory-reserved     │ Order Service      │
│ Svc         │ Event            │                        │ Listener           │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Inventory   │ InventoryFailed  │ inventory-failed       │ Order Service      │
│ Svc         │ Event            │                        │ Listener           │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Order Svc   │ PaymentCompleted │ payment-required       │ Payment Svc        │
│ Listener    │ Event            │                        │                    │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Payment     │ PaymentCompleted │ payment-completed      │ Order Service      │
│ Svc         │ Event (Success)  │                        │ Listener           │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Payment     │ PaymentCompleted │ payment-failed         │ Order Service      │
│ Svc         │ Event (Failed)   │                        │ Listener           │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Order Svc   │ ShippingDispatched│ shipping-required      │ Shipping Svc       │
│ Listener    │ Event            │                        │                    │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Shipping    │ ShippingDispatched│ shipping-dispatched    │ Order Service      │
│ Svc         │ Event (Success)  │                        │ Listener           │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Shipping    │ ShippingDispatched│ shipping-failed        │ Order Service      │
│ Svc         │ Event (Failed)   │                        │ Listener           │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Delivery    │ ShippingDispatched│ delivery-confirmed     │ Order Service      │
│ Svc         │ Event (Delivered)│                        │ Listener           │
├─────────────┼──────────────────┼────────────────────────┼────────────────────┤
│ Order Svc   │ NotificationEvent │ notification-events    │ Notification Svc   │
│ Listener    │                  │                        │                    │
└─────────────┴──────────────────┴────────────────────────┴────────────────────┘
```

---

## Order Status State Machine

```
                    ┌──────────┐
                    │ PENDING  │
                    └─────┬────┘
                          │
                 (Inventory Reserved)
                          │
                    ┌─────▼────────┐
                    │ CONFIRMED    │
                    └─────┬────────┘
                          │
                 (Payment Completed)
                          │
              ┌───────────▼──────────┐
              │ PAYMENT_COMPLETED    │
              └────────┬─────────────┘
                       │
            (Shipping Dispatched)
                       │
              ┌────────▼──────────┐
              │ SHIPPED           │
              └────────┬──────────┘
                       │
           (Delivery Confirmed)
                       │
              ┌────────▼──────────┐
              │ DELIVERED         │
              └───────────────────┘

        FAILURE PATHS (Error States):

    Inventory Failure    ──▶  INVENTORY_PENDING
    Payment Failure      ──▶  PAYMENT_FAILED
    Shipping Failure     ──▶  SHIPMENT_FAILED
    General Failure      ──▶  FAILED
    User Action          ──▶  CANCELLED
```

---

## Database Schema

### orders table

```
┌─────────────────────────────────────────────┐
│              Orders Table                   │
├──────────────┬──────────┬────────────────────┤
│ id (PK)      │ BIGINT   │ Primary Key        │
│ customer_id  │ VARCHAR  │ Foreign Key        │
│ total_price  │ DECIMAL  │ Order Total        │
│ status       │ ENUM     │ Current Status     │
│ created_at   │ DATETIME │ Creation Time      │
└──────────────┴──────────┴────────────────────┘

┌──────────────────────────────────────────────┐
│         Order Items Table (ElementCollection)│
├──────────────┬──────────┬─────────────────────┤
│ order_id (FK)│ BIGINT   │ References orders   │
│ sku_code     │ VARCHAR  │ Product SKU         │
│ quantity     │ INT      │ Quantity Ordered    │
└──────────────┴──────────┴─────────────────────┘
```

---

## Kafka Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all # Wait for all replicas to acknowledge
      retries: 3

    consumer:
      group-id: order-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

    listener:
      ack-mode: manual_immediate # Manual acknowledgment
      concurrency: 3 # Process 3 messages in parallel
```

---

## Sequence Diagram - Happy Path

```
Client    Order Service    Kafka    Inventory    Payment    Shipping    Notification
  │            │            │          │           │           │            │
  ├─POST───────▶            │          │           │           │            │
  │ /createOrder            │          │           │           │            │
  │            ├─Create─────▶(DB)     │           │           │            │
  │            │            │          │           │           │            │
  │◀──202──────┤            │          │           │           │            │
  │ ACCEPTED   │            │          │           │           │            │
  │            ├─Publish────▶order-placed         │           │            │
  │            │            ├─────────────────────▶          │           │
  │            │            │          ├─Process──▶          │           │
  │            │            │          │           │           │            │
  │            │            │          ├─emit─────▶inventory-reserved
  │            │◀────────────┤◀─────────┤           │           │            │
  │            ├─Update─────▶(DB)      │           │           │            │
  │            │ CONFIRMED  │          │           │           │            │
  │            │            │          │           │           │            │
  │            ├─Publish────▶payment-required      │           │            │
  │            │            ├────────────────────────────────▶ │            │
  │            │            │          │           ├─Process──▶│            │
  │            │            │          │           │           │            │
  │            │            │          │           ├─emit─────▶payment-completed
  │            │◀────────────┤◀─────────┤◀──────────┤           │            │
  │            ├─Update─────▶(DB)      │           │           │            │
  │            │ PAYMENT_   │          │           │           │            │
  │            │ COMPLETED  │          │           │           │            │
  │            │            │          │           │           │            │
  │            ├─Publish────▶shipping-required     │           │            │
  │            │            ├────────────────────────────────────────────▶ │
  │            │            │          │           │           ├─Process──▶│
  │            │            │          │           │           │           │
  │            │            │          │           │           ├─emit─────▶shipping-dispatched
  │            │◀────────────┤◀─────────┤◀──────────┤◀──────────┤           │
  │            ├─Update─────▶(DB)      │           │           │           │
  │            │ SHIPPED    │          │           │           │           │
  │            │            │          │           │           │           │
  │            ├─Publish────▶notification-events   │           │           │
  │            │            ├──────────────────────────────────────────────▶│
  │            │            │          │           │           │           ├─Send Email
  │            │            │          │           │           │           │
```

---

## Sequence Diagram - Failure Path (Inventory Fails)

```
Client    Order Service    Kafka    Inventory    Notification
  │            │            │          │            │
  ├─POST───────▶            │          │            │
  │ /createOrder            │          │            │
  │            ├─Create─────▶(DB)     │            │
  │            │            │          │            │
  │◀──202──────┤            │          │            │
  │ ACCEPTED   │            │          │            │
  │            ├─Publish────▶order-placed          │
  │            │            ├────────────────────▶  │
  │            │            │          ├─Fail──────▶
  │            │            │          │            │
  │            │            │          ├─emit──────▶inventory-failed
  │            │◀────────────┤◀─────────┤            │
  │            ├─Update─────▶(DB)      │            │
  │            │ INVENTORY_ │          │            │
  │            │ PENDING    │          │            │
  │            │            │          │            │
  │            ├─Publish────▶notification-events   │
  │            │            ├────────────────────────────▶
  │            │            │          │            ├─Send Email
  │            │            │          │            │ (Failure Notification)
  │            │            │          │            │
  │            [Order Stays in INVENTORY_PENDING]    │
  │            [Manual review needed]                │
```

---

## Error Handling Strategy

### Idempotency

- Use `orderNumber` as idempotency key
- Database uniqueness constraints prevent duplicate processing
- Consumers should be idempotent (safe to process same event twice)

### Dead Letter Queue (Future)

```
Failed Events ──▶ DLQ Topic ──▶ Manual Review ──▶ Retry/Skip
  (max retries    (Topic:        (Admin Panel)    (Decision)
   exceeded)      order-events-dlq)
```

### Retry Logic

- Kafka built-in retries with exponential backoff
- Consumer group maintains offset tracking
- Failed messages stay in topic for replay

---

## Monitoring & Metrics

### Key Metrics to Track

1. **Order Creation Rate** - Orders created per second
2. **Fulfillment Rate** - % of orders reaching DELIVERED
3. **Stage Duration** - Time spent at each status
4. **Failure Rate** - % of orders failing at each stage
5. **Consumer Lag** - Kafka consumer lag per topic
6. **Event Processing Latency** - Time from publish to consume

### Sample Prometheus Queries

```promql
rate(order_created_total[5m])                    # Order creation rate
rate(order_delivered_total[5m])                  # Delivery rate
histogram_quantile(0.95, order_duration_seconds) # 95% fulfillment time
kafka_consumer_lag{group="order-service"}       # Consumer lag
```
