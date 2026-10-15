package com.quanshiguang.shiguang.moderation.model.vo;

import lombok.Data;

/**
 * @date: 2026-10-12
 * @description: 审核概览响应参数
 **/
@Data
public class ModerationOverviewRspVO {

    /** 审核总量 */
    private Long total;

    /** 通过数量 */
    private Long passCount;

    /** 待人工复审数量 */
    private Long reviewCount;

    /** 拒绝数量 */
    private Long rejectCount;

    /** 涉及内容的作者去重数（cardinality 聚合） */
    private Long authorCount;
}
