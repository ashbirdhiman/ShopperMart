package com.shoppermart.inventoryservice.KafkaProducer;

import com.shoppermart.inventoryservice.events.InventoryReservedEvent;
import com.shoppermart.inventoryservice.events.OrderPlaceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryProducer {

    private final KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate;

    private static final Logger logger= LoggerFactory.getLogger(InventoryProducer.class);

    public InventoryProducer( KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    //Send Success Events

    public boolean sendSuccessToNextService(OrderPlaceEvent orderPlaceEvent){
        boolean success=true;

        try {
            InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent(
                    orderPlaceEvent.getOrderNumber(),
                    orderPlaceEvent.getCustomerEmail(),
                    orderPlaceEvent.getTotalPrice()
            );

            kafkaTemplate.send("inventory-reserved", inventoryReservedEvent);
            logger.info("Inventory reserved event sent for order: " + orderPlaceEvent.getOrderNumber());
        }
        catch (Exception e){
            success=false;
            throw new RuntimeException("Sending to Next Service Failed "+orderPlaceEvent.getOrderNumber());
        }

        return success;
    }


    //Send Failed Events

    public boolean sendFailedToNextService(OrderPlaceEvent orderPlaceEvent){
        boolean success=true;

        try {
            InventoryReservedEvent inventoryFailedEvent = new InventoryReservedEvent(
                    orderPlaceEvent.getOrderNumber(),
                    orderPlaceEvent.getCustomerEmail(),
                    orderPlaceEvent.getTotalPrice()
            );
            kafkaTemplate.send("inventory-failed", inventoryFailedEvent);
            logger.error("Inventory reservation failed for order: " + orderPlaceEvent.getOrderNumber() + ", sending to inventory-failed topic");
        }
        catch (Exception e){
            success=false;
            throw new RuntimeException("Sending Failed event to Next Service Failed "+orderPlaceEvent.getOrderNumber());
        }

        return success;
    }
}
