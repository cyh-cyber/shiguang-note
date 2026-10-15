package com.quanshiguang.shiguang.moderation.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date: 2026-10-12
 * @description: 命中词 TopN 响应参数
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotHitWordRspVO {

    /** 敏感词 */
    private String word;

    /** 命中次数 */
    private Long count;
}
