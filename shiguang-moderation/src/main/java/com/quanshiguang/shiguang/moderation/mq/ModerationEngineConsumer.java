package com.quanshiguang.shiguang.moderation.mq;

import com.quanshiguang.framework.common.util.JsonUtils;
import com.quanshiguang.shiguang.moderation.constant.KafkaTopics;
import com.quanshiguang.shiguang.moderation.engine.ModerationEngine;
import com.quanshiguang.shiguang.moderation.model.bo.ModerationResult;
import com.quanshiguang.shiguang.moderation.model.event.ContentReviewEvent;
import com.quanshiguang.shiguang.moderation.repository.ModerationRecordRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @date: 2026-10-12
 * @description: 消费组 A（moderation-engine）：审核判定 + 写 ES
 * <p>
 * 骨架阶段只打印收到的消息，不做判定与落库。
 * 之所以不直接抛异常，是为了避免"消费失败 -> 重试 -> 死信"的重试风暴干扰调试。
 **/
@Slf4j
@Component
public class ModerationEngineConsumer {

    @Resource
    private ModerationEngine moderationEngine;

    @Resource
    private ModerationRecordRepository moderationRecordRepository;

    @KafkaListener(
            topics = KafkaTopics.CONTENT_REVIEW,
            groupId = KafkaTopics.GROUP_ENGINE,
            concurrency = "3"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {

        long start = System.currentTimeMillis();
        log.info("## [engine] 收到审核事件, key={}, partition={}, offset={}",
                record.key(), record.partition(), record.offset());

        // 里程碑 3：完成以下四步后，再把消息位点提交
        ContentReviewEvent event = JsonUtils.parseObject(record.value(), ContentReviewEvent.class);
        ModerationResult result = moderationEngine.judge(event);
        moderationRecordRepository.save(result);   // docId = eventId，重复消费即覆盖，天然幂等
        ack.acknowledge();                          // 只在处理成功后提交位点（at-least-once）

        log.info("## [engine] 处理完成, eventId={}, decision={}, cost={}ms",
                event.getEventId(), result.getDecision(), System.currentTimeMillis() - start);
    }
}
