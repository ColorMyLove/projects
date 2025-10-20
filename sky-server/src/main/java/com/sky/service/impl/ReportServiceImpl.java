package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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
        return UserReportVO.builder()
                .dateList(joined)
                .totalUserList(total)
                .newUserList(newUser)
                .build();
    }
}
