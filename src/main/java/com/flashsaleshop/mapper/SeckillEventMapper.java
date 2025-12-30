package com.flashsaleshop.mapper;

import com.flashsaleshop.model.SeckillEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SeckillEventMapper {

    @Select("SELECT id, title, product_id, seckill_price, limit_per_user, stock, initial_stock, sold, start_at, end_at FROM seckill_event ORDER BY start_at")
    List<SeckillEvent> findAll();

    @Select("SELECT id, title, product_id, seckill_price, limit_per_user, stock, initial_stock, sold, start_at, end_at FROM seckill_event WHERE id = #{id}")
    SeckillEvent findById(String id);

    @Select("SELECT id, title, product_id, seckill_price, limit_per_user, stock, initial_stock, sold, start_at, end_at FROM seckill_event WHERE id = #{id} FOR UPDATE")
    SeckillEvent findByIdForUpdate(String id);

    @Insert("INSERT INTO seckill_event(id, title, product_id, seckill_price, limit_per_user, stock, initial_stock, sold, start_at, end_at) " +
            "VALUES(#{id}, #{title}, #{productId}, #{seckillPrice}, #{limitPerUser}, #{stock}, #{initialStock}, #{sold}, #{startAt}, #{endAt})")
    int insert(SeckillEvent event);

    @Update("UPDATE seckill_event SET stock = stock - #{qty}, sold = sold + #{qty} WHERE id = #{id} AND stock >= #{qty}")
    int decrementStock(@Param("id") String id, @Param("qty") int qty);
}
