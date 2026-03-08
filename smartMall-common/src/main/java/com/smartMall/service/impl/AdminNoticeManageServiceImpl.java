package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartMall.entities.constant.Constants;
import com.smartMall.entities.domain.SysNoticeMessage;
import com.smartMall.entities.domain.UserNoticeRead;
import com.smartMall.entities.dto.AdminNoticeQueryDTO;
import com.smartMall.entities.dto.AdminNoticeSaveDTO;
import com.smartMall.entities.enums.NoticeMessageTypeEnum;
import com.smartMall.entities.enums.NoticePublishStatusEnum;
import com.smartMall.entities.enums.NoticeTargetTypeEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.AdminNoticeDetailVO;
import com.smartMall.entities.vo.AdminNoticeListVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.AdminNoticeManageService;
import com.smartMall.service.SysNoticeMessageService;
import com.smartMall.service.UserNoticeReadService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 后台消息通知管理 Service 实现。
 */
@Service
public class AdminNoticeManageServiceImpl implements AdminNoticeManageService {

    @Resource
    private SysNoticeMessageService sysNoticeMessageService;

    @Resource
    private UserNoticeReadService userNoticeReadService;

    @Override
    public PageResultVO<AdminNoticeListVO> loadNoticeList(AdminNoticeQueryDTO dto) {
        AdminNoticeQueryDTO safeQuery = dto == null ? new AdminNoticeQueryDTO() : dto;
        LambdaQueryWrapper<SysNoticeMessage> queryWrapper = new LambdaQueryWrapper<SysNoticeMessage>()
                .like(StringTools.isNotEmpty(safeQuery.getKeyword()), SysNoticeMessage::getNoticeTitle, safeQuery.getKeyword())
                .eq(StringTools.isNotEmpty(safeQuery.getMessageType()), SysNoticeMessage::getMessageType, safeQuery.getMessageType())
                .eq(safeQuery.getPublishStatus() != null, SysNoticeMessage::getPublishStatus, safeQuery.getPublishStatus())
                .eq(safeQuery.getTargetType() != null, SysNoticeMessage::getTargetType, safeQuery.getTargetType())
                .orderByDesc(SysNoticeMessage::getPublishTime)
                .orderByDesc(SysNoticeMessage::getUpdateTime)
                .orderByDesc(SysNoticeMessage::getCreateTime);
        Page<SysNoticeMessage> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        sysNoticeMessageService.page(page, queryWrapper);
        Map<String, Integer> readCountMap = loadReadCountMap(page.getRecords().stream()
                .map(SysNoticeMessage::getNoticeId)
                .toList());
        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(),
                page.getRecords().stream().map(item -> buildListVO(item, readCountMap)).toList());
    }

    @Override
    public AdminNoticeDetailVO getNoticeDetail(String noticeId) {
        SysNoticeMessage noticeMessage = getNotice(noticeId);
        return buildDetailVO(noticeMessage, loadReadCountMap(List.of(noticeId)).getOrDefault(noticeId, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNotice(AdminNoticeSaveDTO dto) {
        validateSaveDTO(dto);
        Date now = new Date();
        NoticeTargetTypeEnum targetTypeEnum = resolveTargetType(dto.getTargetType());
        if (StringTools.isEmpty(dto.getNoticeId())) {
            SysNoticeMessage noticeMessage = new SysNoticeMessage();
            noticeMessage.setNoticeId(StringTools.getRandomNumber(Constants.LENGTH_32));
            fillNotice(noticeMessage, dto, targetTypeEnum, now);
            noticeMessage.setPublishStatus(NoticePublishStatusEnum.DRAFT.getStatus());
            noticeMessage.setCreateTime(now);
            noticeMessage.setUpdateTime(now);
            sysNoticeMessageService.save(noticeMessage);
            return;
        }
        SysNoticeMessage noticeMessage = getNotice(dto.getNoticeId());
        fillNotice(noticeMessage, dto, targetTypeEnum, now);
        noticeMessage.setUpdateTime(now);
        sysNoticeMessageService.updateById(noticeMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(String noticeId) {
        SysNoticeMessage noticeMessage = getNotice(noticeId);
        if (NoticePublishStatusEnum.PUBLISHED.getStatus().equals(noticeMessage.getPublishStatus())) {
            return;
        }
        noticeMessage.setPublishStatus(NoticePublishStatusEnum.PUBLISHED.getStatus());
        noticeMessage.setPublishTime(new Date());
        noticeMessage.setUpdateTime(new Date());
        sysNoticeMessageService.updateById(noticeMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineNotice(String noticeId) {
        SysNoticeMessage noticeMessage = getNotice(noticeId);
        if (NoticePublishStatusEnum.OFFLINE.getStatus().equals(noticeMessage.getPublishStatus())) {
            return;
        }
        noticeMessage.setPublishStatus(NoticePublishStatusEnum.OFFLINE.getStatus());
        noticeMessage.setUpdateTime(new Date());
        sysNoticeMessageService.updateById(noticeMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(String noticeId) {
        getNotice(noticeId);
        sysNoticeMessageService.removeById(noticeId);
        userNoticeReadService.remove(new LambdaQueryWrapper<UserNoticeRead>()
                .eq(UserNoticeRead::getNoticeId, noticeId));
    }

    private void validateSaveDTO(AdminNoticeSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "notice data is required");
        }
        if (NoticeMessageTypeEnum.getByCode(dto.getMessageType()) == null) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "messageType is invalid");
        }
        NoticeTargetTypeEnum targetTypeEnum = resolveTargetType(dto.getTargetType());
        if (NoticeTargetTypeEnum.SPECIFIED_USER == targetTypeEnum && StringTools.isEmpty(dto.getTargetUserId())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "targetUserId is required");
        }
    }

    private NoticeTargetTypeEnum resolveTargetType(Integer targetType) {
        Integer safeTargetType = targetType == null ? NoticeTargetTypeEnum.ALL_USER.getType() : targetType;
        NoticeTargetTypeEnum targetTypeEnum = NoticeTargetTypeEnum.getByType(safeTargetType);
        if (targetTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "targetType is invalid");
        }
        return targetTypeEnum;
    }

    private SysNoticeMessage getNotice(String noticeId) {
        if (StringTools.isEmpty(noticeId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "noticeId is required");
        }
        SysNoticeMessage noticeMessage = sysNoticeMessageService.getById(noticeId);
        if (noticeMessage == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "notice not found");
        }
        return noticeMessage;
    }

    private void fillNotice(SysNoticeMessage noticeMessage,
                            AdminNoticeSaveDTO dto,
                            NoticeTargetTypeEnum targetTypeEnum,
                            Date now) {
        noticeMessage.setNoticeTitle(dto.getNoticeTitle().trim());
        noticeMessage.setNoticeSummary(normalizeNullable(dto.getNoticeSummary()));
        noticeMessage.setNoticeContent(dto.getNoticeContent().trim());
        noticeMessage.setMessageType(dto.getMessageType().trim());
        noticeMessage.setTargetType(targetTypeEnum.getType());
        noticeMessage.setTargetUserId(targetTypeEnum == NoticeTargetTypeEnum.ALL_USER
                ? null : dto.getTargetUserId().trim());
        noticeMessage.setUpdateTime(now);
    }

    private String normalizeNullable(String value) {
        if (StringTools.isEmpty(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, Integer> loadReadCountMap(List<String> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Map.of();
        }
        return userNoticeReadService.list(new LambdaQueryWrapper<UserNoticeRead>()
                        .in(UserNoticeRead::getNoticeId, noticeIds))
                .stream()
                .collect(Collectors.groupingBy(UserNoticeRead::getNoticeId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private AdminNoticeListVO buildListVO(SysNoticeMessage item, Map<String, Integer> readCountMap) {
        AdminNoticeListVO vo = new AdminNoticeListVO();
        vo.setNoticeId(item.getNoticeId());
        vo.setNoticeTitle(item.getNoticeTitle());
        vo.setNoticeSummary(item.getNoticeSummary());
        vo.setMessageType(item.getMessageType());
        vo.setMessageTypeDesc(resolveMessageTypeDesc(item.getMessageType()));
        vo.setTargetType(item.getTargetType());
        vo.setTargetTypeDesc(resolveTargetTypeDesc(item.getTargetType()));
        vo.setTargetUserId(item.getTargetUserId());
        vo.setPublishStatus(item.getPublishStatus());
        vo.setPublishStatusDesc(resolvePublishStatusDesc(item.getPublishStatus()));
        vo.setReadUserCount(readCountMap.getOrDefault(item.getNoticeId(), 0));
        vo.setPublishTime(item.getPublishTime());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }

    private AdminNoticeDetailVO buildDetailVO(SysNoticeMessage item, Integer readUserCount) {
        AdminNoticeDetailVO vo = new AdminNoticeDetailVO();
        vo.setNoticeId(item.getNoticeId());
        vo.setNoticeTitle(item.getNoticeTitle());
        vo.setNoticeSummary(item.getNoticeSummary());
        vo.setNoticeContent(item.getNoticeContent());
        vo.setMessageType(item.getMessageType());
        vo.setMessageTypeDesc(resolveMessageTypeDesc(item.getMessageType()));
        vo.setTargetType(item.getTargetType());
        vo.setTargetTypeDesc(resolveTargetTypeDesc(item.getTargetType()));
        vo.setTargetUserId(item.getTargetUserId());
        vo.setPublishStatus(item.getPublishStatus());
        vo.setPublishStatusDesc(resolvePublishStatusDesc(item.getPublishStatus()));
        vo.setReadUserCount(readUserCount);
        vo.setPublishTime(item.getPublishTime());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }

    private String resolveMessageTypeDesc(String messageType) {
        NoticeMessageTypeEnum typeEnum = NoticeMessageTypeEnum.getByCode(messageType);
        return typeEnum == null ? null : typeEnum.getDesc();
    }

    private String resolveTargetTypeDesc(Integer targetType) {
        NoticeTargetTypeEnum targetTypeEnum = NoticeTargetTypeEnum.getByType(targetType);
        return targetTypeEnum == null ? null : targetTypeEnum.getDesc();
    }

    private String resolvePublishStatusDesc(Integer publishStatus) {
        NoticePublishStatusEnum publishStatusEnum = NoticePublishStatusEnum.getByStatus(publishStatus);
        return publishStatusEnum == null ? null : publishStatusEnum.getDesc();
    }
}
