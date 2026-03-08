package com.smartMall.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.smartMall.annotation.AdminAuditLog;
import com.smartMall.entities.constant.Constants;
import com.smartMall.entities.domain.AdminOperationLog;
import com.smartMall.entities.dto.LoginDTO;
import com.smartMall.entities.vo.AdminCurrentAccountVO;
import com.smartMall.service.AdminAuthorityManageService;
import com.smartMall.service.AdminOperationLogService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 后台操作审计切面。
 */
@Slf4j
@Aspect
@Component
public class AdminAuditAspect {

    private static final int SUCCESS_STATUS = 1;
    private static final int FAIL_STATUS = 0;
    private static final int REQUEST_PARAM_MAX_LENGTH = 2000;
    private static final String MASKED_VALUE = "***";

    @Resource
    private AdminOperationLogService adminOperationLogService;

    @Resource
    private AdminAuthorityManageService adminAuthorityManageService;

    @Around("@annotation(com.smartMall.annotation.AdminAuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AdminAuditLog auditLog = methodSignature.getMethod().getAnnotation(AdminAuditLog.class);
        HttpServletRequest request = resolveRequest();
        Object result;
        try {
            result = joinPoint.proceed();
            saveOperationLog(auditLog, request, joinPoint.getArgs(), SUCCESS_STATUS, null);
            return result;
        } catch (Throwable throwable) {
            saveOperationLog(auditLog, request, joinPoint.getArgs(), FAIL_STATUS, throwable.getMessage());
            throw throwable;
        }
    }

    private void saveOperationLog(AdminAuditLog auditLog,
                                  HttpServletRequest request,
                                  Object[] args,
                                  Integer operationStatus,
                                  String errorMessage) {
        AdminOperationLog operationLog = new AdminOperationLog();
        operationLog.setLogId(StringTools.getRandomNumber(Constants.LENGTH_32));
        AdminCurrentAccountVO currentAccount = resolveCurrentAccount();
        operationLog.setAccountId(currentAccount == null ? null : currentAccount.getAccountId());
        operationLog.setAccountName(resolveAccountName(currentAccount, args));
        operationLog.setOperationType(auditLog.type().getCode());
        operationLog.setOperationName(auditLog.value());
        operationLog.setRequestUri(request == null ? null : request.getRequestURI());
        operationLog.setRequestMethod(request == null ? null : request.getMethod());
        operationLog.setRequestParam(buildRequestParam(args));
        operationLog.setOperationStatus(operationStatus);
        operationLog.setErrorMessage(truncate(errorMessage));
        operationLog.setCreateTime(new Date());
        adminOperationLogService.saveSilently(operationLog);
    }

    private String resolveAccountName(AdminCurrentAccountVO currentAccount, Object[] args) {
        if (currentAccount != null && StringTools.isNotEmpty(currentAccount.getAccountName())) {
            return currentAccount.getAccountName();
        }
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof LoginDTO loginDTO) {
                return loginDTO.getAccount();
            }
        }
        return null;
    }

    private String buildRequestParam(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> safeArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null
                    || arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile) {
                continue;
            }
            safeArgs.add(arg);
        }
        if (safeArgs.isEmpty()) {
            return null;
        }
        try {
            return sanitizeAndTruncate(JSON.toJSONString(safeArgs));
        } catch (Exception exception) {
            log.warn("serialize admin audit request args failed", exception);
            return null;
        }
    }

    private String sanitizeAndTruncate(String rawJson) {
        if (StringTools.isEmpty(rawJson)) {
            return rawJson;
        }
        String sanitized = rawJson
                .replaceAll("\\\"password\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\\\"password\\\":\\\"" + MASKED_VALUE + "\\\"")
                .replaceAll("\\\"checkCode\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\\\"checkCode\\\":\\\"" + MASKED_VALUE + "\\\"")
                .replaceAll("\\\"adminToken\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\\\"adminToken\\\":\\\"" + MASKED_VALUE + "\\\"")
                .replaceAll("\\\"userToken\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\\\"userToken\\\":\\\"" + MASKED_VALUE + "\\\"");
        return truncate(sanitized);
    }

    private HttpServletRequest resolveRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private AdminCurrentAccountVO resolveCurrentAccount() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            return null;
        }
        try {
            return adminAuthorityManageService.getCurrentAccount(String.valueOf(loginId));
        } catch (Exception exception) {
            log.debug("resolve current admin account failed, loginId={}", loginId, exception);
            return null;
        }
    }

    private String truncate(String value) {
        if (StringTools.isEmpty(value) || value.length() <= REQUEST_PARAM_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, REQUEST_PARAM_MAX_LENGTH);
    }
}
