package com.flashsaleshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String key;
    private Long productId;
    private String name;
    private String type; // normal 或 seckill
    private String eventId;
    private BigDecimal price;
    private int qty;
}

