package com.quanshiguang.shiguang.moderation.controller;

import com.quanshiguang.framework.biz.operationlog.aspect.ApiOperationLog;
import com.quanshiguang.framework.common.response.PageResponse;
import com.quanshiguang.framework.common.response.Response;
import com.quanshiguang.shiguang.moderation.model.vo.HotHitWordRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.ModerationOverviewRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.ModerationTrendRspVO;
import com.quanshiguang.shiguang.moderation.model.vo.SearchModerationRecordReqVO;
import com.quanshiguang.shiguang.moderation.model.vo.SearchModerationRecordRspVO;
import com.quanshiguang.shiguang.moderation.service.ModerationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @date: 2026-10-12
 * @description: 审核后台：检索与统计接口
 **/
@RestController
@RequestMapping("/moderation/admin")
@Slf4j
public class ModerationAdminController {

    @Resource
    private ModerationService moderationService;

    @PostMapping("/record/search")
    @ApiOperationLog(description = "审核流水检索")
    public PageResponse<SearchModerationRecordRspVO> search(@Validated @RequestBody SearchModerationRecordReqVO reqVO) {
        return moderationService.search(reqVO);
    }

    @GetMapping("/stat/trend")
    @ApiOperationLog(description = "审核趋势（按天）")
    public Response<List<ModerationTrendRspVO>> trend(@RequestParam String startTime,
                                                      @RequestParam String endTime) {
        return moderationService.trend(startTime, endTime);
    }

    @GetMapping("/stat/hotwords")
    @ApiOperationLog(description = "命中词 TopN")
    public Response<List<HotHitWordRspVO>> hotHitWords(@RequestParam(defaultValue = "10") Integer topN) {
        return moderationService.hotHitWords(topN);
    }

    @GetMapping("/stat/overview")
    @ApiOperationLog(description = "审核概览")
    public Response<ModerationOverviewRspVO> overview(@RequestParam String startTime,
                                                      @RequestParam String endTime) {
        return moderationService.overview(startTime, endTime);
    }
}
