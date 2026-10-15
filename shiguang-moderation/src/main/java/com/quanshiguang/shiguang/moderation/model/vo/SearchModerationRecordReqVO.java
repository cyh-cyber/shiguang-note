package com.quanshiguang.shiguang.moderation.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @date: 2026-10-12
 * @description: 审核流水检索请求参数
 **/
@Data
public class SearchModerationRecordReqVO {

    /** 关键词（对 content 做全文检索，命中词高亮） */
    private String keyword;

    /** 判定结果：PASS / REVIEW / REJECT */
    private String decision;

    /** 内容类型：NOTE / COMMENT / USER_PROFILE */
    private String contentType;

    /** 最小风险分（含） */
    private Integer minRiskScore;

    /** 开始时间，格式 yyyy-MM-dd HH:mm:ss */
    private String startTime;

    /** 结束时间，格式 yyyy-MM-dd HH:mm:ss */
    private String endTime;

    /** 页码，从 1 开始 */
    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1;
}
