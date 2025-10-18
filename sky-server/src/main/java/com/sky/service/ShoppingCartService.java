package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/*
 * @Author lhj
 * @Create 2025/10/18 14:51
 * Description:
 * @Version 1.0
 */
public interface ShoppingCartService {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);


    List<ShoppingCart> showShoppingCart();
}
