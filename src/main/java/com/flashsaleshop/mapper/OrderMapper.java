package com.flashsaleshop.mapper;

import com.flashsaleshop.model.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders(id, user_id, created_at, total) VALUES(#{order.id}, #{userId}, #{order.createdAt}, #{order.total})")
    int insert(@Param("order") Order order, @Param("userId") Long userId);

    @Select("SELECT id, created_at, total FROM orders WHERE user_id = #{userId} ORDER BY id DESC")
    List<Order> findByUserId(Long userId);
}
