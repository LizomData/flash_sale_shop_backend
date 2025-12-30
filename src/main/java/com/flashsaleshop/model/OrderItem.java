package com.flashsaleshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String key;
    private Long productId;
    private String eventId;
    private String name;
    private String type;
    private BigDecimal price;
    private int qty;
}
