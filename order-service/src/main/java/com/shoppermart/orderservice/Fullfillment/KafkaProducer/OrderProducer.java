package com.shoppermart.orderservice.Fullfillment.KafkaProducer;

import com.shoppermart.orderservice.Model.Order;
import com.shoppermart.orderservice.Model.OrderEvent;
import com.shoppermart.orderservice.Service.OrderService;
import com.shoppermart.orderservice.Fullfillment.event.OrderPlaceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderProducer {
    private static final Logger log= LoggerFactory.getLogger(OrderService.class);


    private final KafkaTemplate<String, OrderPlaceEvent> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, OrderPlaceEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderToInventory(Order createdOrder) throws RuntimeException{

        log.info("Sending Order to Inventory Service with order id: "+createdOrder.getId() +"to Kafka topic :" +"order-placed" );

        try {
            OrderPlaceEvent orderPlaceEvent=new OrderPlaceEvent(createdOrder.getId(),"abc@gmail.com",createdOrder.getSkuCodeQuantityMap(), OrderEvent.ORDER_CREATED,createdOrder.getTotalPrice());
            kafkaTemplate.send("order-placed",orderPlaceEvent);
        }
        catch (Exception e){
            throw new RuntimeException("Sending to kafka Failed ");
        }

    }

    public void sendUpdateOrderToInventory(Order createdOrder,Map<String, Integer> oldSkuCodeQuantityMap) throws RuntimeException{

        log.info("Sending Update Order to Inventory Service with order id: "+createdOrder.getId() +"to Kafka topic :" +"order-placed" );

        try {
            OrderPlaceEvent orderPlaceEvent=new OrderPlaceEvent(createdOrder.getId(),"abc@gmail.com", createdOrder.getSkuCodeQuantityMap(),oldSkuCodeQuantityMap, OrderEvent.ORDER_UPDATED,createdOrder.getTotalPrice());
            kafkaTemplate.send("order-placed",orderPlaceEvent);
        }
        catch (Exception e){
            throw new RuntimeException("Sending to kafka Failed ");
        }

    }
    

}
