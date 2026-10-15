package com.quanshiguang.shiguang.moderation.repository;

import com.quanshiguang.shiguang.moderation.constant.ModerationIndex;
import com.quanshiguang.shiguang.moderation.model.bo.ModerationResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ModerationRecordRepository {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /** 单条写入 */
    public void save(ModerationResult r) {
        try {
            restHighLevelClient.index(toIndexRequest(r), RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("## 写入审核流水失败, eventId={}", r.getEventId(), e);
        }
    }

    /** 批量写入（统计消费组可用） */
    public void saveBatch(List<ModerationResult> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        list.forEach(r -> bulkRequest.add(toIndexRequest(r)));
        try {
            BulkResponse resp = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (resp.hasFailures()) {
                log.error("## 批量写入部分失败: {}", resp.buildFailureMessage());
            }
        } catch (IOException e) {
            log.error("## 批量写入审核流水失败", e);
        }
    }

    private IndexRequest toIndexRequest(ModerationResult r) {
        Map<String, Object> doc = new HashMap<>();
        doc.put(ModerationIndex.FIELD_CONTENT_ID, r.getContentId());
        doc.put(ModerationIndex.FIELD_CONTENT_TYPE, r.getContentType());
        doc.put(ModerationIndex.FIELD_AUTHOR_ID, r.getAuthorId());
        doc.put(ModerationIndex.FIELD_CONTENT, r.getContent());
        doc.put(ModerationIndex.FIELD_HIT_WORDS, r.getHitWords());
        doc.put(ModerationIndex.FIELD_RULE_CODES, r.getRuleCodes());
        doc.put(ModerationIndex.FIELD_DECISION, r.getDecision());
        doc.put(ModerationIndex.FIELD_RISK_SCORE, r.getRiskScore());
        doc.put(ModerationIndex.FIELD_CREATE_TIME, r.getCreateTime().format(FMT));

        // docId 用 eventId —— 同一条事件重复消费时是"覆盖"，天然幂等
        return new IndexRequest(ModerationIndex.NAME)
                .id(r.getEventId())
                .source(doc);
    }
}