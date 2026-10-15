package com.quanshiguang.shiguang.moderation.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @date: 2026-10-12
 * @description: 审核流水检索响应参数
 **/
@Data
public class SearchModerationRecordRspVO {

    private String eventId;
    private Long contentId;
    private String contentType;
    private Long authorId;

    /** 正文（包含高亮标签片段，便于前端直接渲染） */
    private String content;

    /** 命中的敏感词 */
    private List<String> hitWords;

    /** 命中的规则编码 */
    private List<String> ruleCodes;

    private String decision;
    private Integer riskScore;

    /** 创建时间，格式 yyyy-MM-dd HH:mm:ss */
    private String createTime;
}
