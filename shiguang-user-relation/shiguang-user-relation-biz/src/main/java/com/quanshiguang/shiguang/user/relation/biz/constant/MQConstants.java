package com.quanshiguang.shiguang.user.relation.biz.constant;
import com.quanshiguang.framework.common.constant.SharedMQConstants;

/**
 * @author: 犬小哈
 * @date: 2024/8/10 13:21
 * @version: v1.0.0
 * @description: MQ 常量
 **/
public interface MQConstants extends SharedMQConstants{

    /**
     * Topic: 关注、取关共用一个
     */
    String TOPIC_FOLLOW_OR_UNFOLLOW = "FollowUnfollowTopic";


    /**
     * 关注标签
     */
    String TAG_FOLLOW = "Follow";

    /**
     * 取关标签
     */
    String TAG_UNFOLLOW = "Unfollow";
}
