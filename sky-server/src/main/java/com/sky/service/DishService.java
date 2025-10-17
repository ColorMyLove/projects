package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

/*
 * @Author lhj
 * @Create 2025/10/15 21:10
 * Description:
 * @Version 1.0
 */
public interface DishService {
    void saveWithFlavor(DishDTO dto);

    PageResult pageQuery(DishPageQueryDTO queryDTO);

    void deleteBatch(List<Long> ids);

    DishVO getByidWithFlavor(Long id);

    /**
     * 根据 id 修改菜品基本信息和对应的口味信息
     *
     * @param dto
     */
    void updateWithFlavor(DishDTO dto);


    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    List<Dish> selectByCategoryId(Long categoryId);

    void startOrStop(Integer status, Long id);
}
