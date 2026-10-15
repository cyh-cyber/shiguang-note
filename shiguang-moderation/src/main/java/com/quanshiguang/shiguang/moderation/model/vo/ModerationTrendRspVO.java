package com.quanshiguang.shiguang.moderation.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @date: 2026-10-12
 * @description: 审核趋势（按天）响应参数
 **/
@Data
public class ModerationTrendRspVO {

    /** 日期，格式 yyyy-MM-dd */
    private String date;

    /** 当天审核总量 */
    private Long total;

    /** 当天各判定结果分布 */
    private List<DecisionStat> decisions;

    /**
     * 单个判定结果的统计项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DecisionStat {
        /** 判定结果：PASS / REVIEW / REJECT */
        private String decision;
        /** 数量 */
        private Long count;
    }
}
