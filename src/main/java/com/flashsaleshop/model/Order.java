package com.flashsaleshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String createdAt;
    private BigDecimal total;
    private List<OrderItem> items = new ArrayList<>();
}

