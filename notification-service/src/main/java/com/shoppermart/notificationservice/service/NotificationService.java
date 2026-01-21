package com.shoppermart.notificationservice.service;

import com.shoppermart.notificationservice.order.OrderPlaceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(topics ="order-placed")
    public void Listen(OrderPlaceEvent orderPlaceEvent){

        logger.info("Message received from order-placed kafka topic {}",orderPlaceEvent);

    }
}
