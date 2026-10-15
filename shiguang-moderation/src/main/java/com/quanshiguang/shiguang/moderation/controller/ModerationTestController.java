package com.quanshiguang.shiguang.moderation.controller;

import cn.hutool.core.util.IdUtil;
import com.quanshiguang.framework.biz.operationlog.aspect.ApiOperationLog;
import com.quanshiguang.framework.common.response.Response;
import com.quanshiguang.shiguang.moderation.model.event.ContentReviewEvent;
import com.quanshiguang.shiguang.moderation.mq.ContentReviewProducer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @date: 2026-10-12
 * @description: 测试入口：往 Kafka 灌一条审核事件
 * <p>
 * 前几个里程碑全靠它灌数据，不依赖任何老代码，也不需要改网关路由。
 * 直接访问：POST http://127.0.0.1:8095/moderation/test/event
 **/
@RestController
@RequestMapping("/moderation/test")
@Slf4j
public class ModerationTestController {

    @Resource
    private ContentReviewProducer contentReviewProducer;

    @PostMapping("/event")
    public Response<?> mockEvent(@RequestBody Map<String, Object> body) {
        ContentReviewEvent event = ContentReviewEvent.builder()
                .eventId(String.valueOf(IdUtil.getSnowflakeNextId()))
                .contentType((String) body.getOrDefault("contentType", "COMMENT"))
                .contentId(Long.valueOf(String.valueOf(body.getOrDefault("contentId", 1))))
                .authorId(Long.valueOf(String.valueOf(body.getOrDefault("authorId", 1))))
                .content((String) body.getOrDefault("content", ""))
                .version(1)
                .occurredAt(LocalDateTime.now())
                .build();
        contentReviewProducer.send(event);
        return Response.success(event.getEventId());
    }
}
