# Implementation Guide & Best Practices

## Project Structure

```
order-service/
├── src/main/java/com/shoppermart/orderservice/
│   ├── Fulfillment/
│   │   ├── workflow/
│   │   │   └── OrderFulfillmentWorkflow.java      ✓ Implemented
│   │   ├── kafkaConsumer/
│   │   │   └── OrderEventsListener.java           ✓ Implemented
│   │   ├── KafkaProducer/
│   │   │   └── OrderProducer.java                 ✓ Already Present
│   │   ├── event/
│   │   │   ├── OrderPlaceEvent.java               ✓ Already Present
│   │   │   ├── InventoryReservedEvent.java        ✓ Updated
│   │   │   ├── PaymentCompletedEvent.java         ✓ Implemented
│   │   │   ├── ShippingDispatchedEvent.java       ✓ Implemented
│   │   │   └── NotificationEvent.java             ✓ Implemented
│   │   └── DTO/
│   │       ├── CreatedOrderEvent.java
│   │       ├── PaymentOrderEvent.java
│   │       └── ShippingOrderEvent.java
│   ├── Model/
│   │   ├── Order.java                             ✓ Already Present
│   │   └── OrderStatus.java                       ✓ Updated
│   └── Service/
│       └── OrderService.java
├── pom.xml
├── application.yml                                 ✓ See Configuration
├── WORKFLOW_DOCUMENTATION.md                       ✓ Implemented
└── ARCHITECTURE_DIAGRAM.md                         ✓ Implemented
```

---

## Kafka Configuration (Required in application.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        linger.ms: 10 # Batch messages for 10ms
        batch.size: 16384 # Batch size in bytes

    consumer:
      group-id: order-service
      auto-offset-reset: earliest
      max-poll-records: 100 # Max records per poll
      session-timeout-ms: 30000 # Session timeout
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

    listener:
      ack-mode: manual_immediate # Manual offset management
      concurrency: 5 # Process 5 messages in parallel
      poll-timeout: 3000 # Poll timeout in ms

logging:
  level:
    com.shoppermart.orderservice: DEBUG
    org.apache.kafka: INFO
```

---

## Key Implementation Details

### 1. OrderFulfillmentWorkflow (REST Endpoint)

**Entry Point:** `POST /v1/createOrder/`

**Key Points:**

- ✓ Creates order with PENDING status
- ✓ Publishes to Kafka immediately (fire and forget)
- ✓ Returns 202 ACCEPTED (async acknowledgment)
- ✓ Error handling with proper HTTP status codes
- ✓ Comprehensive logging

**Bug Fixes Applied:**

```java
// BEFORE (WRONG):
order.setSkuCodeQuantityMap(order.getSkuCodeQuantityMap());  // Null reference!

// AFTER (CORRECT):
order.setSkuCodeQuantityMap(createdOrderEvent.getSkuCodeQuantityMap());
```

### 2. OrderEventsListener (Kafka Consumer Orchestrator)

**Key Points:**

- ✓ Listens on 7 different Kafka topics
- ✓ Updates order status at each stage
- ✓ Publishes next event in workflow
- ✓ Sends notifications at appropriate times
- ✓ Handles both success and failure scenarios
- ✓ Exception handling with fallback behavior
- ✓ Uses manual acknowledgment mode for reliability

**Event Handler Pattern:**

```java
@KafkaListener(topics = "inventory-reserved", groupId = "order-service")
public void handleInventoryReserved(InventoryReservedEvent event) {
    logger.info("Event received for order: {}", event.getOrderNumber());

    try {
        // 1. Update order status
        orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.CONFIRMED);

        // 2. Publish next event
        kafkaTemplate.send("next-topic", nextEvent);

    } catch (Exception e) {
        logger.error("Failed to process event for order: {}", event.getOrderNumber(), e);
    }
}
```

### 3. Event Classes

All event classes follow this pattern:

```java
public class EventName {
    private Long orderNumber;
    private String customerEmail;
    private String status;           // SUCCESS / FAILED
    private String failureReason;   // Optional

    // Constructors (with and without failure info)
    // Getters/Setters
    // toString() for logging
}
```

**Why this structure:**

- Flexible for both success and failure paths
- Easy to deserialize from Kafka JSON
- Clear logging via toString()
- Type-safe with compile-time checking

---

## Testing Guide

### 1. Unit Tests for OrderFulfillmentWorkflow

```java
@SpringBootTest
public class OrderFulfillmentWorkflowTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderProducer orderProducer;

    @Autowired
    private OrderFulfillmentWorkflow workflow;

    @Test
    public void testCreateOrderSuccess() {
        // Arrange
        CreatedOrderEvent event = new CreatedOrderEvent(
            "customer-123",
            Map.of("SKU-001", 2),
            BigDecimal.valueOf(99.99)
        );

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        when(orderService.createOrder(any())).thenReturn(mockOrder);

        // Act
        ResponseEntity<CreatedOrderEvent> response = workflow.createOrder(event);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(orderProducer).sendOrderToInventory(mockOrder);
    }
}
```

### 2. Integration Tests with Kafka

```java
@SpringBootTest
public class OrderEventsListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderService orderService;

    @Test
    public void testInventoryReservedEvent() throws Exception {
        // Arrange
        InventoryReservedEvent event = new InventoryReservedEvent(
            1L, "customer@email.com", BigDecimal.valueOf(99.99)
        );

        // Act
        kafkaTemplate.send("inventory-reserved", event).get();

        // Assert (with timeout for async processing)
        Thread.sleep(2000);
        Order updatedOrder = orderService.getOrder(1L);
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
    }
}
```

### 3. Manual Testing with Kafka CLI

```bash
# Start Kafka locally
docker-compose -f docker-compose-infra.yml up

