package com.quanshiguang.shiguang.moderation.constant;

/**
 * @date: 2026-10-12
 * @description: Kafka Topic 常量
 **/
public class KafkaTopics {

    /**
     * 审核事件主 Topic（3 分区）
     * 分区键 = contentId，保证同一条内容的多条事件落同一分区、按序处理
     */
    public static final String CONTENT_REVIEW = "shiguang.content.review";

    /**
     * 死信 Topic：消费重试仍失败的消息投递到这里
     */
    public static final String CONTENT_REVIEW_DLT = "shiguang.content.review.DLT";

    /**
     * 消费组 A：审核引擎（单条消费 + 手动 ack，审核判定后写 ES）
     */
    public static final String GROUP_ENGINE = "moderation-engine";

    /**
     * 消费组 B：统计（批量消费）
     * 与 GROUP_ENGINE 消费同一个 Topic，但各自维护 offset、各拿全量消息
     */
    public static final String GROUP_STAT = "moderation-stat";

    private KafkaTopics() {
    }
}
