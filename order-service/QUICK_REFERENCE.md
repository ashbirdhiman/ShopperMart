# Quick Reference Card - Order Fulfillment Workflow

## 🚀 Quick Start

### 1. Start Kafka

```bash
docker-compose -f docker-compose-infra.yml up -d
```

### 2. Start Order Service

```bash
cd order-service && mvn spring-boot:run
```

### 3. Create Order

```bash
curl -X POST http://localhost:8080/v1/createOrder \
  -H "Content-Type: application/json" \
  -d '{"customerId":"c1","totalPrice":99.99,"skuCodeQuantityMap":{"SKU-001":2}}'
```

Response: `202 ACCEPTED` with `orderId: 1`

---

## 📊 Workflow State Machine

```
PENDING ──[Inventory OK]──> CONFIRMED
  │                             │
  └──[Inventory FAIL]──────────┘
                                 │
                     [Payment OK]│
                                 ▼
                         PAYMENT_COMPLETED
                                 │
                     [Shipping OK]│
                                 ▼
                              SHIPPED
                                 │
                  [Delivery OK]   │
                                 ▼
                            DELIVERED

FAILURE STATES:
├─ INVENTORY_PENDING (inventory failed)
├─ PAYMENT_FAILED (payment failed)
├─ SHIPMENT_FAILED (shipping failed)
├─ FAILED (general error)
└─ CANCELLED (user action)
```

---

## 🎯 Kafka Topics Map

### Publishing FROM Order Service

```
✓ order-placed (→ Inventory Service)
✓ payment-required (→ Payment Service)
✓ shipping-required (→ Shipping Service)
✓ notification-events (→ Notification Service)
```

### Consuming BY Order Service

```
✓ inventory-reserved (← Inventory Service)
✓ inventory-failed (← Inventory Service)
✓ payment-completed (← Payment Service)
✓ payment-failed (← Payment Service)
✓ shipping-dispatched (← Shipping Service)
✓ shipping-failed (← Shipping Service)
✓ delivery-confirmed (← Shipping Service)
```

---

## 🔧 Key Classes

| Class                      | Location         | Responsibility                   |
| -------------------------- | ---------------- | -------------------------------- |
| `OrderFulfillmentWorkflow` | `workflow/`      | REST endpoint, order creation    |
| `OrderEventsListener`      | `kafkaConsumer/` | Event consumption, orchestration |
| `OrderProducer`            | `KafkaProducer/` | Event publishing                 |
| `OrderPlaceEvent`          | `event/`         | Initial order event              |
| `InventoryReservedEvent`   | `event/`         | Inventory response               |
| `PaymentCompletedEvent`    | `event/`         | Payment response                 |
| `ShippingDispatchedEvent`  | `event/`         | Shipping response                |
| `NotificationEvent`        | `event/`         | Customer notification            |

---

## 📝 API Endpoints

### Create Order

```
POST /v1/createOrder/
Content-Type: application/json

Request:
{
  "customerId": "string",
  "totalPrice": 99.99,
  "userEmail": "email@example.com",
  "skuCodeQuantityMap": {
    "SKU-001": 2,
    "SKU-002": 1
  }
}

Response: 202 ACCEPTED
{
  "orderId": 1,
  "customerId": "string",
  "status": "PENDING"
}
```

### Get Order Status

```
GET /v1/orders/{orderId}

Response: 200 OK
{
  "id": 1,
  "customerId": "string",
  "status": "CONFIRMED",
  "totalPrice": 99.99,
  "createdAt": "2024-01-29T10:30:00"
}
```

---

## 🔍 Monitoring Commands

### Watch Kafka Topics

```bash
# Monitor all events
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic inventory-reserved --from-beginning

# Check consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group order-service --describe

# List all topics
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Check Logs

```bash
# Tail order service logs
docker logs -f order-service

# Find order in logs
grep -i "order: 1" /var/log/order-service.log

# Check for errors
grep -i "error\|exception" /var/log/order-service.log
```

---

## 🧪 Test Scenarios

### Happy Path (Complete Flow)

```bash
# 1. Create order
curl -X POST http://localhost:8080/v1/createOrder \
  -H "Content-Type: application/json" \
  -d '{"customerId":"c1","totalPrice":99.99,"skuCodeQuantityMap":{"SKU":1}}'

# 2. Watch status progression
for i in {1..12}; do
  echo "Check #$i:"
  curl -s http://localhost:8080/v1/orders/1 | jq '.status'
  sleep 5
