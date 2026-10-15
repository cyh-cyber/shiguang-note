package com.quanshiguang.shiguang.moderation.constant;

/**
 * @date: 2026-10-12
 * @description: ES 索引名与字段名常量
 **/
public class ModerationIndex {

    /** 审核流水索引名 */
    public static final String NAME = "moderation_record";

    public static final String FIELD_EVENT_ID = "event_id";
    public static final String FIELD_CONTENT_ID = "content_id";
    public static final String FIELD_CONTENT_TYPE = "content_type";
    public static final String FIELD_AUTHOR_ID = "author_id";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_HIT_WORDS = "hit_words";
    public static final String FIELD_RULE_CODES = "rule_codes";
    public static final String FIELD_DECISION = "decision";
    public static final String FIELD_RISK_SCORE = "risk_score";
    public static final String FIELD_CREATE_TIME = "create_time";

    private ModerationIndex() {
    }
}
