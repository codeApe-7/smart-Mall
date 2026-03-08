package com.smartMall.service;

import com.smartMall.entities.dto.MallUserLoginDTO;
import com.smartMall.entities.dto.MallUserPasswordUpdateDTO;
import com.smartMall.entities.dto.MallUserProfileUpdateDTO;
import com.smartMall.entities.dto.MallUserRegisterDTO;
import com.smartMall.entities.dto.UserAddressSaveDTO;
import com.smartMall.entities.vo.MallUserLoginVO;
import com.smartMall.entities.vo.MallUserProfileVO;
import com.smartMall.entities.vo.UserAddressVO;

import java.util.List;

/**
 * 用户端账号与个人中心 Service。
 */
public interface MallUserCenterService {

    MallUserLoginVO register(MallUserRegisterDTO dto);

    MallUserLoginVO login(MallUserLoginDTO dto);

    MallUserProfileVO getCurrentProfile(String userToken);

    void updateProfile(String userToken, MallUserProfileUpdateDTO dto);

    void updatePassword(String userToken, MallUserPasswordUpdateDTO dto);

    void logout(String userToken);

    List<UserAddressVO> loadAddressList(String userToken);

    UserAddressVO getAddressDetail(String userToken, String addressId);

    void saveAddress(String userToken, UserAddressSaveDTO dto);

    void deleteAddress(String userToken, String addressId);

    void setDefaultAddress(String userToken, String addressId);
}
