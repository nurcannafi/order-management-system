package com.example.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockNotAvailableEvent {

    private Long orderId;
    private String productName;
    private int quantity;
}
