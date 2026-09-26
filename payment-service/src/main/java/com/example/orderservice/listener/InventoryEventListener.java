package com.example.orderservice.listener;

import com.example.orderservice.event.PaymentFailedEvent;
import com.example.orderservice.event.PaymentProcessedEvent;
import com.example.orderservice.event.StockNotAvailableEvent;
import com.example.orderservice.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "inventory-events", groupId = "payment-service-group")
public class InventoryEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    @KafkaHandler
    public void handleStockReserved(StockReservedEvent event) {
        boolean success = random.nextInt(100) < 80;

        if (success) {
            kafkaTemplate.send("payment-events", new PaymentProcessedEvent(event.getOrderId()));
            System.out.println("💳 Payment processed for Order #" + event.getOrderId());
        } else {
            kafkaTemplate.send("payment-events",
                    new PaymentFailedEvent(event.getOrderId(), event.getProductName(), event.getQuantity()));
            System.out.println("⚠️ Payment failed for Order #" + event.getOrderId());
        }
    }

    @KafkaHandler
    public void handleStockNotAvailable(StockNotAvailableEvent event) {
        System.out.println("ℹ️ Skipping payment — stock was not available for Order #" + event.getOrderId());
    }
}
