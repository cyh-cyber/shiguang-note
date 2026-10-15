package com.quanshiguang.shiguang.moderation.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * @date: 2026-10-12
 * @description: Kafka 监听容器配置：批量消费容器 + 重试/死信错误处理器
 * <p>
 * 说明：单条消费用的默认容器（kafkaListenerContainerFactory）由 Spring Boot 自动配置，
 * 手动提交位点通过 application-dev.yml 的 spring.kafka.listener.ack-mode=manual_immediate 开启。
 * 这里只补充两件事：批量消费容器、以及带死信投递的错误处理器。
 **/
@Configuration
@Slf4j
public class KafkaConfig {

    /**
     * 批量监听容器工厂：一次 poll 处理一批消息（配合 @KafkaListener 的 containerFactory 使用）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> batchKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // 开启批量监听：监听方法参数为 List<ConsumerRecord<String, String>>
        factory.setBatchListener(true);
        // 手动提交位点：只在整批处理成功后提交
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }

    /**
     * 错误处理器：重试 3 次（每次间隔 1 秒），仍失败则投递到 &lt;topic&gt;.DLT 死信 Topic
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}
