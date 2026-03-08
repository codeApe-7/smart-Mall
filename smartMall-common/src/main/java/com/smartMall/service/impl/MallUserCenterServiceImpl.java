package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.component.RedisComponent;
import com.smartMall.entities.constant.Constants;
import com.smartMall.entities.domain.UserAccount;
import com.smartMall.entities.domain.UserDeliveryAddress;
import com.smartMall.entities.dto.MallUserLoginDTO;
import com.smartMall.entities.dto.MallUserPasswordUpdateDTO;
import com.smartMall.entities.dto.MallUserProfileUpdateDTO;
import com.smartMall.entities.dto.MallUserRegisterDTO;
import com.smartMall.entities.dto.UserAddressSaveDTO;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.enums.UserAccountStatusEnum;
import com.smartMall.entities.vo.MallUserLoginVO;
import com.smartMall.entities.vo.MallUserProfileVO;
import com.smartMall.entities.vo.UserAddressVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.MallUserCenterService;
import com.smartMall.service.UserAccountService;
import com.smartMall.service.UserDeliveryAddressService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 用户端账号与个人中心 Service 实现。
 */
@Service
public class MallUserCenterServiceImpl implements MallUserCenterService {

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserDeliveryAddressService userDeliveryAddressService;

    @Resource
    private RedisComponent redisComponent;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallUserLoginVO register(MallUserRegisterDTO dto) {
        checkUsernameUnique(dto.getUsername(), null);
        checkPhoneUnique(dto.getPhone(), null);
        Date now = new Date();
        UserAccount account = new UserAccount();
        account.setUserId(UUID.randomUUID().toString().replace("-", ""));
        account.setUsername(dto.getUsername().trim());
        account.setNickname(StringTools.isNotEmpty(dto.getNickname()) ? dto.getNickname().trim() : dto.getUsername().trim());
        account.setPhone(dto.getPhone().trim());
        account.setPassword(encodePassword(dto.getPassword()));
        account.setStatus(UserAccountStatusEnum.ENABLED.getStatus());
        account.setCreateTime(now);
        account.setUpdateTime(now);
        account.setLastActiveTime(now);
        userAccountService.save(account);
        return buildLoginVO(account, redisComponent.saveUserToken(account.getUserId()));
    }

