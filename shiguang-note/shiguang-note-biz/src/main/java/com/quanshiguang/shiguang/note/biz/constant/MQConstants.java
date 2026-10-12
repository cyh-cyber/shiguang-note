package com.quanshiguang.shiguang.note.biz.constant;
import com.quanshiguang.framework.common.constant.SharedMQConstants;

/**
 * @author: 犬小哈
 * @date: 2024/5/21 15:04
 * @version: v1.0.0
 * @description: TODO
 **/
public interface MQConstants extends SharedMQConstants{

    /**
     * Topic 主题：删除笔记本地缓存
     */
    String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";

    /**
     * Topic 主题：延迟双删 Redis 笔记缓存
     */
    String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE = "DelayDeleteNoteRedisCacheTopic";


    /**
     * Topic 主题：延迟双删 Redis 已发布笔记列表缓存
     */
    String TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE = "DelayDeletePublishedNoteListRedisCacheTopic";

}
