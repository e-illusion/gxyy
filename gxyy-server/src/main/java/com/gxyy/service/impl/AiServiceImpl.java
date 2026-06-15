package com.gxyy.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.gxyy.common.BusinessException;
import com.gxyy.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${gxyy.ai.deepseek.api-key}")
    private String apiKey;

    @Value("${gxyy.ai.deepseek.api-url}")
    private String apiUrl;

    @Override
    public String polishDescription(String description) {
        if (StrUtil.isBlank(apiKey) || "your-deepseek-api-key".equals(apiKey)) {
            throw new BusinessException("AI服务未配置，请在 application.yml 中配置 DeepSeek API Key");
        }

        String prompt = "请帮我润色以下以物易物物品的描述，使其更加生动吸引人、内容更丰富。保留原文的主要信息，直接输出润色后的文本，不要加任何说明：\n\n" + description;

        try {
            JSONObject message = new JSONObject();
            message.set("role", "user");
            message.set("content", prompt);

            JSONArray messages = new JSONArray();
            messages.add(message);

            JSONObject requestBody = new JSONObject();
            requestBody.set("model", "deepseek-chat");
            requestBody.set("messages", messages);
            requestBody.set("max_tokens", 1000);
            requestBody.set("temperature", 0.8);

            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute();

            if (response.getStatus() != 200) {
                log.error("DeepSeek API 调用失败: {} {}", response.getStatus(), response.body());
                throw new BusinessException("AI服务调用失败，请稍后重试");
            }

            JSONObject result = new JSONObject(response.body());
            JSONArray choices = result.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException("AI返回结果为空");
            }
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject msg = firstChoice.getJSONObject("message");
            String polished = msg.getStr("content");
            if (StrUtil.isBlank(polished)) {
                throw new BusinessException("AI返回结果为空");
            }

            return polished.trim();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 DeepSeek API 异常", e);
            throw new BusinessException("AI服务异常，请稍后重试");
        }
    }
}
