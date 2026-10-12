# 项目长期记忆 — qiushui-shiguang（时光 / Spring Cloud 微服务）

## 铁律（用户明确要求，最高优先级）

- **未经用户明确确认，绝对不要修改任何代码。**
  - 「改代码」包含但不限于：新增/删除/重命名源文件、编辑 Java/Lua/YAML/XML/SQL/Dockerfile/pom.xml、执行 `git commit`/`git checkout`、批量格式化。
  - 即便看起来是明显的 bug 或优化点，也只允许**说明问题 + 给出建议 diff/方案**，不得直接落到磁盘。
  - 每次修改前必须先列出「要改哪些文件、改成什么、影响范围」，等用户回复确认后再动手。
  - 用户说「帮我改」才算授权；没有这句话，默认只读。
- 只读操作（Read/Glob/Grep/查看依赖树/编译前分析）不受限制，可自由进行。

## 项目速览

- 技术栈：Spring Cloud Alibaba 多模块微服务（Maven 父工程 + 业务子模块）。
- 模块：agent / auth / benchmark / comment / count / data-align / distributed-id-generator / framework / gateway / kv / note / oss / recommend / search / user / user-relation。
- 数据库脚本与热点逻辑较多使用 Lua（comment、data-align、note、user-relation）。
- 已有面试准备文档：项目技术细节说明_AI接手版.md、项目面试八股问答.md、项目深度面试问答.md、STAR项目深挖面试回答.md。

## 协作偏好

- 语言：简体中文。
- **主人当前的技术水平**：Java 基础 + Spring Boot 熟练，**尚未接触** Spring Cloud（Nacos/Feign/Gateway）、RocketMQ、Redis Lua、ES、Cassandra、XXL-JOB。
  - 讲解时需先把分布式组件"降维"成普通 Spring Boot 概念再解释，避免直接堆术语。
  - 不要假设他懂服务发现、异步消息、缓存一致性这些背景。
