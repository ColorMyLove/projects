package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/*
 * @Author lhj
 * @Create 2025/10/18 14:56
 * Description:
 * @Version 1.0
 */
@Mapper
public interface ShoppingCartMapper {
    /**
     * 动态条件查询
     *
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name,  user_id, dish_id, setmeal_id, dish_flavor, amount, image,create_time) " +
            "values (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    List<ShoppingCart> select(ShoppingCart shoppingCart);

    void delete(ShoppingCart cart);

    @Delete("delete from shopping_cart where user_id = #{id}")
    void deletByUserId(Long id);
}
