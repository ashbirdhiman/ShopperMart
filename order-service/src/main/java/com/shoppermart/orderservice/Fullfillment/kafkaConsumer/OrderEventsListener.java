package com.shoppermart.orderservice.Fullfillment.kafkaConsumer;

import com.shoppermart.orderservice.Model.OrderStatus;
import com.shoppermart.orderservice.Service.OrderService;
import com.shoppermart.orderservice.Fullfillment.event.InventoryReservedEvent;
import com.shoppermart.orderservice.Fullfillment.event.PaymentCompletedEvent;
import com.shoppermart.orderservice.Fullfillment.event.ShippingDispatchedEvent;
import com.shoppermart.orderservice.Fullfillment.event.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * OrderEventsListener
 * Consumes events from downstream services and orchestrates the order workflow.
 * 
 * Workflow:
 * 1. Inventory Reserved -> Update status to CONFIRMED -> Send to Payment
 * 2. Inventory Failed -> Update status to INVENTORY_PENDING -> Keep order for retry
 * 3. Payment Completed -> Update status to PAYMENT_COMPLETED -> Send to Shipping
 * 4. Payment Failed -> Update status to PAYMENT_FAILED -> Trigger compensation
 * 5. Shipping Dispatched -> Update status to SHIPPED -> Send to Notification
 * 6. Shipping Failed -> Update status to SHIPMENT_FAILED -> Trigger compensation
 */
@Service
public class OrderEventsListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventsListener.class);

    private final OrderService orderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventsListener(OrderService orderService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Handles inventory reservation failure
     * Order stays in INVENTORY_PENDING status for potential retry or manual intervention
     */
    @KafkaListener(topics = "inventory-failed", groupId = "order-service")
    public void handleInventoryFailed(InventoryReservedEvent event) {
        logger.error("Inventory Failed Event received for order: {}", event.getOrderNumber());
        logger.error("Failure reason: {} | Customer Email: {}", event.getFailureReason(), event.getCustomerEmail());

        try {
            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.INVENTORY_PENDING);
            logger.info("Order status updated to INVENTORY_PENDING for order: {}", event.getOrderNumber());
            
            // Optionally send notification about inventory failure
            NotificationEvent notification = new NotificationEvent(
                    event.getOrderNumber(),
                    event.getCustomerEmail(),
                    "Inventory Reservation Failed",
                    "We couldn't reserve the items for your order. Please contact support."
            );
            kafkaTemplate.send("notification-events", notification);
        } catch (Exception e) {
            logger.error("Failed to handle inventory failure for order: {}", event.getOrderNumber(), e);
        }
    }

    /**
     * Handles successful inventory reservation
     * Order moves to CONFIRMED status and is sent to payment service
     */
    @KafkaListener(topics = "inventory-reserved", groupId = "order-service")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        logger.info("Inventory Reserved Event received for order: {}", event.getOrderNumber());
        logger.info("Customer Email: {}", event.getCustomerEmail());

        try {
            // Update order status to CONFIRMED
            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.CONFIRMED);
            logger.info("Order status updated to CONFIRMED for order: {}", event.getOrderNumber());

            /**
             * TODO; Remove Notification Service after implementing payment microservice
             */


            NotificationEvent notification = new NotificationEvent(
                    event.getOrderNumber(),
                    event.getCustomerEmail(),
                    "Inventory Reserved and Order Fulfilled Failed",
                    "Order Created Successfully."
            );
            kafkaTemplate.send("notification-events", notification);
            logger.info("Order and Inventory reserved for " +event.getOrderNumber());

//            // Send to payment service for processing
//            PaymentCompletedEvent paymentEvent = new PaymentCompletedEvent(
//                    event.getOrderNumber(),
//                    event.getCustomerEmail(),
//                    event.getTotalPrice()
//            );
//
//            kafkaTemplate.send("payment-required", paymentEvent);
//            logger.info("Order sent to payment service for processing");

        } catch (Exception e) {
            logger.error("Failed to process inventory reservation for order: {}", event.getOrderNumber(), e);
        }
    }

    /**
     * TODO; Under Development
     */



