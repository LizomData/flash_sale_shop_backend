package com.flashsaleshop.mapper;

import com.flashsaleshop.model.UserAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT id, name, phone, password, role, balance FROM user_account WHERE phone = #{phone}")
    UserAccount findByPhone(String phone);

    @Select("SELECT id, name, phone, password, role, balance FROM user_account WHERE id = #{id}")
    UserAccount findById(Long id);

    @Insert("INSERT INTO user_account(name, phone, password, role, balance) VALUES(#{name}, #{phone}, #{password}, #{role}, #{balance})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAccount user);
}
