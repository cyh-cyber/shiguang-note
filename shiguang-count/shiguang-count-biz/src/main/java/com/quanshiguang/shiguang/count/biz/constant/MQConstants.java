package com.quanshiguang.shiguang.count.biz.constant;
import com.quanshiguang.framework.common.constant.SharedMQConstants;

/**
 * @author: 犬小哈
 * @date: 2024/8/10 13:21
 * @version: v1.0.0
 * @description: MQ 常量
 **/
public interface MQConstants extends SharedMQConstants{


    /**
     * Topic: 粉丝数计数入库
     */
    String TOPIC_COUNT_FANS_2_DB = "CountFans2DBTopic";

    /**
     * Topic: 粉丝数计数入库
     */
    String TOPIC_COUNT_FOLLOWING_2_DB = "CountFollowing2DBTopic";

    /**
     * Topic: 计数 - 笔记点赞数落库
     */
    String TOPIC_COUNT_NOTE_LIKE_2_DB = "CountNoteLike2DBTTopic";

    /**
     * Topic: 计数 - 笔记收藏数落库
     */
    String TOPIC_COUNT_NOTE_COLLECT_2_DB = "CountNoteCollect2DBTTopic";

    /**
     * Topic: 评论点赞数更新
     */
    String TOPIC_COMMENT_LIKE_OR_UNLIKE = "CommentLikeUnlikeTopic";

    /**
     * Topic: 计数 - 评论点赞数落库
     */
    String TOPIC_COUNT_COMMENT_LIKE_2_DB = "CountCommentLike2DBTTopic";



}
