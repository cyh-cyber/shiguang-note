package com.quanshiguang.shiguang.moderation.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.quanshiguang.framework.common.response.PageResponse;
import com.quanshiguang.framework.common.response.Response;
import com.quanshiguang.shiguang.moderation.constant.ModerationIndex;
import com.quanshiguang.shiguang.moderation.enums.ModerationDecisionEnum;
import com.quanshiguang.shiguang.moderation.model.vo.HotHitWordRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.ModerationOverviewRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.ModerationTrendRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.SearchModerationRecordReqVO;
import com.quanshiguang.shiguang.moderation.model.vo.SearchModerationRecordRspVO;
import com.quanshiguang.shiguang.moderation.service.ModerationService;
import io.micrometer.common.util.StringUtils;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.ZoneId;

/**
 * @date: 2026-10-12
 * @description: 审核流水检索与统计实现
 * <p>
 * 骨架阶段只保留方法签名，查询与聚合逻辑待实现。
 **/
@Slf4j
@Service
public class ModerationServiceImpl implements ModerationService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public PageResponse<SearchModerationRecordRspVO> search(SearchModerationRecordReqVO reqVO) {


        SearchRequest searchRequest = new SearchRequest(ModerationIndex.NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        if(StringUtils.isNotBlank(reqVO.getKeyword())){
            bool.must(QueryBuilders.multiMatchQuery(reqVO.getKeyword())
                    .field(ModerationIndex.FIELD_CONTENT, 2.0f)
                    .field(ModerationIndex.FIELD_HIT_WORDS, 1.0f));
        } else {
            bool.must(QueryBuilders.matchAllQuery());
        }

        if(StringUtils.isNotBlank(reqVO.getDecision())){
            bool.filter(QueryBuilders.termQuery(ModerationIndex.FIELD_DECISION, reqVO.getDecision()));
        }

        if(StringUtils.isNotBlank(reqVO.getContentType())){
            bool.filter(QueryBuilders.termQuery(ModerationIndex.FIELD_CONTENT, reqVO.getContentType()));
        }

        if(Objects.nonNull(reqVO.getMinRiskScore())){
            bool.filter(QueryBuilders.rangeQuery(ModerationIndex.FIELD_RISK_SCORE).gte(reqVO.getMinRiskScore()));
        }

        if (StringUtils.isNotBlank(reqVO.getStartTime()) || StringUtils.isNotBlank(reqVO.getEndTime())) {
            RangeQueryBuilder range = QueryBuilders.rangeQuery(ModerationIndex.FIELD_CREATE_TIME);
            if (StringUtils.isNotBlank(reqVO.getStartTime())) range.gte(reqVO.getStartTime());
            if (StringUtils.isNotBlank(reqVO.getEndTime()))   range.lte(reqVO.getEndTime());
            bool.filter(range);
        }

        sourceBuilder.query(bool);
        sourceBuilder.sort(ModerationIndex.FIELD_CREATE_TIME, SortOrder.DESC);

        int pageSize = 10;
        sourceBuilder.from((reqVO.getPageNo() - 1) *pageSize);
        sourceBuilder.size(pageSize);

        HighlightBuilder highlight = new HighlightBuilder();
        highlight.field(ModerationIndex.FIELD_CONTENT)
                .preTags("<em>").postTags("</em>")
                .fragmentSize(120).numOfFragments(1);
        sourceBuilder.highlighter(highlight);

        searchRequest.source(sourceBuilder);

        List<SearchModerationRecordRspVO> records = Lists.newArrayList();
        long total = 0;
        try {
            log.info("==> SearchRequest: {}", searchRequest.source());
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            total = response.getHits().getTotalHits().value;

            for (SearchHit hit : response.getHits()) {
                Map<String, Object> src = hit.getSourceAsMap();

                SearchModerationRecordRspVO vo = new SearchModerationRecordRspVO();
                vo.setEventId(hit.getId());                                       // ★ 不在 _source 里，只能用 docId
                vo.setContentId(((Number) src.get(ModerationIndex.FIELD_CONTENT_ID)).longValue()); // ★ 别 (Long) 强转
                vo.setAuthorId(((Number) src.get(ModerationIndex.FIELD_AUTHOR_ID)).longValue());   // ★ 同上
                vo.setContentType((String) src.get(ModerationIndex.FIELD_CONTENT_TYPE));
                vo.setDecision((String) src.get(ModerationIndex.FIELD_DECISION));
                vo.setRiskScore((Integer) src.get(ModerationIndex.FIELD_RISK_SCORE));
                vo.setCreateTime((String) src.get(ModerationIndex.FIELD_CREATE_TIME));  // date 按 mapping 格式返回字符串
                vo.setHitWords((List<String>) src.get(ModerationIndex.FIELD_HIT_WORDS));
                vo.setRuleCodes((List<String>) src.get(ModerationIndex.FIELD_RULE_CODES));

                // 正文：有高亮片段就用高亮，没有就用原文
                vo.setContent((String) src.get(ModerationIndex.FIELD_CONTENT));
                Map<String, HighlightField> hl = hit.getHighlightFields();
                if (CollUtil.isNotEmpty(hl) && hl.containsKey(ModerationIndex.FIELD_CONTENT)) {
                    vo.setContent(hl.get(ModerationIndex.FIELD_CONTENT).fragments()[0].string());
                }

                records.add(vo);
            }
        } catch (IOException e) {
            log.error("==> 查询 Elasticsearch 异常: ", e);
        }

        return PageResponse.success(records, reqVO.getPageNo(), total);
    }

