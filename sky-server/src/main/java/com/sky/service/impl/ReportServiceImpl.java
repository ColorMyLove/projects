package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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

    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 1.  查询数据库, 获取营业额
        LocalDate time1 = LocalDate.now().plusDays(-30);
        LocalDate time2 = LocalDate.now().plusDays(-1);
        LocalDateTime begin = LocalDateTime.of(time1, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(time2, LocalTime.MAX);
        // 获得营业概览数据
        BusinessDataVO vo = workspaceService.getBusinessData(begin, end);
        XSSFWorkbook excel = null;
        try {
            // 基于模板文件创建一个新的 Excel 文件
            excel = new XSSFWorkbook(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("templates/运营数据报表模板.xlsx")));
            XSSFSheet sheet = excel.getSheetAt(0);
            // 填充概览数据
            sheet.getRow(1).getCell(1).setCellValue("时间:" + time1 + "至" + time2);
            sheet.getRow(3).getCell(2).setCellValue(vo.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(vo.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(vo.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(vo.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(vo.getUnitPrice());


            // 获取明细数据, 循环写入到 .xlsx 文件中
            for (int i = 0; i < 30; i++) {
                LocalDate date = time1.plusDays(i);
                BusinessDataVO dataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                sheet.getRow(7 + i).getCell(1).setCellValue(date.toString());
                sheet.getRow(7 + i).getCell(2).setCellValue(dataVO.getTurnover());
                sheet.getRow(7 + i).getCell(3).setCellValue(dataVO.getValidOrderCount());
                sheet.getRow(7 + i).getCell(4).setCellValue(dataVO.getOrderCompletionRate());
                sheet.getRow(7 + i).getCell(5).setCellValue(dataVO.getUnitPrice());
                sheet.getRow(7 + i).getCell(6).setCellValue(dataVO.getNewUsers());
            }
            ServletOutputStream os = response.getOutputStream();
            excel.write(os);

            os.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
