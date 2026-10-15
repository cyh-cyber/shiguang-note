package com.quanshiguang.shiguang.moderation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @date: 2026-10-12
 * @description: 审核判定结论
 **/
@Getter
@AllArgsConstructor
public enum ModerationDecisionEnum {

    PASS("PASS", "通过"),
    REVIEW("REVIEW", "待人工复审"),
    REJECT("REJECT", "拒绝"),
    ;

    private final String code;
    private final String desc;
}
