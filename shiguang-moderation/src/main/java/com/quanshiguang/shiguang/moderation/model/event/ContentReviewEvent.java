package com.quanshiguang.shiguang.moderation.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @date: 2026-10-12
 * @description: 流经 Kafka 的审核事件
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReviewEvent implements Serializable {

    /** 事件唯一 ID（建议用分布式 ID 服务生成）—— 消费幂等的关键，同时作为 ES 的 docId */
    private String eventId;

    /** 内容类型：NOTE / COMMENT / USER_PROFILE，见 ContentTypeEnum */
    private String contentType;

    /** 内容 ID（笔记 ID / 评论 ID） */
    private Long contentId;

    /** 作者 ID */
    private Long authorId;

    /** 待审文本（标题 + 正文 + 话题拼接，或评论正文） */
    private String content;

    /** 内容版本号：同一条内容多次编辑时用来区分 */
    private Integer version;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;
}
