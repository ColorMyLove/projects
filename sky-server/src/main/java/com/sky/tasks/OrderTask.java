package com.sky.tasks;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @Author lhj
 * @Create 2025/10/20 13:57
 * Description:
 * @Version 1.0
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟触发一次
    public void processTimeoutOrder() {
        log.info("定时处理支付超时订单：{}", LocalDateTime.now());
        /*
        select * from orders where order_status = 1 and check_out_time < ?
         */
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        // 查找超时订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        // 更新订单
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时, 自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理处于待派送状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")    // 每天凌晨一点触发
    public void processDeliveryOrder() {
        log.info("处理一直处于派送状态的订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
