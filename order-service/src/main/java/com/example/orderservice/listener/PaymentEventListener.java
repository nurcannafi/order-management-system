package com.example.orderservice.listener;

import com.example.orderservice.event.*;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = {"payment-events", "inventory-events"}, groupId = "order-service-group")
public class PaymentEventListener {

    private final OrderService orderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaHandler
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        orderService.updateOrderStatus(event.getOrderId(), "CONFIRMED");
    }

    @KafkaHandler
    public void handlePaymentFailed(PaymentFailedEvent event) {
        orderService.updateOrderStatus(event.getOrderId(), "CANCELLED");

        kafkaTemplate.send("inventory-events",
                new StockReleaseRequestedEvent(event.getOrderId(), event.getProductName(), event.getQuantity()));
        System.out.println("🔄 Requesting stock release for Order #" + event.getOrderId());
    }

    @KafkaHandler
    public void handleStockNotAvailable(StockNotAvailableEvent event) {
        orderService.updateOrderStatus(event.getOrderId(), "CANCELLED");
    }

    @KafkaHandler
    public void handleStockReserved(StockReservedEvent event) {
    }

    @KafkaHandler
    public void handleStockReleaseRequested(StockReleaseRequestedEvent event) {
    }
}