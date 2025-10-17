package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/*
 * @Author lhj
 * @Create 2025/10/17 16:10
 * Description:
 * @Version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据 open_id 查询用户
     *
     * @param openId
     * @return
     */
    @Select("select * from user where openid = #{openId}")
    User getByOpenId(String openId);

    void insert(User user);
}
