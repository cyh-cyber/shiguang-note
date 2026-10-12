# MQ 常量跨模块共享 —— 手把手修改方案

> 本文只给方案，**未改动任何源码**。所有改动都需要主人确认后再执行。
> 涉及文件：7 个 `MQConstants.java` + 2 个 recommend 消费者 + 1 个新建的公共常量类。

---

## 一、先把问题看准：这不是一个 bug，是三类

主人指出的「常量不共享 + 硬编码」其实包含三个层次，**严重程度和改法都不同**，混在一起改会乱。

| 类别 | 是什么 | 风险 | 本次是否处理 |
|---|---|---|---|
| **A 类** | 同一个 Topic 字符串在 2~3 个模块各声明一遍 | 改名时要人肉同步 N 处，漏一处就"发了没人收"，且**编译期不报错** | ✅ 处理 |
| **B 类** | recommend 直接写字面量 `"LikeUnlikeTopic"`，连常量都没抽 | 同上，而且更隐蔽（搜常量名搜不到） | ✅ 处理 |
| **C 类** | 消息 DTO 在多个模块各写一份（如 `LikeUnlikeNoteMqDTO`） | 契约漂移：一端加字段，另一端不知道 | ⚠️ 只提示，本次不动 |

主人问的是 A + B，本文覆盖 A + B，C 在第九节说明。

---

## 二、精确盘点：到底重复了哪些（这是全文最重要的一张表）

小秋把 7 个 `MQConstants.java` 全部逐行比对过，结论如下。

### 2.1 A 类：跨模块重复声明的 Topic 与 Tag

| 常量名 | 字符串值 | 重复声明于（模块:行号） | 谁在消费 |
|---|---|---|---|
| `TOPIC_NOTE_OPERATE` | `NoteOperateTopic` | note:39、count:59、data-align:24 | count、data-align |
| `TOPIC_COUNT_NOTE_LIKE` | `CountNoteLikeTopic` | note:49、count:34、data-align:14 | count、data-align |
| `TOPIC_COUNT_NOTE_COLLECT` | `CountNoteCollectTopic` | note:54、count:39、data-align:19 | count、data-align |
| `TOPIC_COUNT_FOLLOWING` | `CountFollowingTopic` | count:14、user-relation:19、data-align:29 | count、data-align |
| `TOPIC_COUNT_FANS` | `CountFansTopic` | count:19、user-relation:24 | count |
| `TOPIC_COUNT_NOTE_COMMENT` | `CountNoteCommentTopic` | comment:19、count:54 | count ×2、comment |
| `TOPIC_COMMENT_HEAT_UPDATE` | `CommentHeatUpdateTopic` | comment:24、count:64 | comment |
| `TOPIC_COMMENT_LIKE_OR_UNLIKE` | `CommentLikeUnlikeTopic` | comment:29、count:69 | count |
| `TOPIC_PUBLISH_NOTE_TRANSACTION` | `PublishNoteTransactionTopic` | note:24、kv:14 | kv |
| `TOPIC_LIKE_OR_UNLIKE` | `LikeUnlikeTopic` | 只在 note:34 定义 | note、**recommend（硬编码）** |
| `TOPIC_COLLECT_OR_UN_COLLECT` | `CollectUnCollectTopic` | 只在 note:44 定义 | note、**recommend（硬编码）** |
| `TAG_NOTE_PUBLISH` | `publishNote` | note:79、count:79 | count |
| `TAG_NOTE_DELETE` | `deleteNote` | note:84、count:84 | count |

**共 13 个常量存在跨模块问题。**

### 2.2 B 类：recommend 的硬编码

| 文件 | 行号 | 硬编码内容 |
|---|---|---|
| `recommend/biz/consumer/UserInterestByLikeConsumer.java` | 27 | `consumerGroup = "shiguang_recommend_group_LikeUnlikeTopic"` |
| 同上 | 28 | `topic = "LikeUnlikeTopic"` |
| `recommend/biz/consumer/UserInterestByCollectConsumer.java` | 28 | `consumerGroup = "shiguang_recommend_group_CollectUnCollectTopic"` |
| 同上 | 29 | `topic = "CollectUnCollectTopic"` |

**注意：recommend 模块压根没有 `MQConstants.java`**——所以它连"抽常量"这一步都没做。

### 2.3 不重复的（本次**不动**，避免过度改动）

