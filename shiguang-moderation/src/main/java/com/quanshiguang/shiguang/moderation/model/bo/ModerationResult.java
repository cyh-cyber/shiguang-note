package com.quanshiguang.shiguang.moderation.model.bo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @date: 2026-10-12
 * @description: 审核结论（写入 ES 的文档结构）
 **/
@Data
@Builder
public class ModerationResult {

    private String eventId;
    private String contentType;
    private Long contentId;
    private Long authorId;
    private String content;
    private Integer version;

    /** 命中的敏感词 */
    private List<String> hitWords;

    /** 命中的规则编码，如 TOO_LONG / EXTERNAL_LINK / REPEAT_CHAR / SENSITIVE_WORD */
    private List<String> ruleCodes;

    /** 风险分 */
    private Integer riskScore;

    /** 判定结论，见 ModerationDecisionEnum */
    private String decision;

    private LocalDateTime createTime;
}
