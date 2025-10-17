package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.jaxrs.FastJsonAutoDiscoverable;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * @Author lhj
 * @Create 2025/10/17 15:46
 * Description:
 * @Version 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    // 微信服务接口地址
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 调用微信接口服务, 返回用户 open_id
     *
     * @param jsCode
     * @return
     */
    private String getOpenId(String jsCode) {
        Map<String, String> map = new HashMap<>();
        // 必须要传输的参数
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", jsCode);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        JSONObject jsonObject = JSON.parseObject(json);
        String openId = jsonObject.getString("open_id");
        return openId;
    }

    /**
     * 微信用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
//        GET https://api.weixin.qq.com/sns/jscode2session
        // 调用微信接口服务, 获取当前用户的 openId
        String openId = getOpenId(userLoginDTO.getCode());
        // 判断 openId 是否为空, 如果为空表示登录失败, 抛出业务异常
        if (Objects.equals(openId, "")) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 判断是否为新用户
        User user = userMapper.getByOpenId(openId);
        // 如果是新用户, 自动完成注册
        if (user == null) {
            user = User.builder().openid(openId).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        // 返回结果
        return user;
    }
}
