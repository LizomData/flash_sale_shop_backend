package com.flashsaleshop.dto;

import com.flashsaleshop.model.CartItem;
import com.flashsaleshop.model.Order;
import com.flashsaleshop.model.Product;
import com.flashsaleshop.model.SeckillEvent;
import com.flashsaleshop.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SnapshotResponse {
    private UserProfile user;
    private List<Product> products;
    private List<SeckillEvent> seckillEvents;
    private List<CartItem> cart;
    private List<Order> orders;
    private Map<String, Integer> seckillPurchases;
    private String message;
}

