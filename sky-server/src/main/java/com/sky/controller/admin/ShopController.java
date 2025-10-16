package com.sky.controller.admin;

/*
 * @Author lhj
 * @Create 2025/10/16 12:37
 * Description:
 * @Version 1.0
 */

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = {"店铺相关接口"})
@Slf4j
public class ShopController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY = "SHOP_STATUS";

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable("status") Integer status) {
        log.info("设置店铺的营业状态: {}", status);
        redisTemplate.opsForValue().set(KEY, status.toString());
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus() {
        String status = (String) redisTemplate.opsForValue().get(KEY);
        return Result.success(Integer.parseInt(status));
    }
}