- note 私有：`DeleteNoteLocalCacheTopic`、`DelayDeleteNoteRedisCacheTopic`、`DelayDeletePublishedNoteListRedisCacheTopic`
- user 私有：`DelayDeleteUserRedisCacheTopic`（整个 user 模块只有这一个常量）
- user-relation 私有：`FollowUnfollowTopic`、`TAG_FOLLOW`、`TAG_UNFOLLOW`（已核实只在本模块生产+消费）
- comment 私有：`PublishCommentTopic`、`DeleteCommentDetailLocalCacheTopic`、`DeleteCommentTopic`
- count 私有：全部 `*_2_DB` 系列（如 `CountNoteLike2DBTTopic`）

---

## 三、先决定"公共常量放哪里"

### 方案 A：放进已有的 `shiguang-common`（**推荐**）

路径：`shiguang-framework/shiguang-common/src/main/java/com/quanshiguang/framework/common/constant/`

**为什么推荐**：小秋核实过，`shiguang-common` 已经被 **所有** 相关模块在 pom 里直接依赖（note-biz、count-biz、comment-biz、kv-biz、user-relation-biz、data-align、recommend-biz 的 pom.xml 第 21~23 行都有 `<artifactId>shiguang-common</artifactId>`），而且该模块**已经有 `constant` 包**（`DateConstants`、`GlobalConstants`）——放进去完全自然。

**代价：零 pom 改动。** 这是它最大的优点。

**唯一的争议**：`shiguang-common` 名义上是"框架层"，把业务 Topic 放进去，等于框架知道了业务。想要更纯的架构就选方案 B。

### 方案 B：新建独立模块 `shiguang-mq-api`（更"正确"，但成本高）

优点：框架不染指业务；未来可把消息 DTO 也一起放进来（正好解决 C 类问题）。
缺点：要改顶层 `pom.xml` 的 `<modules>`、新建模块 pom、再给 8 个模块的 pom 各加一个依赖。

**小秋的建议**：**先走方案 A 把重复消掉**（一次安静的重构，风险最低）。等以后真的要动 C 类（消息 DTO）时，再顺势升级成方案 B —— 那时候改 pom 是顺带的，不是额外的。

---

## 四、第一步：新建公共常量类（完整代码，可直接用）

**新建文件**：`shiguang-framework/shiguang-common/src/main/java/com/quanshiguang/framework/common/constant/SharedMQConstants.java`

```java
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
```

**关于命名**：这里特意叫 `SharedMQConstants` 而**不是** `MQConstants`——因为每个模块里已经有一个 `MQConstants` 了，同名会导致 import 冲突。这个名字也自带说明性："共享的"。

---

## 五、第二步：各模块怎么改

### 核心手法：让模块常量接口 **继承** 公共接口

```java
public interface MQConstants extends SharedMQConstants {
    // 只保留本模块私有的，重复的全删掉
}
```

**这样做的好处（关键）**：Java 里接口的字段是 `public static final`，会被子接口继承。所以 `MQConstants.TOPIC_COUNT_NOTE_LIKE` 这样的写法**依然编译通过**（常量来自父接口 `SharedMQConstants`）。

> **也就是说：所有调用点一个都不用改。** 改动只集中在 7 个常量文件里。

**每个模块要做的三件事**：
1. 加一行 `import com.quanshiguang.framework.common.constant.SharedMQConstants;`
2. 接口声明加 `extends SharedMQConstants`
3. 删掉已上移到公共类的常量声明

---

### 5.1 note 模块

文件：`shiguang-note/shiguang-note-biz/src/main/java/com/quanshiguang/shiguang/note/biz/constant/MQConstants.java`

**删除**（13 行常量）：`TOPIC_NOTE_OPERATE`、`TOPIC_LIKE_OR_UNLIKE`、`TOPIC_COLLECT_OR_UN_COLLECT`、`TOPIC_COUNT_NOTE_LIKE`、`TOPIC_COUNT_NOTE_COLLECT`、`TOPIC_PUBLISH_NOTE_TRANSACTION`、`TAG_LIKE`、`TAG_UNLIKE`、`TAG_COLLECT`、`TAG_UN_COLLECT`、`TAG_NOTE_PUBLISH`、`TAG_NOTE_DELETE`

**保留**（3 个私有）：
```java
String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";
String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE = "DelayDeleteNoteRedisCacheTopic";
String TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE = "DelayDeletePublishedNoteListRedisCacheTopic";
```

**改后**：
```java
package com.quanshiguang.shiguang.note.biz.constant;

import com.quanshiguang.framework.common.constant.SharedMQConstants;

public interface MQConstants extends SharedMQConstants {

    /** Topic 主题：删除笔记本地缓存 */
    String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";

    /** Topic 主题：延迟双删 Redis 笔记缓存 */
    String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE = "DelayDeleteNoteRedisCacheTopic";

    /** Topic 主题：延迟双删 Redis 已发布笔记列表缓存 */
    String TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE = "DelayDeletePublishedNoteListRedisCacheTopic";
}
```