    @Override
    public Response<List<ModerationTrendRspVO>> trend(String startTime, String endTime) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(startTime) || StringUtils.isNotBlank(endTime)) {
            RangeQueryBuilder range = QueryBuilders.rangeQuery(ModerationIndex.FIELD_CREATE_TIME);
            if (StringUtils.isNotBlank(startTime)) range.gte(startTime);
            if (StringUtils.isNotBlank(endTime))   range.lte(endTime);
            bool.filter(range);
        }
        sourceBuilder.query(bool);

        sourceBuilder.aggregation(AggregationBuilders.dateHistogram("by_day")
                .field(ModerationIndex.FIELD_CREATE_TIME)
                .calendarInterval(DateHistogramInterval.DAY)
                .format("yyyy-MM-dd")
                .timeZone(ZoneId.of("Asia/Shanghai"))     // ★ 按东八区分天
                .minDocCount(0)
                .subAggregation(AggregationBuilders.terms("by_decision")
                        .field(ModerationIndex.FIELD_DECISION)
                        .size(10)));

        List<ModerationTrendRspVO> result = new ArrayList<>();
        try {
            log.info("==> TrendRequest: {}", sourceBuilder);
            SearchRequest request = new SearchRequest(ModerationIndex.NAME);
            request.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            Histogram byDay = response.getAggregations().get("by_day");
            for (Histogram.Bucket dayBucket : byDay.getBuckets()) {
                ModerationTrendRspVO vo = new ModerationTrendRspVO();
                vo.setDate(dayBucket.getKeyAsString());
                vo.setTotal(dayBucket.getDocCount());

                List<ModerationTrendRspVO.DecisionStat> stats = new ArrayList<>();
                Terms byDecision = dayBucket.getAggregations().get("by_decision");   // ★ 从桶里取
                for (Terms.Bucket db : byDecision.getBuckets()) {
                    stats.add(new ModerationTrendRspVO.DecisionStat(
                            db.getKeyAsString(), db.getDocCount()));
                }
                vo.setDecisions(stats);      // ★ 移出内层循环
                result.add(vo);
            }
        } catch (IOException e) {
            log.error("==> 审核趋势聚合查询 Elasticsearch 异常: ", e);
            return Response.fail("审核趋势查询失败");
        }
        return Response.success(result);
    }

    @Override
    public Response<List<HotHitWordRspVO>> hotHitWords(Integer topN) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        int size = (topN == null || topN <= 0) ? 10 : Math.min(topN, 100);


        sourceBuilder.aggregation(AggregationBuilders
                .terms("top_hit_words")
                .field(ModerationIndex.FIELD_HIT_WORDS)
                .size(size));

        List<HotHitWordRspVO> result = new ArrayList<>();
        try {
            log.info("==> HotWordsRequest: {}", sourceBuilder);
            SearchRequest request = new SearchRequest(ModerationIndex.NAME);
            request.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            Terms terms = response.getAggregations().get("top_hit_words");
            terms.getBuckets().forEach(b-> {
                HotHitWordRspVO vo = new HotHitWordRspVO();
                vo.setWord(b.getKeyAsString());
                vo.setCount(b.getDocCount());
                result.add(vo);
            });
        } catch (IOException e){
            log.error("==> 命中词TOP N Elasticsearch 异常: ", e);
            return Response.fail("命中词TOP N 查询失败");
        }
        return Response.success(result);
    }

    @Override
    public Response<ModerationOverviewRspVO> overview(String startTime, String endTime) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        // 1. 时间范围过滤（结构跟 trend 一模一样，将来可以抽成一个私有方法复用）
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(startTime) || StringUtils.isNotBlank(endTime)) {
            RangeQueryBuilder range = QueryBuilders.rangeQuery(ModerationIndex.FIELD_CREATE_TIME);
            if (StringUtils.isNotBlank(startTime)) range.gte(startTime);
            if (StringUtils.isNotBlank(endTime))   range.lte(endTime);
            bool.filter(range);
        }
        sourceBuilder.query(bool);

        // 2. ★ 解除 hits.total 的 10000 截断，否则总量永远是 10000
        sourceBuilder.trackTotalHits(true);

        // 3. 各判定数量：terms 聚合（PASS / REVIEW / REJECT 三个桶，平级挂）
        sourceBuilder.aggregation(AggregationBuilders.terms("by_decision")
                .field(ModerationIndex.FIELD_DECISION)
                .size(10));

        // 4. 作者去重数：cardinality 聚合
        sourceBuilder.aggregation(AggregationBuilders.cardinality("author_cnt")
                .field(ModerationIndex.FIELD_AUTHOR_ID));

        try {
            SearchRequest request = new SearchRequest(ModerationIndex.NAME);
            request.source(sourceBuilder);

            log.info("==> OverviewRequest: {}", sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            ModerationOverviewRspVO vo = new ModerationOverviewRspVO();

            // 5. 总量：用 hits.total，★ 别把三个桶相加
            vo.setTotal(response.getHits().getTotalHits().value);

            // 6. 判定分布：遍历 terms 桶，按 code 落到对应字段
            long passCount = 0L, reviewCount = 0L, rejectCount = 0L;
            Terms byDecision = response.getAggregations().get("by_decision");
            for (Terms.Bucket bucket : byDecision.getBuckets()) {
                String code = bucket.getKeyAsString();
                long count = bucket.getDocCount();
                if (ModerationDecisionEnum.PASS.getCode().equals(code))        passCount = count;
                else if (ModerationDecisionEnum.REVIEW.getCode().equals(code)) reviewCount = count;
                else if (ModerationDecisionEnum.REJECT.getCode().equals(code)) rejectCount = count;
            }
            vo.setPassCount(passCount);
            vo.setReviewCount(reviewCount);
            vo.setRejectCount(rejectCount);

            // 7. 作者去重数
            Cardinality authorCnt = response.getAggregations().get("author_cnt");
            vo.setAuthorCount(authorCnt.getValue());

            return Response.success(vo);
        } catch (IOException e) {
            log.error("==> 审核概览聚合查询 Elasticsearch 异常: ", e);
            return Response.fail("审核概览查询失败");
        }
    }
}
