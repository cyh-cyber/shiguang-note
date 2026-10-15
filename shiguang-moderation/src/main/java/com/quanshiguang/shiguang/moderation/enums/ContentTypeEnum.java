package com.quanshiguang.shiguang.moderation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @date: 2026-10-12
 * @description: 待审核内容类型
 **/
@Getter
@AllArgsConstructor
public enum ContentTypeEnum {

    NOTE("NOTE", "笔记"),
    COMMENT("COMMENT", "评论"),
    USER_PROFILE("USER_PROFILE", "用户资料"),
    ;

    private final String code;
    private final String desc;
}
