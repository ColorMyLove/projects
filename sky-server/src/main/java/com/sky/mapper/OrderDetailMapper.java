package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
 * @Author lhj
 * @Create 2025/10/18 17:23
 * Description:
 * @Version 1.0
 */
@Mapper
public interface OrderDetailMapper {

    void insertBatch(List<OrderDetail> orderDetails);
}
