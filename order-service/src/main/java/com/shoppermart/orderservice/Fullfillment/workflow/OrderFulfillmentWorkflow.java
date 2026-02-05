package com.shoppermart.orderservice.Fullfillment.workflow;

import com.shoppermart.orderservice.Fullfillment.DTO.CreatedOrderEvent;
import com.shoppermart.orderservice.Fullfillment.KafkaProducer.OrderProducer;
import com.shoppermart.orderservice.Model.Order;
import com.shoppermart.orderservice.Model.OrderStatus;
import com.shoppermart.orderservice.Service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OrderFulfillmentWorkflow
 * Handles the asynchronous event-driven order fulfillment workflow.
 * 
 * Workflow: Order Creation -> Inventory -> Payment -> Shipping -> Notification
 */
@RestController
@RequestMapping("/v1/createOrder/")
public class OrderFulfillmentWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(OrderFulfillmentWorkflow.class);

    private final OrderService orderService;
    private final OrderProducer orderProducer;

    public OrderFulfillmentWorkflow(OrderService orderService, OrderProducer orderProducer) {
        this.orderService = orderService;
        this.orderProducer = orderProducer;
    }

    /**
     * Creates an order and initiates the async fulfillment workflow
     * Step 1: Create order in DB with PENDING status
     * Step 2: Publish to inventory service via Kafka
     * 
     * @param createdOrderEvent Event containing order details
     * @return Response with created order event
     */
    @PostMapping
    public ResponseEntity<CreatedOrderEvent> createOrder(@RequestBody CreatedOrderEvent createdOrderEvent) {
        try {
            logger.info("Creating order for customer: {}", createdOrderEvent.getCustomerId());

            // Step 1: Create order entity and persist to database
            Order order = new Order();
            order.setCustomerId(createdOrderEvent.getCustomerId());
            order.setStatus(OrderStatus.PENDING);
            order.setTotalPrice(createdOrderEvent.getTotalPrice());
            order.setSkuCodeQuantityMap(createdOrderEvent.getSkuCodeQuantityMap());
            
            Order createdOrder = orderService.createOrder(order);
            logger.info("Order created successfully with ID: {}", createdOrder.getId());

            // Step 2: Send to inventory service asynchronously
            orderProducer.sendOrderToInventory(createdOrder);
            logger.info("Order sent to inventory service for SKU reservation");

            createdOrderEvent.setOrderId(createdOrder.getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdOrderEvent);

        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