---

### 5.2 count 模块

文件：`shiguang-count/shiguang-count-biz/.../count/biz/constant/MQConstants.java`

**删除**：`TOPIC_COUNT_FOLLOWING`、`TOPIC_COUNT_FANS`、`TOPIC_COUNT_NOTE_LIKE`、`TOPIC_COUNT_NOTE_COLLECT`、`TOPIC_COUNT_NOTE_COMMENT`、`TOPIC_NOTE_OPERATE`、`TOPIC_COMMENT_HEAT_UPDATE`、`TOPIC_COMMENT_LIKE_OR_UNLIKE`、`TAG_NOTE_PUBLISH`、`TAG_NOTE_DELETE`

**保留**（全部是 count 私有的落库 Topic）：
```java
String TOPIC_COUNT_FANS_2_DB = "CountFans2DBTopic";
String TOPIC_COUNT_FOLLOWING_2_DB = "CountFollowing2DBTopic";
String TOPIC_COUNT_NOTE_LIKE_2_DB = "CountNoteLike2DBTTopic";
String TOPIC_COUNT_NOTE_COLLECT_2_DB = "CountNoteCollect2DBTTopic";
String TOPIC_COMMENT_LIKE_OR_UNLIKE = ...   // 见下方注意
String TOPIC_COUNT_COMMENT_LIKE_2_DB = "CountCommentLike2DBTTopic";
```

> ⚠️ **注意**：`TOPIC_COMMENT_LIKE_OR_UNLIKE` 定义在 **公共类**（因为 comment 和 count 都用）。所以 count 这边删掉即可，`MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE` 依然可用。

**改后接口头**：
```java
public interface MQConstants extends SharedMQConstants { ... }
```

---

### 5.3 data-align 模块

文件：`shiguang-data-align/.../data/align/constant/MQConstants.java`

这个模块的 4 个常量（`TOPIC_COUNT_NOTE_LIKE`、`TOPIC_COUNT_NOTE_COLLECT`、`TOPIC_NOTE_OPERATE`、`TOPIC_COUNT_FOLLOWING`）**全部**要被上移。

**改后**：
```java
package com.quanshiguang.shiguang.data.align.constant;

import com.quanshiguang.framework.common.constant.SharedMQConstants;

/**
 * data-align 使用的 MQ 常量全部来自公共定义，此处仅保留接口以兼容既有引用。
 */
public interface MQConstants extends SharedMQConstants {
}
```

> 这个空接口看起来别扭，但它的作用是：**让 `MQConstants.TOPIC_NOTE_OPERATE` 这类既有写法继续编译通过，本次改动不扩散到任何消费者文件**。
> 如果主人愿意多改几个文件，也可以把这个类直接删掉、把消费者里的 `MQConstants.XXX` 改成 `SharedMQConstants.XXX`——那就是第九节的"第二步"。

---

### 5.4 kv 模块

文件：`shiguang-kv/shiguang-kv-biz/.../kv/biz/constant/MQConstants.java`

情况和 data-align 一样，唯一常量 `TOPIC_PUBLISH_NOTE_TRANSACTION` 上移 → 变成空接口：

```java
package com.quanshiguang.shiguang.kv.biz.constant;

import com.quanshiguang.framework.common.constant.SharedMQConstants;

public interface MQConstants extends SharedMQConstants {
}
```

---

### 5.5 user-relation 模块

文件：`shiguang-user-relation/.../user/relation/biz/constant/MQConstants.java`

**删除**：`TOPIC_COUNT_FOLLOWING`、`TOPIC_COUNT_FANS`
**保留**：`TOPIC_FOLLOW_OR_UNFOLLOW`、`TAG_FOLLOW`、`TAG_UNFOLLOW`

> `TAG_FOLLOW`/`TAG_UNFOLLOW` **不上移**——小秋核实过它们只在本模块的生产方（`RelationServiceImpl` 163/272 行）和本模块的消费者（`FollowUnfollowConsumer` 77/79 行）里使用，是模块私有的。

---

### 5.6 comment 模块 ⚠️（**这一节要特别小心**）

文件：`shiguang-comment/shiguang-comment-biz/.../comment/biz/constant/MQConstants.java`

