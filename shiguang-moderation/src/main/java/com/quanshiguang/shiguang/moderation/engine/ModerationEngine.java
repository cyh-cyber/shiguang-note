package com.quanshiguang.shiguang.moderation.engine;

import com.quanshiguang.shiguang.moderation.enums.ModerationDecisionEnum;
import com.quanshiguang.shiguang.moderation.model.bo.ModerationResult;
import com.quanshiguang.shiguang.moderation.model.event.ContentReviewEvent;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class ModerationEngine {

    private static final Pattern EXTERNAL_LINK =
            Pattern.compile("(https?://|www\\.|([a-zA-Z0-9-]+\\.)+(com|cn|net|org))");
    private static final Pattern REPEAT_CHAR = Pattern.compile("(.)\\1{7,}");

    @Resource
    private SensitiveWordFilter sensitiveWordFilter;

    @Value("${moderation.rule.max-length:2000}")
    private int maxLength;
    @Value("${moderation.rule.review-score:10}")
    private int reviewScore;
    @Value("${moderation.rule.reject-score:30}")
    private int rejectScore;

    public ModerationResult judge(ContentReviewEvent event) {
        String content = event.getContent();
        List<String> hitWords = new ArrayList<>();
        List<String> ruleCodes = new ArrayList<>();
        int score = 0;

        // 规则 1：敏感词（每个 +10）
        Set<String> words = sensitiveWordFilter.match(content);
        if (!words.isEmpty()) {
            hitWords.addAll(words);
            ruleCodes.add("SENSITIVE_WORD");
            score += words.size() * 10;
        }

        // 规则 2：超长（+5）
        if (StringUtils.length(content) > maxLength) {
            ruleCodes.add("TOO_LONG");
            score += 5;
        }

        // 规则 3：外链（+5）
        if (StringUtils.isNotBlank(content) && EXTERNAL_LINK.matcher(content).find()) {
            ruleCodes.add("EXTERNAL_LINK");
            score += 5;
        }

        // 规则 4：连续重复字符（+5）
        if (StringUtils.isNotBlank(content) && REPEAT_CHAR.matcher(content).find()) {
            ruleCodes.add("REPEAT_CHAR");
            score += 5;
        }

        String decision = score >= rejectScore ? ModerationDecisionEnum.REJECT.getCode()
                : score >= reviewScore ? ModerationDecisionEnum.REVIEW.getCode()
                : ModerationDecisionEnum.PASS.getCode();

        return ModerationResult.builder()
                .eventId(event.getEventId())
                .contentType(event.getContentType())
                .contentId(event.getContentId())
                .authorId(event.getAuthorId())
                .content(content)
                .version(event.getVersion())
                .hitWords(hitWords)
                .ruleCodes(ruleCodes)
                .riskScore(score)
                .decision(decision)
                .createTime(LocalDateTime.now())
                .build();
    }
}