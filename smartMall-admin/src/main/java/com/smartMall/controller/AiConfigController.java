package com.smartMall.controller;

import com.smartMall.annotation.AdminAuditLog;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.smartMall.entities.dto.AdminAiConfigSaveDTO;
import com.smartMall.entities.vo.AdminAiConfigVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.enums.AdminOperationTypeEnum;
import com.smartMall.service.AiConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin AI config controller.
 */
@RestController
@SaCheckPermission("ai:config")
@RequestMapping("/ai-config")
public class AiConfigController {

    @Resource
    private AiConfigService aiConfigService;

    @GetMapping("/detail")
    public ResponseVO<AdminAiConfigVO> detail() {
        return ResponseVO.success(aiConfigService.getAdminAiConfig());
    }

    @PostMapping("/save")
    @AdminAuditLog(value = "保存 AI 配置", type = AdminOperationTypeEnum.AI)
    public ResponseVO<Void> save(@RequestBody AdminAiConfigSaveDTO dto) {
        aiConfigService.saveAdminAiConfig(dto);
        return ResponseVO.success();
    }
}


