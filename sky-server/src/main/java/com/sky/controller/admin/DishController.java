package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.user.DefaultUserDestinationResolver;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/*
 * @Author lhj
 * @Create 2025/10/15 21:02
 * Description:
 * @Version 1.0
 */
@Api("菜品管理")
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    // 注入 RedisTemplate, 用于操作Redis数据库
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dto) {
        log.info("新增菜品: {}", dto);
        dishService.saveWithFlavor(dto);
        // 清理缓存数据
        String key = "dish_" + dto.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("查询菜品")
    public Result<PageResult> page(DishPageQueryDTO queryDTO) {
        PageResult query = dishService.pageQuery(queryDTO);
        return Result.success(query);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除:{ }", ids);
        dishService.deleteBatch(ids);
        // 先查找所有以 dish 开头的 key
        Set keys = redisTemplate.keys("dish_*");
        // 删除所有相关的 redis 缓存
        redisTemplate.delete(keys);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据 Id 查询菜品")
    public Result<DishVO> getById(@PathVariable("id") Long id) {
        log.info("根据 id 查询菜品: {}", id);
        DishVO dishVO = dishService.getByidWithFlavor(id);
        return Result.success(dishVO);
    }

    @ApiOperation("修改菜品信息")
    @PutMapping
    public Result update(@RequestBody DishDTO dto) {
        log.info("修改菜品: {}", dto);
        dishService.updateWithFlavor(dto);
        // 先查找所有以 dish 开头的 key
        Set keys = redisTemplate.keys("dish_*");
        // 删除所有相关的 redis 缓存
        redisTemplate.delete(keys);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类Id 查询分类")
    public Result<List<Dish>> selectByCategoryId(@RequestParam("categoryId") Long categoryId) {
        log.info("分类Id: {}", categoryId);
        List<Dish> dishes = dishService.selectByCategoryId(categoryId);
        return Result.success(dishes);
    }

    @PutMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        return Result.success();
    }

    public void cleanCache(String pattern) {
        // 查找所有匹配的 key
        Set keys = redisTemplate.keys(pattern);
        // 删除匹配的 key
        redisTemplate.delete(keys);
    }
}
