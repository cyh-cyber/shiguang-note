package com.quanshiguang.shiguang.moderation.mq;

import com.quanshiguang.framework.common.util.JsonUtils;
import com.quanshiguang.shiguang.moderation.constant.KafkaTopics;
import com.quanshiguang.shiguang.moderation.model.event.ContentReviewEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @date: 2026-10-12
 * @description: 审核事件生产者（Kafka）
 **/
@Slf4j
@Component
public class ContentReviewProducer {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送审核事件
     * <p>
     * 注意：分区键（key）使用 contentId —— 保证同一条内容的多条事件落到同一个分区、按发送顺序消费。
     *
     * @param event 审核事件
     */
    public void send(ContentReviewEvent event) {
        //里程碑 2：序列化事件并发送，观察发送结果回调
        String key = String.valueOf(event.getContentId());
        String payload = JsonUtils.toJsonString(event);
        kafkaTemplate.send(KafkaTopics.CONTENT_REVIEW, key, payload).whenComplete((result, ex) -> {
            if(ex != null){
                log.error("审核事件发送失败，evenId={}",event.getEventId(),ex);
            }
            else{
                log.info("审核事件发送成功，eventId={}，partition={}，offset={}",
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
