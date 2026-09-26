package com.example.orderservice.listener;

import com.example.orderservice.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("📩 Notification: Order #" + event.getOrderId() +
                " has been received — " + event.getQuantity() + " X " + event.getProductName());
    }
}
