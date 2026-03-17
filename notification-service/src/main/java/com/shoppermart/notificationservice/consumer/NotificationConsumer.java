package com.shoppermart.notificationservice.consumer;

import com.shoppermart.notificationservice.event.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEvent.class);

    @KafkaListener(topics = "notification-events",groupId = "notification-service")
    public void listenNotificationEvent(NotificationEvent event){

        logger.info("Inventory Reserved Event received for order: " + event.getOrderNumber());
        logger.info("Customer Email: " + event.getCustomerEmail());
        logger.info("Message: " + event.getMessage());
        logger.info("Notification Type: " + event.getNotificationType());
        logger.info("Created At: " + event.getCreatedAt());

        // TODO: Send email to customer about successful inventory reservation
        sendEmail(event.getCustomerEmail(), "Inventory Reserved",
                "Your order #" + event.getOrderNumber() + " inventory has been reserved. "+
                        "Event Message"+event.getMessage());
    }

    private void sendEmail(String email, String subject, String message) {
        // TODO: Implement actual email sending logic
        logger.info("Sending email to: " + email);
        logger.info("Subject: " + subject);
        logger.info("Message: " + message);

    }
}
