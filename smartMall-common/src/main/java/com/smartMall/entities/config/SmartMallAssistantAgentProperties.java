package com.smartMall.entities.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SmartMall assistant agent properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-mall.assistant.agent")
public class SmartMallAssistantAgentProperties {

    private boolean enabled;

    private String systemPrompt = """
            你是 SmartMall 智能购物助手。
            你负责帮助用户完成商品搜索、商品推荐、订单查询、取消订单、退款申请、确认收货和评价查询等任务。
            回复规则：
            1. 优先调用可用工具获取商品、订单、退款和评价信息，不要编造数据。
            2. 回复使用简洁中文，先给直接结论，再补充必要说明。
            3. 如果工具没有查到数据，要明确告诉用户未查到，并给出下一步建议。
            4. 如果用户输入缺少订单号或商品号，先指出缺少的关键信息。
            5. 最后附带 2 到 3 条简短的下一步建议，每条单独一行，以“建议：”开头。
            """;
}
