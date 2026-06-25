package com.gxyy.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxyy.common.BusinessException;
import com.gxyy.entity.Category;
import com.gxyy.entity.Item;
import com.gxyy.mapper.CategoryMapper;
import com.gxyy.mapper.ItemMapper;
import com.gxyy.service.AiService;
import com.gxyy.vo.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${gxyy.ai.deepseek.api-key}")
    private String apiKey;

    @Value("${gxyy.ai.deepseek.api-url}")
    private String apiUrl;

    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;

    public AiServiceImpl(ItemMapper itemMapper, CategoryMapper categoryMapper) {
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public String polishDescription(String description) {
        if (StrUtil.isBlank(apiKey) || "your-deepseek-api-key".equals(apiKey)) {
            throw new BusinessException("AI服务未配置，请在 application.yml 中配置 DeepSeek API Key");
        }

        String prompt = "请帮我润色以下以物易物物品的描述，使其更加生动吸引人、内容更丰富。保留原文的主要信息，直接输出润色后的文本，不要加任何说明：\n\n" + description;

        return callDeepSeek(prompt);
    }

    @Override
    public List<MatchResult> matchItems(Long itemId, Long userId) {
        if (StrUtil.isBlank(apiKey) || "your-deepseek-api-key".equals(apiKey)) {
            throw new BusinessException("AI服务未配置");
        }

        // 1. 查源物品
        Item sourceItem = itemMapper.selectById(itemId);
        if (sourceItem == null) {
            throw new BusinessException("物品不存在");
        }
        Category sourceCat = categoryMapper.selectById(sourceItem.getCategoryId());

        // 2. 查候选池：ACTIVE + 非本人 + 最多100条（后续关键词筛选到20条）
        List<Item> pool = itemMapper.selectList(
            new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "ACTIVE")
                .ne(Item::getOwnerId, userId)
                .last("LIMIT 100")
        );

        if (pool.isEmpty()) {
            throw new BusinessException("当前没有可匹配的物品");
        }

        // ── 优化1: 关键词预筛选 ──
        // 从候选池中按关键词匹配度打分排序，取前20条
        List<Item> candidates = rankByKeywords(sourceItem, pool);

        log.info("AI Match: total pool={}, keyword-filtered={}",
                 pool.size(), candidates.size());

        // 3. 构建 prompt
        String sourceInfo = String.format(
            "【我的物品】\n  标题：%s\n  分类：%s\n  描述：%s\n  ★ 我想换什么：%s",
            ellipsis(sourceItem.getTitle(), 60),
            sourceCat != null ? sourceCat.getName() : "未知",
            ellipsis(sourceItem.getDescription(), 120),
            sourceItem.getWantDescription() != null ? sourceItem.getWantDescription() : "无"
        );

        StringBuilder candidateList = new StringBuilder();
        for (Item c : candidates) {
            Category cat = categoryMapper.selectById(c.getCategoryId());
            candidateList.append(String.format(
                "{\"id\":%d,\"title\":\"%s\",\"cat\":\"%s\",\"desc\":\"%s\",\"want\":\"%s\"}\n",
                c.getId(),
                escapeJson(ellipsis(c.getTitle(), 60)),
                cat != null ? escapeJson(cat.getName()) : "未知",
                escapeJson(ellipsis(c.getDescription(), 100)),
                escapeJson(ellipsis(c.getWantDescription(), 100))
            ));
        }

        // ── 优化2+3: 改进 Prompt + 双向匹配 ──
        String prompt = String.format(
            "你是一个以物易物匹配助手。请根据以下双向匹配规则，从候选列表中推荐最匹配的3个交换对象。\n\n" +
            "%s\n\n" +
            "候选列表：\n%s\n\n" +
            "【匹配规则（按重要性排序）】\n" +
            "1. 双向需求契合（最重要）：候选物品【描述/标题】能否满足我的【想换什么】？" +
            "同时我的物品【描述/标题】能否满足候选物品的【想换什么】？双向都满足的优先。\n" +
            "2. 物品类型相近：分类相同或功能互补的物品更容易交换成功。\n" +
            "3. 价值相当：新旧程度、物品档次相近的优先。\n\n" +
            "请严格只返回JSON数组（不要markdown代码块，不要任何解释），" +
            "最多3个元素，按匹配度从高到低排序，" +
            "每个元素含itemId和reason(20字内，说明为什么匹配)：\n" +
            "[{\"itemId\":1,\"reason\":\"你的风扇正好是我想要的夏日好物，我的巧克力也满足你的零食需求\"}]",
            sourceInfo,
            candidateList.toString()
        );

        log.info("AI Match prompt length: {} chars, candidates: {}", prompt.length(), candidates.size());

        // 4. 调用 DeepSeek
        String response = callDeepSeek(prompt);

        // 5. 解析响应
        try {
            // 兼容 markdown 代码块
            String json = response.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            }
            JSONArray arr = new JSONArray(json);
            List<MatchResult> results = new ArrayList<>();
            for (int i = 0; i < Math.min(arr.size(), 3); i++) {
                JSONObject obj = arr.getJSONObject(i);
                MatchResult mr = new MatchResult();
                mr.setItemId(obj.getLong("itemId"));
                mr.setReason(obj.getStr("reason"));
                results.add(mr);
            }
            return results;
        } catch (Exception e) {
            log.error("解析AI匹配结果失败: {}", response, e);
            throw new BusinessException("AI返回结果解析失败，请稍后重试");
        }
    }

    // ══════════════════════════════════════
    // 关键词预筛选：双向关键词匹配打分 + 排序
    // ══════════════════════════════════════
    private List<Item> rankByKeywords(Item source, List<Item> pool) {
        List<String> sourceWantKw = extractKeywords(source.getWantDescription());
        String sourceText = normalize(source.getTitle() + " " + source.getDescription());

        // 为每个候选物品计算双向匹配分
        List<ScoredItem> scored = new ArrayList<>();
        for (Item candidate : pool) {
            int score = 0;
            String candText = normalize(
                candidate.getTitle() + " " +
                candidate.getDescription() + " " +
                candidate.getWantDescription()
            );

            // 方向1: 源物品"想换什么"的关键词在候选物品文本中命中
            for (String kw : sourceWantKw) {
                if (candText.contains(kw)) {
                    score += 4; // 权重最高：满足我的需求
                }
            }

            // 方向2: 候选物品"想换什么"的关键词在源物品文本中命中
            List<String> candWantKw = extractKeywords(candidate.getWantDescription());
            for (String kw : candWantKw) {
                if (sourceText.contains(kw)) {
                    score += 4; // 权重最高：满足对方需求
                }
            }

            // 方向3: 源物品描述关键词和候选物品描述关键词的交叉命中
            List<String> sourceDescKw = extractKeywords(source.getDescription());
            for (String kw : sourceDescKw) {
                if (candText.contains(kw)) {
                    score += 1; // 低权重：物品类型相近
                }
            }

            // 分类相同加分
            if (source.getCategoryId().equals(candidate.getCategoryId())) {
                score += 2;
            }

            if (score > 0) {
                scored.add(new ScoredItem(candidate, score));
            }
        }

        // 按分数降序排列，取前 20；如果有效候选不足 20，用所有候选补齐
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        List<Item> result = scored.stream()
                .limit(20)
                .map(s -> s.item)
                .collect(Collectors.toList());

        // 如果关键词筛选后不足3条，从原始池中补充（排除已选的）
        if (result.size() < 3) {
            Set<Long> existing = result.stream().map(Item::getId).collect(Collectors.toSet());
            for (Item item : pool) {
                if (result.size() >= 10) break;
                if (!existing.contains(item.getId())) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    /** 中文关键词提取：分词 + 2-char bigram */
    private List<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) return List.of();
        String cleaned = normalize(text);

        Set<String> keywords = new LinkedHashSet<>();

        // 提取 2-char bigrams（中文最有效的局部匹配方式）
        for (int i = 0; i < cleaned.length() - 1; i++) {
            String bigram = cleaned.substring(i, i + 2);
            // 过滤纯数字/符号
            if (bigram.matches(".*[\\d\\s\\p{Punct}].*")) continue;
            keywords.add(bigram);
        }

        // 同时保留完整词语片段（用常见分隔符切分后 2-8 字的片段）
        String[] parts = text.split("[，。、；！？\\n：\"\"''《》（）\\[\\]\\s,.;:!?'\"()\\-]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.length() >= 2 && trimmed.length() <= 8) {
                keywords.add(trimmed);
            }
        }

        return new ArrayList<>(keywords);
    }

    /** 文本归一化：去标点、转小写 */
    private String normalize(String text) {
        if (text == null) return "";
        return text.replaceAll("[，。、；！？\\n：\"\"''《》（）\\[\\]\\s,.;:!?'\"()\\-]+", "")
                   .toLowerCase();
    }

    /** 评分包装类 */
    private static class ScoredItem {
        final Item item;
        final int score;
        ScoredItem(Item item, int score) { this.item = item; this.score = score; }
    }

    // ══════════════════════════════════════
    // DeepSeek API 调用
    // ══════════════════════════════════════

    private String callDeepSeek(String prompt) {
        try {
            JSONObject message = new JSONObject();
            message.set("role", "user");
            message.set("content", prompt);

            JSONArray messages = new JSONArray();
            messages.add(message);

            JSONObject requestBody = new JSONObject();
            requestBody.set("model", "deepseek-chat");
            requestBody.set("messages", messages);
            requestBody.set("max_tokens", 500);
            requestBody.set("temperature", 0.5);

            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute();

            if (response.getStatus() != 200) {
                log.error("DeepSeek API error: {} {}", response.getStatus(), response.body());
                throw new BusinessException("AI服务调用失败");
            }

            JSONObject result = new JSONObject(response.body());
            JSONArray choices = result.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException("AI返回空");
            }
            String content = choices.getJSONObject(0).getJSONObject("message").getStr("content");
            if (StrUtil.isBlank(content)) {
                throw new BusinessException("AI返回空");
            }
            return content.trim();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek API异常", e);
            throw new BusinessException("AI服务异常");
        }
    }

    // ══════════════════════════════════════
    // 工具方法
    // ══════════════════════════════════════

    private String ellipsis(String text, int maxLen) {
        if (text == null || text.isEmpty()) return "无";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                   .replace("\n", " ").replace("\r", " ");
    }
}
