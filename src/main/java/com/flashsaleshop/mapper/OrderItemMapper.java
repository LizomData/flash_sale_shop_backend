package com.flashsaleshop.mapper;

import com.flashsaleshop.model.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Insert("INSERT INTO order_item(order_id, item_key, product_id, name, item_type, event_id, price, qty) " +
            "VALUES(#{orderId}, #{item.key}, #{item.productId}, #{item.name}, #{item.type}, #{item.eventId}, #{item.price}, #{item.qty})")
    int insert(@Param("orderId") String orderId, @Param("item") OrderItem item);

    @Select("SELECT item_key AS `key`, product_id, event_id, name, item_type AS type, price, qty FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(String orderId);

    @Select("SELECT COALESCE(SUM(oi.qty), 0) FROM order_item oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.user_id = #{userId} AND oi.event_id = #{eventId} AND oi.item_type = 'seckill'")
    int sumPurchasedByUserEvent(@Param("userId") Long userId, @Param("eventId") String eventId);
}