# Monitor topics in real-time
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic inventory-reserved \
  --from-beginning

# Send test event
echo '{"orderNumber":1,"customerEmail":"test@email.com","totalPrice":99.99}' | \
kafka-console-producer.sh \
  --broker-list localhost:9092 \
  --topic inventory-reserved
```

### 4. End-to-End Test Scenario

```bash
# 1. Create order
curl -X POST http://localhost:8080/v1/createOrder \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust-123",
    "totalPrice": 199.99,
    "skuCodeQuantityMap": {"SKU-LAPTOP": 1, "SKU-MOUSE": 2}
  }'

# Response: 202 ACCEPTED with orderId=1

# 2. Monitor order status
for i in {1..10}; do
  curl http://localhost:8080/v1/orders/1 | jq '.status'
  sleep 2
done

# Expected progression:
# PENDING → CONFIRMED → PAYMENT_COMPLETED → SHIPPED → DELIVERED
```

---

## Common Issues & Troubleshooting

### Issue: "Failed to send message to Kafka"

**Causes:**

- Kafka broker not running
- Invalid bootstrap servers in config
- Network connectivity issue

**Solution:**

```bash
# Check Kafka is running
docker-compose -f docker-compose-infra.yml ps

# Check connectivity
nc -zv localhost 9092

# Check logs
docker-compose -f docker-compose-infra.yml logs kafka
```

### Issue: "Consumer group not receiving messages"

**Causes:**

- Wrong topic name in @KafkaListener
- Consumer group offset is beyond published messages
- Message deserialization failing silently

**Solution:**

```java
// Check topic name matches exactly
@KafkaListener(topics = "inventory-reserved", groupId = "order-service")

// Reset consumer offset to beginning
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group order-service \
  --topic inventory-reserved \
  --reset-offsets --to-earliest --execute

// Check deserializer config
spring.json.trusted.packages: "*"
```

### Issue: "Null Pointer Exception in event handler"

**Causes:**

- Event fields not serialized from JSON
- Missing no-arg constructor in event class
- Mismatched field names

**Solution:**

```java
// Ensure all event classes have:
public class MyEvent {
    private Long id;

    // 1. No-arg constructor (required for JSON deserialization)
    public MyEvent() {}

    // 2. All-args constructor (for convenience)
    public MyEvent(Long id) { this.id = id; }

    // 3. Getters/Setters (required for JSON serialization)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
```

### Issue: "Messages being processed multiple times"

**Causes:**

- Multiple consumer instances without proper load balancing
- Manual acknowledgment not being called
- Offset not committed

**Solution:**

```yaml
# Use manual acknowledgment with proper handling
spring:
  kafka:
    listener:
      ack-mode: manual_immediate # Or MANUAL
```

```java
// Ensure no exceptions thrown (exceptions trigger reprocessing)
try {
    // Process message
    orderService.updateOrderStatus(...);
} catch (Exception e) {
    logger.error("Failed to process", e);
    // Don't rethrow - keep processing
}
```

---

## Performance Optimization Tips

### 1. Batch Processing

```yaml
spring:
  kafka:
    consumer:
      max-poll-records: 500 # Process in batches
      fetch-min-bytes: 1024 # Wait for more data
```

### 2. Concurrency

```yaml
spring:
  kafka:
    listener:
      concurrency: 10 # Process 10 messages in parallel
```

### 3. Database Optimization

```java
// Use batch updates for multiple orders
@Transactional
public void updateOrdersStatus(List<Long> orderIds, OrderStatus status) {
    orderIds.stream()
        .forEach(id -> updateOrderStatus(id, status));
    // Spring will batch these if database supports it
}
```

### 4. Kafka Batching

```yaml
spring:
  kafka:
    producer:
      batch-size: 32768 # 32KB batches
      linger-ms: 10 # Wait 10ms for more messages
      buffer-memory: 67108864 # 64MB buffer
```

---

## Deployment Checklist

- [ ] All event classes have no-arg constructors
- [ ] Kafka broker is running and accessible
- [ ] All topics are created (or auto-created)
- [ ] Consumer group configured correctly
- [ ] OrderService.updateOrderStatus() works correctly
- [ ] Logging is configured (DEBUG for troubleshooting)
- [ ] Error handling in all event handlers
- [ ] Manual acknowledgment configured
- [ ] Idempotency keys set (orderNumber)
- [ ] Load testing completed
- [ ] Monitoring/alerting configured
- [ ] Documentation updated
- [ ] Team trained on workflow

---

## Monitoring Checklist

### Logs to Monitor

```
INFO  OrderFulfillmentWorkflow: Order created successfully with ID: 1
INFO  OrderEventsListener: Inventory Reserved Event received for order: 1
ERROR OrderEventsListener: Failed to update order status for order: 1
WARN  OrderProducer: Message delivery took longer than expected
```

### Kafka Metrics to Alert On

- Consumer lag > 1000 messages
- Message processing latency > 5 seconds
- Broker availability < 99.9%
- Failed delivery rate > 0.1%

### Database Metrics

- Order table row count growing unexpectedly
- Update query latency > 100ms
- Connection pool exhaustion

---

## Future Enhancements

### Short Term

1. Add retry logic with exponential backoff
2. Implement dead letter queue (DLQ)
3. Add distributed tracing (Spring Cloud Sleuth)
4. Implement order cancellation workflow

### Medium Term

1. Saga pattern for distributed transactions
2. Event sourcing for complete audit trail
3. CQRS for query optimization
4. Circuit breaker for downstream services

### Long Term

1. ML-based anomaly detection
2. Predictive order routing
3. Multi-region deployment
4. Event replay and time-travel debugging
