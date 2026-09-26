package com.example.orderservice.listener;

import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.repository.ProductRepository;
import com.example.orderservice.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = {"order-events", "payment-events"}, groupId = "inventory-service-group")
public class OrderEventListener {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        Optional<Product> productOpt = productRepository.findById(event.getProductName());

        if (productOpt.isPresent() && productOpt.get().getStockQuantity() >= event.getQuantity()) {
            Product product = productOpt.get();
            product.setStockQuantity(product.getStockQuantity() - event.getQuantity());
            productRepository.save(product);

            kafkaTemplate.send("inventory-events",
                    new StockReservedEvent(event.getOrderId(), event.getProductName(), event.getQuantity()));
            System.out.println("✅ Stock reserved for Order #" + event.getOrderId());
        } else {
            kafkaTemplate.send("inventory-events",
                    new StockNotAvailableEvent(event.getOrderId(), event.getProductName(), event.getQuantity()));
            System.out.println("❌ Stock not available for Order #" + event.getOrderId());
        }
    }

    @KafkaHandler
    public void handlePaymentFailed(PaymentFailedEvent event) {
        productRepository.findById(event.getProductName()).ifPresent(product -> {
            product.setStockQuantity(product.getStockQuantity() + event.getQuantity());
            productRepository.save(product);
            System.out.println("↩️ Stock released for Order #" + event.getOrderId() +
                    " — " + event.getQuantity() + "x " + event.getProductName());
        });
    }

    @KafkaHandler
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
    }
}
