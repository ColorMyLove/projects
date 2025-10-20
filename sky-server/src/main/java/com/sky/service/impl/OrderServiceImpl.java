package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.models.auth.In;
import io.swagger.util.Json;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/*
 * @Author lhj
 * @Create 2025/10/18 17:20
 * Description:
 * @Version 1.0
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    public static Long orderId;

    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 判断配送地址是否存在
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            // 抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 判断用户购物车是否为空
        Long id = BaseContext.getCurrentId();
        ShoppingCart cart = ShoppingCart.builder().id(id).build();
        List<ShoppingCart> select = shoppingCartMapper.select(cart);
        if (select == null || select.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 创建订单类并拷贝属性
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(id);
        orderMapper.insert(orders);

        List<OrderDetail> orderDetails = new ArrayList<>();
        // 向订单明细表插入 n 条数据
        for (ShoppingCart shoppingCart : select) {
            // 订单明细
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            // 设置当前订单明细关联的订单 Id
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        // 批量插入数据
        orderDetailMapper.insertBatch(orderDetails);

        // 删除购物车
        shoppingCartMapper.deletByUserId(id);

        // 创建返回对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(id).orderTime(orders.getOrderTime()).orderAmount(orders.getAmount()).orderNumber(orders.getNumber()).build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrdersPaidStatus = Orders.PAID;
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;

        LocalDateTime check_out_time = LocalDateTime.now();
        orderMapper.updateStatus(OrderStatus, OrdersPaidStatus, check_out_time, orderId);

        // 通过 Web Socket 向客户端推送数据
        Map map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", orderId);
        map.put("content", "订单号：" + orderId);

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder().id(ordersDB.getId()).status(Orders.TO_BE_CONFIRMED).payStatus(Orders.PAID).checkoutTime(LocalDateTime.now()).build();

        orderMapper.update(orders);
    }
}