**删除**：`TOPIC_COUNT_NOTE_COMMENT`、`TOPIC_COMMENT_HEAT_UPDATE`、`TOPIC_COMMENT_LIKE_OR_UNLIKE`、`TAG_LIKE`
**保留**：`TOPIC_PUBLISH_COMMENT`、`TOPIC_DELETE_COMMENT_LOCAL_CACHE`、`TOPIC_DELETE_COMMENT`

**然后是这个改动里唯一有"行为风险"的地方**：

```java
// comment 现在写的是：
String TAG_UNLIKE = "UnLike";    // ← 大写 L

// 而 note 和公共类里写的是：
String TAG_UNLIKE = "Unlike";    // ← 小写 l
```

**同一个常量名，两个模块的值不一样。** 一旦 comment 改成 `extends SharedMQConstants` 并删掉自己的声明，它发出的 tag 就会从 `"UnLike"` **变成** `"Unlike"`。

小秋核实过的安全性结论：
- comment 发消息的地方是 `CommentServiceImpl` 第 639 行：`TOPIC_COMMENT_LIKE_OR_UNLIKE + ":" + TAG_UNLIKE`
- 该 Topic 的**唯一消费者**是 `count/CountCommentLikeConsumer`，而它**根本不读 tag**（它只解析 body 里的 `type` 字段判断点赞/取消）。

所以**改了不会出错**。但这仍是一次行为变更，请主人按下面二选一：

- **选项 1（推荐）**：统一成 `"Unlike"`，删除 comment 本地的 `TAG_UNLIKE`。
  理由：与 `TAG_LIKE = "Like"` 风格一致；note 是主要生产方。
- **选项 2（保守）**：在 comment 的 `MQConstants` 里**重新声明** `String TAG_UNLIKE = "UnLike";` 覆盖父接口的同名常量，保持线上 tag 字符串完全不变。
  代价：这个"同名不同值"的隐患被保留下来了，属于把问题往后推。

> **这一条必须由主人拍板，小秋不替主人决定。**

---

### 5.7 user 模块

文件：`shiguang-user/shiguang-user-biz/.../user/biz/constant/MQConstants.java`

**完全不用改**——它只有 `TOPIC_DELAY_DELETE_USER_REDIS_CACHE` 一个常量，且是模块私有的。

---

## 六、第三步：拆掉 recommend 的硬编码

recommend 没有 `MQConstants.java`，所以要么新建一个，要么直接引用公共类。**推荐后者**（更直接）。

### 6.1 `UserInterestByLikeConsumer.java`

**改前**（第 26–29 行）：
```java
@RocketMQMessageListener(
        consumerGroup = "shiguang_recommend_group_LikeUnlikeTopic",
        topic = "LikeUnlikeTopic"
)
```

**改后**：
```java
import com.quanshiguang.framework.common.constant.SharedMQConstants;

@RocketMQMessageListener(
        consumerGroup = "shiguang_recommend_group_" + SharedMQConstants.TOPIC_LIKE_OR_UNLIKE,
        topic = SharedMQConstants.TOPIC_LIKE_OR_UNLIKE
)
```

> 注意：注解里的值必须是**编译期常量**。`SharedMQConstants.TOPIC_LIKE_OR_UNLIKE` 是 `static final String` 字面量，字符串拼接 `"前缀" + 常量` 也是编译期常量，**完全合法**。

### 6.2 `UserInterestByCollectConsumer.java`

同理，第 27–29 行：
```java
consumerGroup = "shiguang_recommend_group_" + SharedMQConstants.TOPIC_COLLECT_OR_UN_COLLECT,
topic = SharedMQConstants.TOPIC_COLLECT_OR_UN_COLLECT
```

### 6.3 顺带一提：recommend 的 consumerGroup 命名和别处不一样

全项目有多种 group 命名风格（`shiguang_group_` + topic、`shiguang_group_count_` + topic、`shiguang_recommend_group_` + topic…）。本次**不动**这个，只是让主人知道这不属于"常量共享"问题，不必顺手改。

---

## 七、第四步：怎么验证改对了

**第 1 步 · 确认没有残留的重复定义**（改完后跑）：

```bash
rg -n 'String\s+(TOPIC_NOTE_OPERATE|TOPIC_COUNT_NOTE_LIKE|TOPIC_COUNT_NOTE_COLLECT|TOPIC_LIKE_OR_UNLIKE|TOPIC_COLLECT_OR_UN_COLLECT|TOPIC_PUBLISH_NOTE_TRANSACTION|TOPIC_COUNT_FOLLOWING|TOPIC_COUNT_FANS|TOPIC_COUNT_NOTE_COMMENT|TOPIC_COMMENT_HEAT_UPDATE|TOPIC_COMMENT_LIKE_OR_UNLIKE|TAG_NOTE_PUBLISH|TAG_NOTE_DELETE|TAG_LIKE|TAG_COLLECT|TAG_UN_COLLECT)\s*=' --glob '*.java'
```

