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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.DefaultUserDestinationResolver;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dto) {
        log.info("新增菜品: {}", dto);
        dishService.saveWithFlavor(dto);
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
        return Result.success();
    }
}
