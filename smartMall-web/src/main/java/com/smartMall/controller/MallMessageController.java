package com.smartMall.controller;

import com.smartMall.entities.dto.MessageQueryDTO;
import com.smartMall.entities.vo.MallMessageVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.MallMessageCenterService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端消息中心控制器。
 */
@RestController
@RequestMapping("/message")
public class MallMessageController {

    @Resource
    private MallMessageCenterService mallMessageCenterService;

    @PostMapping("/list")
    public ResponseVO<PageResultVO<MallMessageVO>> list(@RequestParam String userToken,
                                                        @RequestBody(required = false) MessageQueryDTO dto) {
        return ResponseVO.success(mallMessageCenterService.loadMessageList(userToken, dto));
    }

    @GetMapping("/detail/{noticeId}")
    public ResponseVO<MallMessageVO> detail(@RequestParam String userToken, @PathVariable String noticeId) {
        return ResponseVO.success(mallMessageCenterService.getMessageDetail(userToken, noticeId));
    }

    @PostMapping("/read/{noticeId}")
    public ResponseVO<Void> read(@RequestParam String userToken, @PathVariable String noticeId) {
        mallMessageCenterService.markRead(userToken, noticeId);
        return ResponseVO.success();
    }

    @GetMapping("/unreadCount")
    public ResponseVO<Integer> unreadCount(@RequestParam String userToken) {
        return ResponseVO.success(mallMessageCenterService.getUnreadCount(userToken));
    }
}
