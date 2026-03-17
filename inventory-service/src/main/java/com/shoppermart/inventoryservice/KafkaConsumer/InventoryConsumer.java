package com.shoppermart.inventoryservice.KafkaConsumer;

import com.shoppermart.inventoryservice.Controller.InventoryController;
import com.shoppermart.inventoryservice.GlobalException.ResourceNotFoundException;
import com.shoppermart.inventoryservice.Service.InventoryService;

import com.shoppermart.inventoryservice.events.OrderPlaceEvent;
import com.shoppermart.inventoryservice.events.InventoryReservedEvent;
import com.shoppermart.inventoryservice.Model.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Service
public class InventoryConsumer {


    private final InventoryService inventoryService;
    private KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate;

    private static final Logger logger= LoggerFactory.getLogger(InventoryConsumer.class);

    public InventoryConsumer(InventoryService inventoryService, KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-placed",groupId = "order-service")
    public void OrderPlacedEvent(OrderPlaceEvent orderPlaceEvent){
        logger.info("Order event received from order: "+orderPlaceEvent.getOrderNumber());
        logger.info("Event Type: " + orderPlaceEvent.getOrderEvent());

        try {
            if (orderPlaceEvent.getOrderEvent() == OrderEvent.ORDER_CREATED) {
                handleOrderCreated(orderPlaceEvent);
            } else if (orderPlaceEvent.getOrderEvent() == OrderEvent.ORDER_UPDATED) {
                handleOrderUpdated(orderPlaceEvent);
            } else if (orderPlaceEvent.getOrderEvent() == OrderEvent.ORDER_DELETED) {
                handleOrderDeleted(orderPlaceEvent);
            } else {
                logger.warn("Unknown event type: " + orderPlaceEvent.getOrderEvent());
            }
        } catch (Exception e) {
            // Send failure event to inventory-failed topic
            InventoryReservedEvent inventoryFailedEvent = new InventoryReservedEvent(
                orderPlaceEvent.getOrderNumber(),
                orderPlaceEvent.getCustomerEmail(),
                orderPlaceEvent.getTotalPrice()
            );
            kafkaTemplate.send("inventory-failed", inventoryFailedEvent);
            logger.error("Inventory operation failed for order: " + orderPlaceEvent.getOrderNumber() + ", sending to inventory-failed topic", e);
        }
    }

    private void handleOrderCreated(OrderPlaceEvent orderPlaceEvent) throws ResourceNotFoundException {
        logger.info("Processing ORDER_CREATED for order: " + orderPlaceEvent.getOrderNumber());

        Map<String, Integer> skuCodeQuantityMap = orderPlaceEvent.getSkuCodeQuantityMap();

        for (Map.Entry<String, Integer> entry : skuCodeQuantityMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            try {
                inventoryService.decrementQuantity(key, value);
                logger.info("Successfully decremented inventory for SKU: " + key + ", Quantity: " + value);
            } catch (Exception e) {
                logger.error("Failed to decrement inventory for SKU: " + key + ", Quantity: " + value, e);
                throw new ResourceNotFoundException("Failed to Decrement Quantity");
            }
        }

        // If all decrements successful, send to inventory-reserved topic
        InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent(
            orderPlaceEvent.getOrderNumber(),
            orderPlaceEvent.getCustomerEmail(),
            orderPlaceEvent.getTotalPrice()
        );
        kafkaTemplate.send("inventory-reserved", inventoryReservedEvent);
        logger.info("Inventory reserved event sent for order: " + orderPlaceEvent.getOrderNumber());
    }

    private void handleOrderUpdated(OrderPlaceEvent orderPlaceEvent) throws ResourceNotFoundException {
        logger.info("Processing ORDER_UPDATED for order: " + orderPlaceEvent.getOrderNumber());

        Map<String, Integer> newQuantityMap = orderPlaceEvent.getSkuCodeQuantityMap();
        Map<String, Integer> previousQuantityMap = orderPlaceEvent.getPreviousQuantityMap();

        // Calculate quantity differences and adjust inventory
        for (Map.Entry<String, Integer> entry : newQuantityMap.entrySet()) {
            String sku = entry.getKey();
            Integer newQuantity = entry.getValue();
            Integer oldQuantity = previousQuantityMap != null ? previousQuantityMap.getOrDefault(sku, 0) : 0;
            int difference = newQuantity - oldQuantity;

            try {
                if (difference > 0) {
                    // Increment inventory (customer ordered more)
                    inventoryService.incrementQuantity(sku, difference);
                    logger.info("Successfully incremented inventory for SKU: " + sku + ", Difference: " + difference);
                } else if (difference < 0) {
                    // Decrement inventory (customer ordered less)
                    inventoryService.decrementQuantity(sku, Math.abs(difference));
                    logger.info("Successfully decremented inventory for SKU: " + sku + ", Difference: " + Math.abs(difference));
                } else {
                    logger.info("No quantity change for SKU: " + sku);
                }
            } catch (Exception e) {
                logger.error("Failed to update inventory for SKU: " + sku + ", Difference: " + difference, e);
                throw new ResourceNotFoundException("Failed to Update Inventory");
            }
        }

        // Send success event
        InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent(
            orderPlaceEvent.getOrderNumber(),
            orderPlaceEvent.getCustomerEmail(),
            orderPlaceEvent.getTotalPrice()
        );
        kafkaTemplate.send("inventory-reserved", inventoryReservedEvent);
        logger.info("Inventory update event sent for order: " + orderPlaceEvent.getOrderNumber());
    }

    private void handleOrderDeleted(OrderPlaceEvent orderPlaceEvent) throws ResourceNotFoundException {
        logger.info("Processing ORDER_DELETED for order: " + orderPlaceEvent.getOrderNumber());

        Map<String, Integer> skuCodeQuantityMap = orderPlaceEvent.getSkuCodeQuantityMap();

        // Increment inventory back (reverse the decrement from ORDER_CREATED)
        for (Map.Entry<String, Integer> entry : skuCodeQuantityMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            try {
                inventoryService.incrementQuantity(key, value);
                logger.info("Successfully incremented inventory for SKU: " + key + ", Quantity: " + value + " (Order deleted, returning reserved stock)");
            } catch (Exception e) {
                logger.error("Failed to increment inventory for SKU: " + key + ", Quantity: " + value, e);
                throw new ResourceNotFoundException("Failed to Increment Quantity");
            }
        }

        // Send success event
        InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent(
            orderPlaceEvent.getOrderNumber(),
            orderPlaceEvent.getCustomerEmail(),
            orderPlaceEvent.getTotalPrice()
        );
        kafkaTemplate.send("inventory-reserved", inventoryReservedEvent);
        logger.info("Inventory deletion event sent for order: " + orderPlaceEvent.getOrderNumber());
    }


}