    @Override
    public MallUserLoginVO login(MallUserLoginDTO dto) {
        UserAccount account = loadByPrincipal(dto.getAccount());
        if (account == null || StringTools.isEmpty(account.getPassword())
                || !Objects.equals(account.getPassword(), encodePassword(dto.getPassword()))) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED, "account or password is invalid");
        }
        if (!Objects.equals(account.getStatus(), UserAccountStatusEnum.ENABLED.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.FORBIDDEN, "user account is disabled");
        }
        Date now = new Date();
        account.setLastActiveTime(now);
        account.setUpdateTime(now);
        userAccountService.updateById(account);
        return buildLoginVO(account, redisComponent.saveUserToken(account.getUserId()));
    }

    @Override
    public MallUserProfileVO getCurrentProfile(String userToken) {
        return buildProfileVO(getCurrentAccount(userToken));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userToken, MallUserProfileUpdateDTO dto) {
        UserAccount account = getCurrentAccount(userToken);
        if (StringTools.isNotEmpty(dto.getPhone()) && !Objects.equals(dto.getPhone().trim(), account.getPhone())) {
            checkPhoneUnique(dto.getPhone(), account.getUserId());
            account.setPhone(dto.getPhone().trim());
        }
        if (StringTools.isNotEmpty(dto.getNickname())) {
            account.setNickname(dto.getNickname().trim());
        }
        if (dto.getAvatar() != null) {
            account.setAvatar(normalizeNullable(dto.getAvatar()));
        }
        account.setUpdateTime(new Date());
        userAccountService.updateById(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String userToken, MallUserPasswordUpdateDTO dto) {
        UserAccount account = getCurrentAccount(userToken);
        if (!Objects.equals(account.getPassword(), encodePassword(dto.getOldPassword()))) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED, "old password is invalid");
        }
        account.setPassword(encodePassword(dto.getNewPassword()));
        account.setUpdateTime(new Date());
        userAccountService.updateById(account);
    }

    @Override
    public void logout(String userToken) {
        if (StringTools.isNotEmpty(userToken)) {
            redisComponent.cleanUserToken(userToken);
        }
    }

    @Override
    public List<UserAddressVO> loadAddressList(String userToken) {
        String userId = getCurrentUserId(userToken);
        return userDeliveryAddressService.list(new LambdaQueryWrapper<UserDeliveryAddress>()
                        .eq(UserDeliveryAddress::getUserId, userId)
                        .orderByDesc(UserDeliveryAddress::getDefaultAddress)
                        .orderByDesc(UserDeliveryAddress::getUpdateTime)
                        .orderByDesc(UserDeliveryAddress::getCreateTime))
                .stream()
                .map(this::buildAddressVO)
                .toList();
    }

    @Override
    public UserAddressVO getAddressDetail(String userToken, String addressId) {
        return buildAddressVO(getAddress(userToken, addressId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAddress(String userToken, UserAddressSaveDTO dto) {
        String userId = getCurrentUserId(userToken);
        Date now = new Date();
        boolean shouldDefault = Boolean.TRUE.equals(dto.getDefaultAddress()) || countUserAddresses(userId) == 0;
        if (shouldDefault) {
            clearDefaultAddress(userId);
        }
        if (StringTools.isEmpty(dto.getAddressId())) {
            UserDeliveryAddress address = new UserDeliveryAddress();
            address.setAddressId(StringTools.getRandomNumber(Constants.LENGTH_32));
            fillAddressFields(address, dto, userId, now);
            address.setCreateTime(now);
            address.setUpdateTime(now);
            address.setDefaultAddress(shouldDefault ? 1 : 0);
            userDeliveryAddressService.save(address);
            return;
        }
        UserDeliveryAddress address = getAddress(userToken, dto.getAddressId());
        fillAddressFields(address, dto, userId, now);
        address.setDefaultAddress(shouldDefault ? 1 : 0);
        address.setUpdateTime(now);
        userDeliveryAddressService.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(String userToken, String addressId) {
        String userId = getCurrentUserId(userToken);
        UserDeliveryAddress address = getAddress(userToken, addressId);
        boolean wasDefault = Objects.equals(address.getDefaultAddress(), 1);
        userDeliveryAddressService.removeById(address.getAddressId());
        if (wasDefault) {
            UserDeliveryAddress nextAddress = userDeliveryAddressService.getOne(new LambdaQueryWrapper<UserDeliveryAddress>()
                    .eq(UserDeliveryAddress::getUserId, userId)
                    .orderByDesc(UserDeliveryAddress::getUpdateTime)
                    .last("LIMIT 1"));
            if (nextAddress != null) {
                nextAddress.setDefaultAddress(1);
                nextAddress.setUpdateTime(new Date());
                userDeliveryAddressService.updateById(nextAddress);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(String userToken, String addressId) {
        String userId = getCurrentUserId(userToken);
        UserDeliveryAddress address = getAddress(userToken, addressId);
        clearDefaultAddress(userId);
        address.setDefaultAddress(1);
        address.setUpdateTime(new Date());
        userDeliveryAddressService.updateById(address);
    }

    private void fillAddressFields(UserDeliveryAddress address, UserAddressSaveDTO dto, String userId, Date now) {
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName().trim());
        address.setReceiverPhone(dto.getReceiverPhone().trim());
        address.setProvince(dto.getProvince().trim());
        address.setCity(dto.getCity().trim());
        address.setRegion(normalizeNullable(dto.getRegion()));
        address.setDetailAddress(dto.getDetailAddress().trim());
        address.setAddressLabel(normalizeNullable(dto.getAddressLabel()));
        if (address.getCreateTime() == null) {
            address.setCreateTime(now);
        }
    }

    private long countUserAddresses(String userId) {
        return userDeliveryAddressService.count(new LambdaQueryWrapper<UserDeliveryAddress>()
                .eq(UserDeliveryAddress::getUserId, userId));
    }

    private void clearDefaultAddress(String userId) {
        List<UserDeliveryAddress> addressList = userDeliveryAddressService.list(new LambdaQueryWrapper<UserDeliveryAddress>()
                .eq(UserDeliveryAddress::getUserId, userId)
                .eq(UserDeliveryAddress::getDefaultAddress, 1));
        if (addressList.isEmpty()) {
            return;
        }
        Date now = new Date();
        addressList.forEach(item -> {
            item.setDefaultAddress(0);
            item.setUpdateTime(now);
        });
        userDeliveryAddressService.updateBatchById(addressList);
    }

    private UserDeliveryAddress getAddress(String userToken, String addressId) {
        String userId = getCurrentUserId(userToken);
        UserDeliveryAddress address = userDeliveryAddressService.getOne(new LambdaQueryWrapper<UserDeliveryAddress>()
                .eq(UserDeliveryAddress::getAddressId, addressId)
                .eq(UserDeliveryAddress::getUserId, userId)
                .last("LIMIT 1"));
        if (address == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "address not found");
        }
        return address;
    }

    private String getCurrentUserId(String userToken) {
        String userId = redisComponent.getUserToken(userToken);
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED, "user token is invalid");
        }
        return userId;
    }

    private UserAccount getCurrentAccount(String userToken) {
        String userId = getCurrentUserId(userToken);
        UserAccount account = userAccountService.getById(userId);
        if (account == null) {
            throw new BusinessException(ResponseCodeEnum.USER_NOT_EXIST, "user account not found");
        }
        return account;
    }

    private UserAccount loadByPrincipal(String principal) {
        String safePrincipal = principal == null ? "" : principal.trim();
        if (safePrincipal.isEmpty()) {
            return null;
        }
        return userAccountService.getOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, safePrincipal)
                .or()
                .eq(UserAccount::getPhone, safePrincipal)
                .last("LIMIT 1"));
    }

    private void checkUsernameUnique(String username, String excludeUserId) {
        UserAccount account = userAccountService.getOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, username.trim())
                .last("LIMIT 1"));
        if (account != null && !Objects.equals(account.getUserId(), excludeUserId)) {
            throw new BusinessException(ResponseCodeEnum.RESOURCE_CONFLICT, "username already exists");
        }
    }

    private void checkPhoneUnique(String phone, String excludeUserId) {
        UserAccount account = userAccountService.getOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getPhone, phone.trim())
                .last("LIMIT 1"));
        if (account != null && !Objects.equals(account.getUserId(), excludeUserId)) {
            throw new BusinessException(ResponseCodeEnum.RESOURCE_CONFLICT, "phone already exists");
        }
    }

    private MallUserLoginVO buildLoginVO(UserAccount account, String userToken) {
        MallUserLoginVO vo = new MallUserLoginVO();
        vo.setUserToken(userToken);
        vo.setProfile(buildProfileVO(account));
        return vo;
    }

    private MallUserProfileVO buildProfileVO(UserAccount account) {
        MallUserProfileVO vo = new MallUserProfileVO();
        vo.setUserId(account.getUserId());
        vo.setUsername(account.getUsername());
        vo.setNickname(account.getNickname());
        vo.setAvatar(account.getAvatar());
        vo.setPhone(account.getPhone());
        vo.setStatus(account.getStatus());
        vo.setStatusDesc(resolveStatusDesc(account.getStatus()));
        vo.setCreateTime(account.getCreateTime());
        vo.setLastActiveTime(account.getLastActiveTime());
        return vo;
    }

    private UserAddressVO buildAddressVO(UserDeliveryAddress address) {
        UserAddressVO vo = new UserAddressVO();
        vo.setAddressId(address.getAddressId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setRegion(address.getRegion());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setFullAddress(buildFullAddress(address));
        vo.setAddressLabel(address.getAddressLabel());
        vo.setDefaultAddress(address.getDefaultAddress());
        vo.setCreateTime(address.getCreateTime());
        vo.setUpdateTime(address.getUpdateTime());
        return vo;
    }

    private String buildFullAddress(UserDeliveryAddress address) {
        StringBuilder builder = new StringBuilder();
        appendSegment(builder, address.getProvince());
        appendSegment(builder, address.getCity());
        appendSegment(builder, address.getRegion());
        appendSegment(builder, address.getDetailAddress());
        return builder.toString();
    }

    private void appendSegment(StringBuilder builder, String value) {
        if (StringTools.isNotEmpty(value)) {
            builder.append(value.trim());
        }
    }

    private String resolveStatusDesc(Integer status) {
        UserAccountStatusEnum statusEnum = UserAccountStatusEnum.getByStatus(status);
        return statusEnum == null ? "未知" : statusEnum.getDesc();
    }

    private String encodePassword(String password) {
        return StringTools.encodeByMd5(password == null ? "" : password.trim());
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
