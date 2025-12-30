package com.flashsaleshop.mapper;

import com.flashsaleshop.model.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper {

    @Select("SELECT item_key AS `key`, product_id, name, item_type AS type, event_id, price, qty " +
            "FROM cart_item WHERE user_id = #{userId} ORDER BY id")
    List<CartItem> findByUserId(Long userId);

    @Select("SELECT item_key AS `key`, product_id, name, item_type AS type, event_id, price, qty " +
            "FROM cart_item WHERE user_id = #{userId} AND item_key = #{key} LIMIT 1")
    CartItem findByUserAndKey(@Param("userId") Long userId, @Param("key") String key);

    @Insert("INSERT INTO cart_item(user_id, item_key, product_id, name, item_type, event_id, price, qty) " +
            "VALUES(#{userId}, #{item.key}, #{item.productId}, #{item.name}, #{item.type}, #{item.eventId}, #{item.price}, #{item.qty})")
    int insert(@Param("userId") Long userId, @Param("item") CartItem item);

    @Update("UPDATE cart_item SET qty = qty + #{qty} WHERE user_id = #{userId} AND item_key = #{key}")
    int incrementQty(@Param("userId") Long userId, @Param("key") String key, @Param("qty") int qty);

    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int clearByUser(Long userId);

    @Select("SELECT COALESCE(SUM(qty), 0) FROM cart_item WHERE user_id = #{userId} AND event_id = #{eventId} AND item_type = 'seckill'")
    int sumSeckillQty(@Param("userId") Long userId, @Param("eventId") String eventId);
}
