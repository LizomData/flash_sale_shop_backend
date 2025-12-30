package com.flashsaleshop.mapper;

import com.flashsaleshop.model.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT id, name, description, tag, price, stock FROM product ORDER BY id")
    List<Product> findAll();

    @Select("SELECT id, name, description, tag, price, stock FROM product WHERE id = #{id}")
    Product findById(Long id);

    @Insert("INSERT INTO product(name, description, tag, price, stock) VALUES(#{name}, #{description}, #{tag}, #{price}, #{stock})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET stock = stock - #{qty} WHERE id = #{id} AND stock >= #{qty}")
    int decrementStock(@Param("id") Long id, @Param("qty") int qty);
}
