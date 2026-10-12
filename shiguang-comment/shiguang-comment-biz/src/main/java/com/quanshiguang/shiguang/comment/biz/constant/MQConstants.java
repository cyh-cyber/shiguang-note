package com.quanshiguang.shiguang.comment.biz.constant;
import com.quanshiguang.framework.common.constant.SharedMQConstants;


/**
 * @author: 犬小哈
 * @date: 2024/8/10 13:21
 * @version: v1.0.0
 * @description: MQ 常量
 **/
public interface MQConstants extends SharedMQConstants{

    /**
     * Topic: 评论发布
     */
    String TOPIC_PUBLISH_COMMENT = "PublishCommentTopic";

    /**
     * Topic: 删除本地缓存 —— 评论详情
     */
    String TOPIC_DELETE_COMMENT_LOCAL_CACHE = "DeleteCommentDetailLocalCacheTopic";

    /**
     * Topic: 删除评论
     */
    String TOPIC_DELETE_COMMENT = "DeleteCommentTopic";


}
