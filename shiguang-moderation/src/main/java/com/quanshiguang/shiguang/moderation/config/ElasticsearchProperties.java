package com.quanshiguang.shiguang.moderation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @date: 2026-10-12
 * @description: Elasticsearch 配置项
 **/
@ConfigurationProperties(prefix = "elasticsearch")
@Component
@Data
public class ElasticsearchProperties {
    /** ES 地址，格式 host:port，如 127.0.0.1:9200 */
    private String address;
}
