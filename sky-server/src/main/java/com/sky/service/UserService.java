package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

/*
 * @Author lhj
 * @Create 2025/10/17 15:29
 * Description:
 * @Version 1.0
 */
public interface UserService {

    /**
     * 微信用户登录功能
     *
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

}
