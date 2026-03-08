package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.component.RedisComponent;
import com.smartMall.entities.constant.Constants;
import com.smartMall.entities.domain.SysNoticeMessage;
import com.smartMall.entities.domain.UserNoticeRead;
import com.smartMall.entities.dto.MessageQueryDTO;
import com.smartMall.entities.enums.NoticeMessageTypeEnum;
import com.smartMall.entities.enums.NoticePublishStatusEnum;
import com.smartMall.entities.enums.NoticeTargetTypeEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.MallMessageVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.MallMessageCenterService;
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
 * 用户消息中心 Service 实现。
 */
@Service
public class MallMessageCenterServiceImpl implements MallMessageCenterService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private SysNoticeMessageService sysNoticeMessageService;

    @Resource
    private UserNoticeReadService userNoticeReadService;

    @Override
    public PageResultVO<MallMessageVO> loadMessageList(String userToken, MessageQueryDTO dto) {
        String userId = getCurrentUserId(userToken);
        MessageQueryDTO safeQuery = dto == null ? new MessageQueryDTO() : dto;
        List<SysNoticeMessage> visibleNoticeList = loadVisibleNoticeList(userId, safeQuery.getMessageType());
        if (visibleNoticeList.isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }
        Map<String, UserNoticeRead> readMap = loadReadMap(userId, visibleNoticeList.stream().map(SysNoticeMessage::getNoticeId).toList());
        List<MallMessageVO> messageList = visibleNoticeList.stream()
                .map(item -> buildMessageVO(item, readMap.containsKey(item.getNoticeId()), false))
                .filter(item -> safeQuery.getRead() == null || safeQuery.getRead().equals(item.getRead()))
                .toList();
        return paginate(messageList, safeQuery.getPageNo(), safeQuery.getPageSize());
    }

    @Override
    public MallMessageVO getMessageDetail(String userToken, String noticeId) {
        String userId = getCurrentUserId(userToken);
        SysNoticeMessage notice = getVisibleNotice(userId, noticeId);
        Map<String, UserNoticeRead> readMap = loadReadMap(userId, List.of(noticeId));
        return buildMessageVO(notice, readMap.containsKey(noticeId), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(String userToken, String noticeId) {
        String userId = getCurrentUserId(userToken);
        getVisibleNotice(userId, noticeId);
        UserNoticeRead readRecord = userNoticeReadService.getOne(new LambdaQueryWrapper<UserNoticeRead>()
                .eq(UserNoticeRead::getUserId, userId)
                .eq(UserNoticeRead::getNoticeId, noticeId)
                .last("LIMIT 1"));
        if (readRecord != null) {
            return;
        }
        Date now = new Date();
        UserNoticeRead entity = new UserNoticeRead();
        entity.setReadId(StringTools.getRandomNumber(Constants.LENGTH_32));
        entity.setNoticeId(noticeId);
        entity.setUserId(userId);
        entity.setReadTime(now);
        entity.setCreateTime(now);
        userNoticeReadService.save(entity);
    }

    @Override
    public Integer getUnreadCount(String userToken) {
        String userId = getCurrentUserId(userToken);
        List<SysNoticeMessage> noticeList = loadVisibleNoticeList(userId, null);
        if (noticeList.isEmpty()) {
            return 0;
        }
        Map<String, UserNoticeRead> readMap = loadReadMap(userId, noticeList.stream().map(SysNoticeMessage::getNoticeId).toList());
        return Math.toIntExact(noticeList.stream().filter(item -> !readMap.containsKey(item.getNoticeId())).count());
    }

    private String getCurrentUserId(String userToken) {
        String userId = redisComponent.getUserToken(userToken);
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED, "user token is invalid");
        }
        return userId;
    }

    private List<SysNoticeMessage> loadVisibleNoticeList(String userId, String messageType) {
        return sysNoticeMessageService.list(new LambdaQueryWrapper<SysNoticeMessage>()
                .eq(SysNoticeMessage::getPublishStatus, NoticePublishStatusEnum.PUBLISHED.getStatus())
                .eq(StringTools.isNotEmpty(messageType), SysNoticeMessage::getMessageType, messageType)
                .and(wrapper -> wrapper.eq(SysNoticeMessage::getTargetType, NoticeTargetTypeEnum.ALL_USER.getType())
                        .or(item -> item.eq(SysNoticeMessage::getTargetType, NoticeTargetTypeEnum.SPECIFIED_USER.getType())
                                .eq(SysNoticeMessage::getTargetUserId, userId)))
                .orderByDesc(SysNoticeMessage::getPublishTime)
                .orderByDesc(SysNoticeMessage::getUpdateTime));
    }

    private SysNoticeMessage getVisibleNotice(String userId, String noticeId) {
        SysNoticeMessage notice = sysNoticeMessageService.getOne(new LambdaQueryWrapper<SysNoticeMessage>()
                .eq(SysNoticeMessage::getNoticeId, noticeId)
                .eq(SysNoticeMessage::getPublishStatus, NoticePublishStatusEnum.PUBLISHED.getStatus())
                .and(wrapper -> wrapper.eq(SysNoticeMessage::getTargetType, NoticeTargetTypeEnum.ALL_USER.getType())
                        .or(item -> item.eq(SysNoticeMessage::getTargetType, NoticeTargetTypeEnum.SPECIFIED_USER.getType())
                                .eq(SysNoticeMessage::getTargetUserId, userId)))
                .last("LIMIT 1"));
        if (notice == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "message not found");
        }
        return notice;
    }

    private Map<String, UserNoticeRead> loadReadMap(String userId, List<String> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Map.of();
        }
        return userNoticeReadService.list(new LambdaQueryWrapper<UserNoticeRead>()
                        .eq(UserNoticeRead::getUserId, userId)
                        .in(UserNoticeRead::getNoticeId, noticeIds))
                .stream()
                .collect(Collectors.toMap(UserNoticeRead::getNoticeId, Function.identity(), (left, right) -> left));
    }

    private MallMessageVO buildMessageVO(SysNoticeMessage notice, boolean read, boolean withContent) {
        MallMessageVO vo = new MallMessageVO();
        vo.setNoticeId(notice.getNoticeId());
        vo.setNoticeTitle(notice.getNoticeTitle());
        vo.setNoticeSummary(notice.getNoticeSummary());
        vo.setNoticeContent(withContent ? notice.getNoticeContent() : null);
        vo.setMessageType(notice.getMessageType());
        NoticeMessageTypeEnum typeEnum = NoticeMessageTypeEnum.getByCode(notice.getMessageType());
        vo.setMessageTypeDesc(typeEnum == null ? "未知" : typeEnum.getDesc());
        vo.setRead(read);
        vo.setPublishTime(notice.getPublishTime());
        return vo;
    }

    private PageResultVO<MallMessageVO> paginate(List<MallMessageVO> source, Integer pageNo, Integer pageSize) {
        int currentPage = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int start = (currentPage - 1) * currentSize;
        if (start >= source.size()) {
            return PageResultVO.empty(currentPage, currentSize);
        }
        int end = Math.min(start + currentSize, source.size());
        return new PageResultVO<>(currentPage, currentSize, (long) source.size(), source.subList(start, end));
    }
}
