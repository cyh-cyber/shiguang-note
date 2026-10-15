package com.quanshiguang.shiguang.moderation.constant;

/**
 * @date: 2026-10-12
 * @description: Redis Key 常量
 **/
public class RedisKeyConstants {

    /**
     * 每日审核量统计（Hash）
     * field = yyyy-MM-dd，value = 当天审核事件总数
     * 由第二个消费组（moderation-stat）批量累加
     */
    public static final String STAT_DAILY = "moderation:stat:daily";

    private RedisKeyConstants() {
    }
}
