package com.flashsaleshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillEvent {
    private String id;
    private String title;
    private Long productId;
    private BigDecimal seckillPrice;
    private int limitPerUser;
    private int stock;
    private int initialStock;
    private int sold;
    private long startAt;
    private long endAt;
}