//    /**
//     * Handles successful payment completion
//     * Order moves to PAYMENT_COMPLETED status and is sent to shipping service
//     */
//    @KafkaListener(topics = "payment-completed", groupId = "order-service")
//    public void handlePaymentCompleted(PaymentCompletedEvent event) {
//        logger.info("Payment Completed Event received for order: {}", event.getOrderNumber());
//        logger.info("Payment amount: {} | Customer Email: {}", event.getAmount(), event.getCustomerEmail());
//
//        try {
//            // Update order status
//            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.PAYMENT_COMPLETED);
//            logger.info("Order status updated to PAYMENT_COMPLETED for order: {}", event.getOrderNumber());
//
//            // Send to shipping service
//            ShippingDispatchedEvent shippingEvent = new ShippingDispatchedEvent(
//                    event.getOrderNumber(),
//                    event.getCustomerEmail()
//            );
//            kafkaTemplate.send("shipping-required", shippingEvent);
//            logger.info("Order sent to shipping service for dispatch");
//
//        } catch (Exception e) {
//            logger.error("Failed to process payment completion for order: {}", event.getOrderNumber(), e);
//        }
//    }
//
//    /**
//     * Handles payment failure
//     * Order moves to PAYMENT_FAILED status - triggers compensation workflow
//     */
//    @KafkaListener(topics = "payment-failed", groupId = "order-service")
//    public void handlePaymentFailed(PaymentCompletedEvent event) {
//        logger.error("Payment Failed Event received for order: {}", event.getOrderNumber());
//        logger.error("Customer Email: {}", event.getCustomerEmail());
//
//        try {
//            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.PAYMENT_FAILED);
//            logger.info("Order status updated to PAYMENT_FAILED for order: {}", event.getOrderNumber());
//
//            // Trigger compensation - release reserved inventory
//            NotificationEvent notification = new NotificationEvent(
//                    event.getOrderNumber(),
//                    event.getCustomerEmail(),
//                    "Payment Failed",
//                    "Payment processing failed. Reserved items will be released. Please try again."
//            );
//            kafkaTemplate.send("notification-events", notification);
//
//        } catch (Exception e) {
//            logger.error("Failed to handle payment failure for order: {}", event.getOrderNumber(), e);
//        }
//    }
//
//    /**
//     * Handles successful shipment dispatch
//     * Order moves to SHIPPED status and notification is sent
//     */
//    @KafkaListener(topics = "shipping-dispatched", groupId = "order-service")
//    public void handleShippingDispatched(ShippingDispatchedEvent event) {
//        logger.info("Shipping Dispatched Event received for order: {}", event.getOrderNumber());
//        logger.info("Tracking number: {} | Customer Email: {}", event.getTrackingNumber(), event.getCustomerEmail());
//
//        try {
//            // Update order status
//            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.SHIPPED);
//            logger.info("Order status updated to SHIPPED for order: {}", event.getOrderNumber());
//
//            // Send notification to customer
//            NotificationEvent notification = new NotificationEvent(
//                    event.getOrderNumber(),
//                    event.getCustomerEmail(),
//                    "Order Shipped",
//                    "Your order has been dispatched! Tracking: " + event.getTrackingNumber()
//            );
//            kafkaTemplate.send("notification-events", notification);
//
//        } catch (Exception e) {
//            logger.error("Failed to process shipping dispatch for order: {}", event.getOrderNumber(), e);
//        }
//    }
//
//    /**
//     * Handles shipment failure
//     * Order moves to SHIPMENT_FAILED status - triggers compensation workflow
//     */
//    @KafkaListener(topics = "shipping-failed", groupId = "order-service")
//    public void handleShippingFailed(ShippingDispatchedEvent event) {
//        logger.error("Shipping Failed Event received for order: {}", event.getOrderNumber());
//        logger.error("Failure reason: {} | Customer Email: {}", event.getFailureReason(), event.getCustomerEmail());
//
//        try {
//            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.SHIPMENT_FAILED);
//            logger.info("Order status updated to SHIPMENT_FAILED for order: {}", event.getOrderNumber());
//
//            // Notify customer and support team
//            NotificationEvent notification = new NotificationEvent(
//                    event.getOrderNumber(),
//                    event.getCustomerEmail(),
//                    "Shipping Failed",
//                    "We encountered an issue shipping your order. Our team will contact you shortly."
//            );
//            kafkaTemplate.send("notification-events", notification);
//
//        } catch (Exception e) {
//            logger.error("Failed to handle shipping failure for order: {}", event.getOrderNumber(), e);
//        }
//    }
//
//    /**
//     * Handles delivery confirmation
//     * Order moves to DELIVERED status
//     */
//    @KafkaListener(topics = "delivery-confirmed", groupId = "order-service")
//    public void handleDeliveryConfirmed(ShippingDispatchedEvent event) {
//        logger.info("Delivery Confirmed Event received for order: {}", event.getOrderNumber());
//
//        try {
//            orderService.updateOrderStatus(event.getOrderNumber(), OrderStatus.DELIVERED);
//            logger.info("Order status updated to DELIVERED for order: {}", event.getOrderNumber());
//
//            // Send delivery confirmation notification
//            NotificationEvent notification = new NotificationEvent(
//                    event.getOrderNumber(),
//                    event.getCustomerEmail(),
//                    "Order Delivered",
//                    "Your order has been successfully delivered!"
//            );
//            kafkaTemplate.send("notification-events", notification);
//
//        } catch (Exception e) {
//            logger.error("Failed to process delivery confirmation for order: {}", event.getOrderNumber(), e);
//        }
//    }
}