done

# Expected: PENDING → CONFIRMED → PAYMENT_COMPLETED → SHIPPED → DELIVERED
```

### Inventory Failure Scenario

```bash
# 1. Create order with out-of-stock item
curl -X POST http://localhost:8080/v1/createOrder \
  -H "Content-Type: application/json" \
  -d '{"customerId":"c1","totalPrice":99.99,"skuCodeQuantityMap":{"OUT-OF-STOCK":99}}'

# 2. Inventory service publishes inventory-failed
# 3. Order status should be INVENTORY_PENDING
curl http://localhost:8080/v1/orders/1 | jq '.status'

# Expected: INVENTORY_PENDING
```

---

## 💾 Database Queries

### Check Order Status

```sql
SELECT id, customer_id, status, created_at FROM orders WHERE id = 1;
```

### Check Order Items

```sql
SELECT * FROM order_items WHERE order_id = 1;
```

### Find Orders by Status

```sql
SELECT id, customer_id, status, created_at
FROM orders
WHERE status = 'CONFIRMED'
ORDER BY created_at DESC
LIMIT 10;
```

### Order Statistics

```sql
SELECT
  status,
  COUNT(*) as count,
  AVG(total_price) as avg_price
FROM orders
GROUP BY status;
```

---

## ⚠️ Common Issues

| Issue                                   | Solution                                                        |
| --------------------------------------- | --------------------------------------------------------------- |
| 202 ACCEPTED but order stuck in PENDING | Kafka not running, check: `docker-compose ps`                   |
| "Failed to send to Kafka"               | Bootstrap server config wrong, check logs                       |
| Event not processed                     | Check consumer group lag: `kafka-consumer-groups.sh --describe` |
| Null pointer in event                   | Missing no-arg constructor in event class                       |
| Order status not updating               | Check if `orderService.updateOrderStatus()` is working          |
| Duplicate processing                    | Use manual ack mode: `ack-mode: manual_immediate`               |

---

## 📋 Configuration Checklist

```yaml
# application.yml required settings
spring:
  kafka:
    bootstrap-servers: localhost:9092 # ✓ Check this
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: order-service
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*" # ✓ Required
    listener:
      ack-mode: manual_immediate # ✓ Recommended
      concurrency: 5 # ✓ For parallel processing

  datasource:
    url: jdbc:mysql://localhost:3306/order_db # ✓ Database connection

logging:
  level:
    com.shoppermart.orderservice: DEBUG # ✓ For debugging
```

---

## 🎓 Understanding the Flow

### Step 1: Create Order

```java
Client → OrderFulfillmentWorkflow.createOrder()
  ├─ Creates Order(PENDING)
  ├─ Saves to DB
  └─ Publishes: orderNumber=1 → topic: order-placed
```

### Step 2: Inventory Processing

```
Kafka topic: order-placed
  └─ Inventory Service consumes
      ├─ Reserves SKUs
      └─ Publishes: inventory-reserved (success)
         OR inventory-failed (failure)
```

### Step 3: Listener Processes Response

```
Kafka topic: inventory-reserved
  └─ OrderEventsListener.handleInventoryReserved()
      ├─ Updates order → CONFIRMED
      ├─ Publishes: payment-required → Payment Service
      └─ Continues workflow...
```

### Step 4-7: Repeat for Payment → Shipping → Delivery

---

## 🔐 Best Practices

✓ **Always use try-catch** in event handlers
✓ **Log with context** - include orderId, email
✓ **Return 202 ACCEPTED** - don't block clients
✓ **Use manual ACK** - ensure message reliability
✓ **Implement idempotency** - safe to retry events
✓ **Handle failures gracefully** - update status, notify user
✓ **Monitor Kafka lag** - detect processing delays
✓ **Test end-to-end** - simulate real scenarios

---

## 📞 Support

### Documentation Files

- `WORKFLOW_DOCUMENTATION.md` - Complete workflow guide
- `ARCHITECTURE_DIAGRAM.md` - Visual diagrams
- `IMPLEMENTATION_GUIDE.md` - Technical details
- `IMPLEMENTATION_SUMMARY.md` - Full overview

### Logs Location

```
Order Service: stdout or configured log file
Kafka: docker logs kafka
MySQL: docker logs mysql
```

### Health Check

```bash
# Check if order service is running
curl http://localhost:8080/actuator/health

# Check if Kafka is accessible
nc -zv localhost 9092
```

---

**Keep this card handy for quick reference!**
