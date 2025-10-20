package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
 * @Author lhj
 * @Create 2025/10/18 17:23
 * Description:
 * @Version 1.0
 */
@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    @Update("update orders set status = #{orderStatus},pay_status=#{ordersPaidStatus},checkout_time = #{checkOutTime} where id=#{orderId}")
    void updateStatus(Integer orderStatus, Integer ordersPaidStatus, LocalDateTime checkOutTime, Long orderId);

    @Select("select * from orders where status = #{pendingPayment} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer pendingPayment, LocalDateTime time);

    @Select("select * from orders where id = #{id}")
    Orders getById();

    /**
     * 根据条件查询某一时间段订单总金额
     *
     * @param map
     * @return
     */
    Double sumByMap(Map map);
}