**期望结果：只剩 `SharedMQConstants.java` 一处命中**（`TAG_UNLIKE` 若选了"选项 2"，comment 里会多一处，属预期）。

**第 2 步 · 确认没有残留的硬编码字符串**：

```bash
rg -n '"LikeUnlikeTopic"|"CollectUnCollectTopic"' --glob '*.java'
```

**期望结果：只剩 `SharedMQConstants.java` 一处**。

**第 3 步 · 编译**（在项目根目录）：

```bash
mvn -q -DskipTests clean compile
```

只要接口继承写对了，**所有调用点零改动就能通过**。如果报 `找不到符号 TOPIC_XXX`，说明某个模块漏加了 `extends SharedMQConstants`。

**第 4 步 · 行为验证（可选，需起中间件）**：
- 起 note + count + recommend，发一次点赞，看 count 的计数是否 +1、recommend 的日志是否打出 `[用户兴趣]`。
- 因为本次**一个字符串的值都没变**（除 comment 的 `TAG_UNLIKE` 需单独决策），行为应当与改前完全一致。

---

## 八、修改清单速查表

| # | 文件 | 操作 | 预计改动行数 |
|---|---|---|---|
| 1 | `framework/common/constant/SharedMQConstants.java` | **新建** | +95 |
| 2 | `note/biz/constant/MQConstants.java` | 加 extends + import，删 12 个常量 | −80 / +3 |
| 3 | `count/biz/constant/MQConstants.java` | 同上，删 10 个常量 | −55 / +3 |
| 4 | `data-align/constant/MQConstants.java` | 同上，删 4 个常量（变空接口） | −28 / +8 |
| 5 | `kv/biz/constant/MQConstants.java` | 同上，删 1 个常量（变空接口） | −8 / +6 |
| 6 | `user-relation/biz/constant/MQConstants.java` | 同上，删 2 个常量 | −14 / +3 |
| 7 | `comment/biz/constant/MQConstants.java` | 同上，删 4 个常量（**含 TAG_UNLIKE 决策**） | −32 / +3 |
| 8 | `recommend/.../UserInterestByLikeConsumer.java` | 2 行硬编码 → 常量 | ±3 |
| 9 | `recommend/.../UserInterestByCollectConsumer.java` | 2 行硬编码 → 常量 | ±3 |
| — | `user/biz/constant/MQConstants.java` | **不动** | 0 |

**净效果**：删掉约 220 行重复声明，所有调用点零改动，任何 Topic 字符串的值都不变。

---

## 九、关于 C 类（消息 DTO 重复）—— 只提示，本次不动

同一个坏味道还有第二个面：**消息体 DTO 也在各模块复制**。

小秋核实过：

| DTO | 出现位置 | 字段是否一致 |
|---|---|---|
| `LikeUnlikeNoteMqDTO` | note:20–37、recommend:7–13 | **完全一致**（userId / noteId / type / noteCreatorId / createTime），只是 note 那份多了 `@Builder` |
| `CollectUnCollectNoteMqDTO` | note、recommend | 同上模式 |
| `CountLikeUnlikeNoteMqDTO` | count（消费后转发的内部 DTO） | 属 count 内部，不算问题 |

**风险**：note 给 `LikeUnlikeNoteMqDTO` 加一个字段，recommend 那份不会跟着变。如果 recommend 之后也要读那个字段，就会静默拿到 `null`——**编译期毫无提示**。

**修法**（未来可做）：把这类"跨模块消息契约 DTO"和 `SharedMQConstants` 一起放到独立模块（方案 B 的 `shiguang-mq-api`），各模块依赖它。

**为什么本次不做**：它要动 pom 和大量 import，改动面比 A+B 大一个量级。**先把 A+B 做干净，再单独安排 C**，一次只做一件事。

---

## 十、最后：这次改动为什么是安全的

三句话：

1. **一个字符串的值都不变**（comment 的 `TAG_UNLIKE` 除外，且已验证无消费者依赖）。
2. **一个调用点都不改**（靠接口继承让 `MQConstants.XXX` 继续可用）。
3. **改错了编译就会报错**（这正是我们要的效果——把"运行时才炸"变成"编译期就炸"）。

改之前建议先开个分支：`git checkout -b refactor/share-mq-constants`。

---

*本文档由小秋整理，未改动任何源码。所有改动请主人确认后再执行。*
