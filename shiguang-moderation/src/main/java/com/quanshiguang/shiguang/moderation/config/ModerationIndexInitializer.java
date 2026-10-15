package com.quanshiguang.shiguang.moderation.config;

import com.quanshiguang.shiguang.moderation.constant.ModerationIndex;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class ModerationIndexInitializer {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Bean
    public ApplicationRunner initModerationIndex() {
        return args -> {
            GetIndexRequest existsReq = new GetIndexRequest().indices(ModerationIndex.NAME);
            if (restHighLevelClient.indices().exists(existsReq, RequestOptions.DEFAULT)) {
                log.info("## 索引 {} 已存在，跳过创建", ModerationIndex.NAME);
                return;
            }
            String mappingJson = StreamUtils.copyToString(
                    new ClassPathResource("es/moderation_record_mapping.json").getInputStream(),
                    StandardCharsets.UTF_8);

            CreateIndexRequest createReq = new CreateIndexRequest(ModerationIndex.NAME);
            createReq.source(mappingJson, XContentType.JSON);
            restHighLevelClient.indices().create(createReq, RequestOptions.DEFAULT);
            log.info("## 索引 {} 创建成功", ModerationIndex.NAME);
        };
    }
}