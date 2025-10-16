package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @Author lhj
 * @Create 2025/10/15 21:24
 * Description:
 * @Version 1.0
 */
@Mapper
public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);


    /**
     * 根据菜品数据删除口味数据
     *
     * @param dishId
     */
    @Select("delete from dish_flavor where dish_id = #{dishId};")
    void deleteByDishId(Long dishId);

    void deleteByDishIds(List<Long> ids);

    /**
     * 根据菜品数据查询口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
