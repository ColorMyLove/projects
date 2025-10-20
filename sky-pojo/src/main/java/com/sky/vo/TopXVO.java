package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author lhj
 * @Create 2025/10/20 19:41
 * Description:
 * @Version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TopXVO {
    private String name;
    private Integer number;
}
