package com.quanshiguang.framework.common.constant;

/**
 * 跨模块共享的 MQ 常量（Topic 与 Tag）。
 *
 * 约定：定义在本接口中的常量，至少被两个模块使用（一个生产、一个消费，或两个都是消费者）。
 * 只被单个模块使用的 Topic 请留在各模块自己的 MQConstants 中，不要放这里。
 *
 * 注意：新增或修改此处任何常量的【值】，都会同时影响所有生产方与消费方，
 * 必须全量回归，不要单独改一处。
 */
public interface SharedMQConstants {

    // ==================== 笔记域 ====================

    /** 笔记操作（发布、删除）。生产：note；消费：count、data-align */
    String TOPIC_NOTE_OPERATE = "NoteOperateTopic";

    /** 点赞、取消点赞。生产：note；消费：note、recommend */
    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic";

    /** 收藏、取消收藏。生产：note；消费：note、recommend */
    String TOPIC_COLLECT_OR_UN_COLLECT = "CollectUnCollectTopic";

    /** 计数：笔记点赞数。生产：note；消费：count、data-align */
    String TOPIC_COUNT_NOTE_LIKE = "CountNoteLikeTopic";

    /** 计数：笔记收藏数。生产：note；消费：count、data-align */
    String TOPIC_COUNT_NOTE_COLLECT = "CountNoteCollectTopic";

    /** 笔记发布事务消息。生产：note；消费：kv */
    String TOPIC_PUBLISH_NOTE_TRANSACTION = "PublishNoteTransactionTopic";

    // ==================== 评论域 ====================

    /** 计数：笔记评论总数。生产：comment；消费：count、comment */
    String TOPIC_COUNT_NOTE_COMMENT = "CountNoteCommentTopic";

    /** 评论热度值更新。生产：comment；消费：comment */
    String TOPIC_COMMENT_HEAT_UPDATE = "CommentHeatUpdateTopic";

    /** 评论点赞、取消点赞。生产：comment；消费：count */
    String TOPIC_COMMENT_LIKE_OR_UNLIKE = "CommentLikeUnlikeTopic";

    // ==================== 用户关系域 ====================

    /** 关注数计数。生产：user-relation；消费：count、data-align */
    String TOPIC_COUNT_FOLLOWING = "CountFollowingTopic";

    /** 粉丝数计数。生产：user-relation；消费：count */
    String TOPIC_COUNT_FANS = "CountFansTopic";

    // ==================== 通用 Tag ====================

    /** Tag：笔记发布 */
    String TAG_NOTE_PUBLISH = "publishNote";

    /** Tag：笔记删除 */
    String TAG_NOTE_DELETE = "deleteNote";

    /** Tag：点赞 */
    String TAG_LIKE = "Like";

    /** Tag：取消点赞 */
    String TAG_UNLIKE = "Unlike";

    /** Tag：收藏 */
    String TAG_COLLECT = "Collect";

    /** Tag：取消收藏 */
    String TAG_UN_COLLECT = "UnCollect";
}