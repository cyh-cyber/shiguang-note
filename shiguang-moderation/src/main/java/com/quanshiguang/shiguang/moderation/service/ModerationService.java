package com.quanshiguang.shiguang.moderation.service;

import com.quanshiguang.framework.common.response.PageResponse;
import com.quanshiguang.framework.common.response.Response;
import com.quanshiguang.shiguang.moderation.model.vo.HotHitWordRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.ModerationOverviewRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.ModerationTrendRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.SearchModerationRecordReqVO;
import com.quanshiguang.shiguang.moderation.model.vo.SearchModerationRecordRspVO;

import java.util.List;

/**
 * @date: 2026-10-12
 * @description: 审核流水检索与统计
 **/
public interface ModerationService {

    /**
     * 审核流水检索：全文检索 + 多条件筛选 + 高亮 + 分页
     *
     * @param reqVO 检索条件
     * @return 分页结果
     */
    PageResponse<SearchModerationRecordRspVO> search(SearchModerationRecordReqVO reqVO);

    /**
     * 按天统计审核趋势（含各判定结果分布）
     *
     * @param startTime 开始时间，格式 yyyy-MM-dd HH:mm:ss
     * @param endTime   结束时间，格式 yyyy-MM-dd HH:mm:ss
     * @return 按天的趋势列表
     */
    Response<List<ModerationTrendRspVO>> trend(String startTime, String endTime);

    /**
     * 命中词 TopN
     *
     * @param topN 取前 N 个
     * @return 命中词及次数
     */
    Response<List<HotHitWordRspVO>> hotHitWords(Integer topN);

    /**
     * 审核概览：总量、各判定数量、作者去重数
     *
     * @param startTime 开始时间，格式 yyyy-MM-dd HH:mm:ss
     * @param endTime   结束时间，格式 yyyy-MM-dd HH:mm:ss
     * @return 概览数据
     */
    Response<ModerationOverviewRspVO> overview(String startTime, String endTime);
}
