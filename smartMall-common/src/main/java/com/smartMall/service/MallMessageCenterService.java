package com.smartMall.service;

import com.smartMall.entities.dto.MessageQueryDTO;
import com.smartMall.entities.vo.MallMessageVO;
import com.smartMall.entities.vo.PageResultVO;

/**
 * 用户消息中心 Service。
 */
public interface MallMessageCenterService {

    PageResultVO<MallMessageVO> loadMessageList(String userToken, MessageQueryDTO dto);

    MallMessageVO getMessageDetail(String userToken, String noticeId);

    void markRead(String userToken, String noticeId);

    Integer getUnreadCount(String userToken);
}
