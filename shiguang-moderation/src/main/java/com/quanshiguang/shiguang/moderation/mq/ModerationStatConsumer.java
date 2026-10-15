package com.quanshiguang.shiguang.moderation.mq;

import com.quanshiguang.framework.common.util.JsonUtils;
import com.quanshiguang.shiguang.moderation.constant.KafkaTopics;
import com.quanshiguang.shiguang.moderation.constant.RedisKeyConstants;
import com.quanshiguang.shiguang.moderation.model.event.ContentReviewEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @date: 2026-10-12
 * @description: 消费组 B（moderation-stat）：批量消费，做统计
 * <p>
 * 与 ModerationEngineConsumer 消费同一个 Topic，但消费组不同 —— 各自维护 offset、各拿全量消息。
 **/
@Slf4j
@Component
public class ModerationStatConsumer {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(
            topics = KafkaTopics.CONTENT_REVIEW,
            groupId = KafkaTopics.GROUP_STAT,
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void onMessages(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        log.info("## [stat] 一批拉取 {} 条, 起始 offset={}", records.size(), records.get(0).offset());

        // 里程碑 5：整批聚合后累加统计（示例：按天累加审核量到 Redis Hash）
          for (ConsumerRecord<String, String> record : records) {
              ContentReviewEvent event = JsonUtils.parseObject(record.value(), ContentReviewEvent.class);
              redisTemplate.opsForHash().increment(RedisKeyConstants.STAT_DAILY,
                      event.getOccurredAt().toLocalDate().toString(), 1);
          }
          ack.acknowledge();   // 整批处理完再提交位点，吞吐更高（代价：失败要整批重放）
    }
}
