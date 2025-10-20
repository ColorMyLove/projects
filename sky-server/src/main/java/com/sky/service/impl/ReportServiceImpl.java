package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * @Author lhj
 * @Create 2025/10/20 16:57
 * Description:
 * @Version 1.0
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public TurnoverReportVO getTurnOverStatistics(LocalDate begin, LocalDate end) {
        // 创建返回日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String joined = StringUtils.join(dateList, ",");

//        Map<LocalDateTime, Integer> map = new HashMap<>();
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            if (turnover != null) {
                turnoverList.add(turnover);
            } else {
                turnoverList.add(0.0);
            }
        }

        // 创建视图对象并返回
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(joined);
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 创建返回日期列表
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        Integer sum = 0;
        for (LocalDate dl : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(dl, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(dl, LocalTime.MAX);
            Integer t = orderMapper.sumByCreateDay(beginTime, endTime);
            sum += t;
            newUserList.add(t);
            totalUserList.add(sum);
        }

        String joined = StringUtils.join(dateList, ",");
        String newUser = StringUtils.join(newUserList, ",");
        String total = StringUtils.join(totalUserList, ",");
        // 返回从数据库中获取的数据
        return UserReportVO.builder().dateList(joined).totalUserList(total).newUserList(newUser).build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 创建时间列表
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate dl : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(dl, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(dl, LocalTime.MAX);
            Integer total = orderMapper.sumByCreateDay(beginTime, endTime);
            Integer valid = orderMapper.sumByCreateDayWithStatus(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(total);
            validOrderCountList.add(valid);
        }
        String dataListString = StringUtils.join(dateList, ",");
        String orderCountListString = StringUtils.join(orderCountList, ",");
        String validOrderCountListString = StringUtils.join(validOrderCountList, ",");

        Integer orderSum = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderSum = validOrderCountList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = 0.0;
        if (orderSum != 0) orderCompletionRate = validOrderSum.doubleValue() / orderSum.doubleValue();
        return OrderReportVO.builder().dateList(dataListString).orderCountList(orderCountListString).validOrderCountList(validOrderCountListString).totalOrderCount(orderSum).validOrderCount(validOrderSum).orderCompletionRate(orderCompletionRate).build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        // 计算最早和最晚的时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> topXVOList = orderMapper.getTopN(beginTime, endTime, Orders.COMPLETED);
        List<String> names = topXVOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = topXVOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String joined_name = StringUtils.join(names, ",");
        String joined_number = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO.builder().nameList(joined_name).numberList(joined_number).build();
    }
}
