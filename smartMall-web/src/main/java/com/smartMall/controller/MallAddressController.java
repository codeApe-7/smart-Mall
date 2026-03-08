package com.smartMall.controller;

import com.smartMall.entities.dto.UserAddressSaveDTO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.vo.UserAddressVO;
import com.smartMall.service.MallUserCenterService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端收货地址控制器。
 */
@Slf4j
@RestController
@RequestMapping("/address")
public class MallAddressController {

    @Resource
    private MallUserCenterService mallUserCenterService;

    @GetMapping("/list")
    public ResponseVO<List<UserAddressVO>> list(@RequestParam String userToken) {
        return ResponseVO.success(mallUserCenterService.loadAddressList(userToken));
    }

    @GetMapping("/detail/{addressId}")
    public ResponseVO<UserAddressVO> detail(@RequestParam String userToken, @PathVariable String addressId) {
        return ResponseVO.success(mallUserCenterService.getAddressDetail(userToken, addressId));
    }

    @PostMapping("/save")
    public ResponseVO<Void> save(@RequestParam String userToken, @RequestBody @Valid UserAddressSaveDTO dto) {
        mallUserCenterService.saveAddress(userToken, dto);
        return ResponseVO.success();
    }

    @PostMapping("/delete/{addressId}")
    public ResponseVO<Void> delete(@RequestParam String userToken, @PathVariable String addressId) {
        mallUserCenterService.deleteAddress(userToken, addressId);
        return ResponseVO.success();
    }

    @PostMapping("/default/{addressId}")
    public ResponseVO<Void> setDefault(@RequestParam String userToken, @PathVariable String addressId) {
        mallUserCenterService.setDefaultAddress(userToken, addressId);
        return ResponseVO.success();
    }
}
