package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

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
}
