package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.config.AppConfig;
import com.smartMall.entities.constant.Constants;
import com.smartMall.entities.domain.SysAdminAccount;
import com.smartMall.entities.domain.SysAdminAccountRole;
import com.smartMall.entities.domain.SysAdminRole;
import com.smartMall.entities.dto.AdminAccountPasswordDTO;
import com.smartMall.entities.dto.AdminAccountQueryDTO;
import com.smartMall.entities.dto.AdminAccountSaveDTO;
import com.smartMall.entities.dto.AdminAccountStatusDTO;
import com.smartMall.entities.dto.AdminRoleQueryDTO;
import com.smartMall.entities.dto.AdminRoleSaveDTO;
import com.smartMall.entities.dto.AdminRoleStatusDTO;
import com.smartMall.entities.enums.AdminAccountStatusEnum;
import com.smartMall.entities.enums.AdminPermissionEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.AdminAccountDetailVO;
import com.smartMall.entities.vo.AdminAccountListVO;
import com.smartMall.entities.vo.AdminCurrentAccountVO;
import com.smartMall.entities.vo.AdminPermissionGroupVO;
import com.smartMall.entities.vo.AdminPermissionVO;
import com.smartMall.entities.vo.AdminRoleVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.AdminAuthorityManageService;
import com.smartMall.service.SysAdminAccountRoleService;
import com.smartMall.service.SysAdminAccountService;
import com.smartMall.service.SysAdminRoleService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 后台账户与权限管理 Service 实现。
 */
@Service
public class AdminAuthorityManageServiceImpl implements AdminAuthorityManageService {

    private static final Pattern MD5_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}$");
    private static final String CONFIG_ADMIN_ID = "CONFIG_ADMIN";

    @Resource
    private SysAdminAccountService sysAdminAccountService;

    @Resource
    private SysAdminRoleService sysAdminRoleService;

    @Resource
    private SysAdminAccountRoleService sysAdminAccountRoleService;

    @Resource
    private AppConfig appConfig;

    @Override
    public PageResultVO<AdminAccountListVO> loadAccountList(AdminAccountQueryDTO dto) {
        AdminAccountQueryDTO safeQuery = dto == null ? new AdminAccountQueryDTO() : dto;
        List<SysAdminAccount> accountList = sysAdminAccountService.list(new LambdaQueryWrapper<SysAdminAccount>()
                .orderByDesc(SysAdminAccount::getUpdateTime)
                .orderByDesc(SysAdminAccount::getCreateTime));
        if (accountList.isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }

        Map<String, List<SysAdminRole>> accountRoleMap = loadRoleMapByAccountIds(accountList.stream()
                .map(SysAdminAccount::getAccountId)
                .toList());
        List<AdminAccountListVO> records = accountList.stream()
                .filter(account -> matchAccount(account, accountRoleMap.getOrDefault(account.getAccountId(), List.of()), safeQuery))
                .map(account -> buildAccountListVO(account, accountRoleMap.getOrDefault(account.getAccountId(), List.of())))
                .toList();
        return paginate(records, safeQuery.getPageNo(), safeQuery.getPageSize());
    }

    @Override
    public AdminAccountDetailVO getAccountDetail(String accountId) {
        SysAdminAccount account = getAdminAccount(accountId);
        return buildAccountDetailVO(account, loadRolesByAccountId(accountId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAccount(AdminAccountSaveDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getAccountName())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "accountName is required");
        }
        AdminAccountStatusEnum statusEnum = resolveStatusEnum(dto.getStatus());
        validateRoleIds(dto.getRoleIds());

        String accountName = dto.getAccountName().trim();
        Date now = new Date();
        SysAdminAccount duplicate = loadAccountByName(accountName);
        if (StringTools.isEmpty(dto.getAccountId())) {
            if (duplicate != null) {
                throw new BusinessException(ResponseCodeEnum.RESOURCE_CONFLICT, "account name already exists");
            }
            if (StringTools.isEmpty(dto.getPassword())) {
                throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "password is required");
            }
            SysAdminAccount account = new SysAdminAccount();
            account.setAccountId(StringTools.getRandomNumber(Constants.LENGTH_32));
            account.setAccountName(accountName);
            account.setPassword(normalizePassword(dto.getPassword()));
            account.setNickname(resolveNickname(dto.getNickname(), accountName));
            account.setPhone(normalizeNullable(dto.getPhone()));
            account.setEmail(normalizeNullable(dto.getEmail()));
            account.setStatus(statusEnum.getStatus());
            account.setSuperAdmin(Boolean.TRUE.equals(dto.getSuperAdmin()) ? 1 : 0);
            account.setRemark(normalizeNullable(dto.getRemark()));
            account.setCreateTime(now);
            account.setUpdateTime(now);
            sysAdminAccountService.save(account);
            replaceAccountRoles(account.getAccountId(), dto.getRoleIds(), now);
            return;
        }

        SysAdminAccount account = getAdminAccount(dto.getAccountId());
        if (duplicate != null && !Objects.equals(duplicate.getAccountId(), account.getAccountId())) {
            throw new BusinessException(ResponseCodeEnum.RESOURCE_CONFLICT, "account name already exists");
        }
        account.setAccountName(accountName);
        account.setNickname(resolveNickname(dto.getNickname(), accountName));
        account.setPhone(normalizeNullable(dto.getPhone()));
        account.setEmail(normalizeNullable(dto.getEmail()));
        account.setStatus(statusEnum.getStatus());
        account.setSuperAdmin(Boolean.TRUE.equals(dto.getSuperAdmin()) ? 1 : 0);
        account.setRemark(normalizeNullable(dto.getRemark()));
        if (StringTools.isNotEmpty(dto.getPassword())) {
            account.setPassword(normalizePassword(dto.getPassword()));
        }
        account.setUpdateTime(now);
        sysAdminAccountService.updateById(account);
        replaceAccountRoles(account.getAccountId(), dto.getRoleIds(), now);
    }

    @Override
    public void updateAccountStatus(AdminAccountStatusDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getAccountId())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "accountId is required");
        }
        SysAdminAccount account = getAdminAccount(dto.getAccountId());
        account.setStatus(resolveStatusEnum(dto.getStatus()).getStatus());
        account.setUpdateTime(new Date());
        sysAdminAccountService.updateById(account);
    }

    @Override
    public void resetPassword(AdminAccountPasswordDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getAccountId())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "accountId is required");
        }
        if (StringTools.isEmpty(dto.getPassword())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "password is required");
        }
        SysAdminAccount account = getAdminAccount(dto.getAccountId());
        account.setPassword(normalizePassword(dto.getPassword()));
        account.setUpdateTime(new Date());
        sysAdminAccountService.updateById(account);
    }

    @Override
    public PageResultVO<AdminRoleVO> loadRoleList(AdminRoleQueryDTO dto) {
        AdminRoleQueryDTO safeQuery = dto == null ? new AdminRoleQueryDTO() : dto;
        List<SysAdminRole> roleList = sysAdminRoleService.list(new LambdaQueryWrapper<SysAdminRole>()
                .orderByDesc(SysAdminRole::getUpdateTime)
                .orderByDesc(SysAdminRole::getCreateTime));
        if (roleList.isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }

        Map<String, Long> accountCountMap = loadAccountCountMap(roleList.stream().map(SysAdminRole::getRoleId).toList());
        List<AdminRoleVO> records = roleList.stream()
                .filter(role -> matchRole(role, safeQuery))
                .map(role -> buildRoleVO(role, accountCountMap.getOrDefault(role.getRoleId(), 0L)))
                .toList();
        return paginate(records, safeQuery.getPageNo(), safeQuery.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(AdminRoleSaveDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getRoleCode())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "roleCode is required");
        }
        if (StringTools.isEmpty(dto.getRoleName())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "roleName is required");
        }
        String roleCode = dto.getRoleCode().trim();
        Date now = new Date();
        SysAdminRole duplicate = loadRoleByCode(roleCode);
        List<String> permissionCodes = normalizePermissionCodes(dto.getPermissionCodes());
        AdminAccountStatusEnum statusEnum = resolveStatusEnum(dto.getStatus());

        if (StringTools.isEmpty(dto.getRoleId())) {
            if (duplicate != null) {
                throw new BusinessException(ResponseCodeEnum.RESOURCE_CONFLICT, "role code already exists");
            }
            SysAdminRole role = new SysAdminRole();
            role.setRoleId(StringTools.getRandomNumber(Constants.LENGTH_32));
            role.setRoleCode(roleCode);
            role.setRoleName(dto.getRoleName().trim());
            role.setPermissionCodes(String.join(",", permissionCodes));
            role.setStatus(statusEnum.getStatus());
            role.setRemark(normalizeNullable(dto.getRemark()));
            role.setCreateTime(now);
            role.setUpdateTime(now);
            sysAdminRoleService.save(role);
            return;
        }

        SysAdminRole role = getRole(dto.getRoleId());
        if (duplicate != null && !Objects.equals(duplicate.getRoleId(), role.getRoleId())) {
            throw new BusinessException(ResponseCodeEnum.RESOURCE_CONFLICT, "role code already exists");
        }
        role.setRoleCode(roleCode);
        role.setRoleName(dto.getRoleName().trim());
        role.setPermissionCodes(String.join(",", permissionCodes));
        role.setStatus(statusEnum.getStatus());
        role.setRemark(normalizeNullable(dto.getRemark()));
        role.setUpdateTime(now);
        sysAdminRoleService.updateById(role);
    }

    @Override
    public void updateRoleStatus(AdminRoleStatusDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getRoleId())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "roleId is required");
        }
        SysAdminRole role = getRole(dto.getRoleId());
        role.setStatus(resolveStatusEnum(dto.getStatus()).getStatus());
        role.setUpdateTime(new Date());
        sysAdminRoleService.updateById(role);
    }

    @Override
    public List<AdminPermissionGroupVO> listPermissionGroups() {
        Map<String, AdminPermissionGroupVO> groupMap = new LinkedHashMap<>();
        for (AdminPermissionEnum item : AdminPermissionEnum.values()) {
            AdminPermissionGroupVO groupVO = groupMap.computeIfAbsent(item.getGroupCode(), key -> {
                AdminPermissionGroupVO value = new AdminPermissionGroupVO();
                value.setGroupCode(item.getGroupCode());
                value.setGroupName(item.getGroupName());
                value.setPermissions(new ArrayList<>());
                return value;
            });
            AdminPermissionVO permissionVO = new AdminPermissionVO();
            permissionVO.setCode(item.getCode());
            permissionVO.setName(item.getName());
            permissionVO.setDescription(item.getDescription());
            groupVO.getPermissions().add(permissionVO);
        }
        return new ArrayList<>(groupMap.values());
    }

    @Override
    public String authenticate(String accountName, String password) {
        if (StringTools.isEmpty(accountName) || StringTools.isEmpty(password)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "account and password are required");
        }
        SysAdminAccount account = loadAccountByName(accountName.trim());
        if (account != null) {
            if (!Objects.equals(account.getStatus(), AdminAccountStatusEnum.ENABLED.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.FORBIDDEN, "admin account is disabled");
            }
            if (!matchesPassword(account.getPassword(), password)) {
                throw new BusinessException("账号或密码错误");
            }
            Date now = new Date();
            account.setLastLoginTime(now);
            account.setUpdateTime(now);
            sysAdminAccountService.updateById(account);
            return account.getAccountId();
        }
        if (accountName.trim().equalsIgnoreCase(appConfig.getAdminAccount())
                && matchesPassword(normalizePassword(appConfig.getAdminPassword()), password)) {
            return appConfig.getAdminAccount();
        }
        throw new BusinessException("账号或密码错误");
    }

    @Override
    public AdminCurrentAccountVO getCurrentAccount(String principal) {
        if (StringTools.isEmpty(principal)) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED, "admin token is invalid");
        }
        SysAdminAccount account = sysAdminAccountService.getById(principal);
        if (account == null) {
            account = loadAccountByName(principal);
        }
        if (account != null) {
            return buildCurrentAccountVO(account, loadRolesByAccountId(account.getAccountId()));
        }
        if (principal.equalsIgnoreCase(appConfig.getAdminAccount())) {
            return buildConfigAdminProfile();
        }
        throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED, "admin token is invalid");
    }

    private void validateRoleIds(List<String> roleIds) {
        List<String> normalizedRoleIds = normalizeIdList(roleIds);
        if (normalizedRoleIds.isEmpty()) {
            return;
        }
        long matchedCount = sysAdminRoleService.count(new LambdaQueryWrapper<SysAdminRole>()
                .in(SysAdminRole::getRoleId, normalizedRoleIds));
        if (matchedCount != normalizedRoleIds.size()) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "role not found");
        }
    }

    private SysAdminAccount getAdminAccount(String accountId) {
        if (StringTools.isEmpty(accountId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "accountId is required");
        }
        SysAdminAccount account = sysAdminAccountService.getById(accountId);
        if (account == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "admin account not found");
        }
        return account;
    }

    private SysAdminRole getRole(String roleId) {
        if (StringTools.isEmpty(roleId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "roleId is required");
        }
        SysAdminRole role = sysAdminRoleService.getById(roleId);
        if (role == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "admin role not found");
        }
        return role;
    }

    private SysAdminAccount loadAccountByName(String accountName) {
        if (StringTools.isEmpty(accountName)) {
            return null;
        }
        return sysAdminAccountService.getOne(new LambdaQueryWrapper<SysAdminAccount>()
                .eq(SysAdminAccount::getAccountName, accountName.trim())
                .last("LIMIT 1"));
    }

    private SysAdminRole loadRoleByCode(String roleCode) {
        if (StringTools.isEmpty(roleCode)) {
            return null;
        }
        return sysAdminRoleService.getOne(new LambdaQueryWrapper<SysAdminRole>()
                .eq(SysAdminRole::getRoleCode, roleCode.trim())
                .last("LIMIT 1"));
    }

    private void replaceAccountRoles(String accountId, List<String> roleIds, Date now) {
        sysAdminAccountRoleService.remove(new LambdaQueryWrapper<SysAdminAccountRole>()
                .eq(SysAdminAccountRole::getAccountId, accountId));
        List<String> normalizedRoleIds = normalizeIdList(roleIds);
        if (normalizedRoleIds.isEmpty()) {
            return;
        }
        List<SysAdminAccountRole> relationList = normalizedRoleIds.stream()
                .map(roleId -> buildAccountRole(accountId, roleId, now))
                .toList();
        sysAdminAccountRoleService.saveBatch(relationList);
    }

    private SysAdminAccountRole buildAccountRole(String accountId, String roleId, Date now) {
        SysAdminAccountRole relation = new SysAdminAccountRole();
        relation.setRelId(StringTools.getRandomNumber(Constants.LENGTH_32));
        relation.setAccountId(accountId);
        relation.setRoleId(roleId);
        relation.setCreateTime(now);
        relation.setUpdateTime(now);
        return relation;
    }

    private Map<String, List<SysAdminRole>> loadRoleMapByAccountIds(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return Map.of();
        }
        List<SysAdminAccountRole> relationList = sysAdminAccountRoleService.list(new LambdaQueryWrapper<SysAdminAccountRole>()
                .in(SysAdminAccountRole::getAccountId, accountIds));
        if (relationList.isEmpty()) {
            return Map.of();
        }
        Map<String, SysAdminRole> roleMap = sysAdminRoleService.listByIds(relationList.stream()
                        .map(SysAdminAccountRole::getRoleId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(SysAdminRole::getRoleId, Function.identity(), (left, right) -> left));
        Map<String, List<SysAdminRole>> result = new LinkedHashMap<>();
        for (SysAdminAccountRole relation : relationList) {
            SysAdminRole role = roleMap.get(relation.getRoleId());
            if (role == null) {
                continue;
            }
            result.computeIfAbsent(relation.getAccountId(), key -> new ArrayList<>()).add(role);
        }
        return result;
    }

    private List<SysAdminRole> loadRolesByAccountId(String accountId) {
        return loadRoleMapByAccountIds(List.of(accountId)).getOrDefault(accountId, List.of());
    }

    private Map<String, Long> loadAccountCountMap(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return sysAdminAccountRoleService.list(new LambdaQueryWrapper<SysAdminAccountRole>()
                        .in(SysAdminAccountRole::getRoleId, roleIds))
                .stream()
                .collect(Collectors.groupingBy(SysAdminAccountRole::getRoleId, Collectors.counting()));
    }

    private boolean matchAccount(SysAdminAccount account, List<SysAdminRole> roleList, AdminAccountQueryDTO query) {
        if (query.getStatus() != null && !Objects.equals(account.getStatus(), query.getStatus())) {
            return false;
        }
        if (StringTools.isNotEmpty(query.getRoleId())
                && roleList.stream().noneMatch(role -> query.getRoleId().equals(role.getRoleId()))) {
            return false;
        }
        if (StringTools.isEmpty(query.getKeyword())) {
            return true;
        }
        String keyword = query.getKeyword().trim();
        return contains(account.getAccountId(), keyword)
                || contains(account.getAccountName(), keyword)
                || contains(account.getNickname(), keyword)
                || contains(account.getPhone(), keyword)
                || contains(account.getEmail(), keyword);
    }

    private boolean matchRole(SysAdminRole role, AdminRoleQueryDTO query) {
        if (query.getStatus() != null && !Objects.equals(role.getStatus(), query.getStatus())) {
            return false;
        }
        if (StringTools.isEmpty(query.getKeyword())) {
            return true;
        }
        String keyword = query.getKeyword().trim();
        return contains(role.getRoleCode(), keyword)
                || contains(role.getRoleName(), keyword)
                || contains(role.getRemark(), keyword);
    }

    private AdminAccountListVO buildAccountListVO(SysAdminAccount account, List<SysAdminRole> roleList) {
        AdminAccountListVO vo = new AdminAccountListVO();
        vo.setAccountId(account.getAccountId());
        vo.setAccountName(account.getAccountName());
        vo.setNickname(account.getNickname());
        vo.setStatus(account.getStatus());
        vo.setStatusDesc(resolveStatusDesc(account.getStatus()));
        vo.setSuperAdmin(isSuperAdmin(account));
        vo.setRoleNames(roleList.stream().map(SysAdminRole::getRoleName).distinct().toList());
        vo.setLastLoginTime(account.getLastLoginTime());
        vo.setCreateTime(account.getCreateTime());
        return vo;
    }

    private AdminAccountDetailVO buildAccountDetailVO(SysAdminAccount account, List<SysAdminRole> roleList) {
        AdminAccountDetailVO vo = new AdminAccountDetailVO();
        vo.setAccountId(account.getAccountId());
        vo.setAccountName(account.getAccountName());
        vo.setNickname(account.getNickname());
        vo.setPhone(account.getPhone());
        vo.setEmail(account.getEmail());
        vo.setStatus(account.getStatus());
        vo.setStatusDesc(resolveStatusDesc(account.getStatus()));
        vo.setSuperAdmin(isSuperAdmin(account));
        vo.setRemark(account.getRemark());
        vo.setRoleIds(roleList.stream().map(SysAdminRole::getRoleId).distinct().toList());
        vo.setRoleNames(roleList.stream().map(SysAdminRole::getRoleName).distinct().toList());
        List<String> permissionCodes = resolvePermissionCodes(isSuperAdmin(account), roleList);
        vo.setPermissionCodes(permissionCodes);
        vo.setPermissionNames(resolvePermissionNames(permissionCodes));
        vo.setLastLoginTime(account.getLastLoginTime());
        vo.setCreateTime(account.getCreateTime());
        vo.setUpdateTime(account.getUpdateTime());
        return vo;
    }

    private AdminRoleVO buildRoleVO(SysAdminRole role, Long accountCount) {
        AdminRoleVO vo = new AdminRoleVO();
        vo.setRoleId(role.getRoleId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setStatus(role.getStatus());
        vo.setStatusDesc(resolveStatusDesc(role.getStatus()));
        vo.setRemark(role.getRemark());
        List<String> permissionCodes = splitCommaValue(role.getPermissionCodes());
        vo.setPermissionCodes(permissionCodes);
        vo.setPermissionNames(resolvePermissionNames(permissionCodes));
        vo.setAccountCount(accountCount);
        vo.setCreateTime(role.getCreateTime());
        vo.setUpdateTime(role.getUpdateTime());
        return vo;
    }

    private AdminCurrentAccountVO buildCurrentAccountVO(SysAdminAccount account, List<SysAdminRole> roleList) {
        AdminCurrentAccountVO vo = new AdminCurrentAccountVO();
        vo.setAccountId(account.getAccountId());
        vo.setAccountName(account.getAccountName());
        vo.setNickname(account.getNickname());
        vo.setSuperAdmin(isSuperAdmin(account));
        vo.setRoleCodes(roleList.stream().map(SysAdminRole::getRoleCode).distinct().toList());
        vo.setRoleNames(roleList.stream().map(SysAdminRole::getRoleName).distinct().toList());
        vo.setPermissionCodes(resolvePermissionCodes(isSuperAdmin(account), roleList));
        vo.setLastLoginTime(account.getLastLoginTime());
        return vo;
    }

    private AdminCurrentAccountVO buildConfigAdminProfile() {
        AdminCurrentAccountVO vo = new AdminCurrentAccountVO();
        vo.setAccountId(CONFIG_ADMIN_ID);
        vo.setAccountName(appConfig.getAdminAccount());
        vo.setNickname("系统管理员");
        vo.setSuperAdmin(Boolean.TRUE);
        vo.setRoleCodes(List.of("super-admin"));
        vo.setRoleNames(List.of("系统管理员"));
        vo.setPermissionCodes(allPermissionCodes());
        return vo;
    }

    private List<String> resolvePermissionCodes(boolean superAdmin, List<SysAdminRole> roleList) {
        if (superAdmin) {
            return allPermissionCodes();
        }
        return roleList.stream()
                .filter(role -> Objects.equals(role.getStatus(), AdminAccountStatusEnum.ENABLED.getStatus()))
                .flatMap(role -> splitCommaValue(role.getPermissionCodes()).stream())
                .distinct()
                .toList();
    }

    private List<String> resolvePermissionNames(List<String> permissionCodes) {
        return permissionCodes.stream()
                .map(AdminPermissionEnum::getByCode)
                .filter(Objects::nonNull)
                .map(AdminPermissionEnum::getName)
                .toList();
    }

    private List<String> allPermissionCodes() {
        return List.of(AdminPermissionEnum.values()).stream()
                .map(AdminPermissionEnum::getCode)
                .toList();
    }

    private AdminAccountStatusEnum resolveStatusEnum(Integer status) {
        AdminAccountStatusEnum statusEnum = AdminAccountStatusEnum.getByStatus(status == null
                ? AdminAccountStatusEnum.ENABLED.getStatus() : status);
        if (statusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "status is invalid");
        }
        return statusEnum;
    }

    private String resolveStatusDesc(Integer status) {
        AdminAccountStatusEnum statusEnum = AdminAccountStatusEnum.getByStatus(status);
        return statusEnum == null ? "unknown" : statusEnum.getDesc();
    }

    private boolean isSuperAdmin(SysAdminAccount account) {
        return account != null && Objects.equals(account.getSuperAdmin(), 1);
    }

    private boolean matchesPassword(String storedPassword, String inputPassword) {
        if (StringTools.isEmpty(storedPassword) || StringTools.isEmpty(inputPassword)) {
            return false;
        }
        String normalizedInput = inputPassword.trim();
        return storedPassword.equalsIgnoreCase(normalizedInput)
                || storedPassword.equalsIgnoreCase(StringTools.encodeByMd5(normalizedInput));
    }

    private String normalizePassword(String password) {
        String normalized = normalizeNullable(password);
        if (normalized == null) {
            return null;
        }
        if (MD5_PATTERN.matcher(normalized).matches()) {
            return normalized.toLowerCase();
        }
        return StringTools.encodeByMd5(normalized);
    }

    private String resolveNickname(String nickname, String fallback) {
        String normalized = normalizeNullable(nickname);
        return normalized == null ? fallback : normalized;
    }

    private String normalizeNullable(String value) {
        if (StringTools.isEmpty(value)) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private List<String> normalizeIdList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(StringTools::isNotEmpty)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private List<String> normalizePermissionCodes(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return List.of();
        }
        List<String> codes = permissionCodes.stream()
                .filter(StringTools::isNotEmpty)
                .map(String::trim)
                .distinct()
                .toList();
        for (String code : codes) {
            if (AdminPermissionEnum.getByCode(code) == null) {
                throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "permission code is invalid");
            }
        }
        return codes;
    }

    private List<String> splitCommaValue(String value) {
        if (StringTools.isEmpty(value)) {
            return List.of();
        }
        return List.of(value.split(",")).stream()
                .map(String::trim)
                .filter(StringTools::isNotEmpty)
                .distinct()
                .toList();
    }

    private boolean contains(String source, String keyword) {
        return StringTools.isNotEmpty(source) && StringTools.isNotEmpty(keyword) && source.contains(keyword);
    }

    private <T> PageResultVO<T> paginate(List<T> source, Integer pageNo, Integer pageSize) {
        if (source == null || source.isEmpty()) {
            return PageResultVO.empty(pageNo, pageSize);
        }
        int currentPage = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int fromIndex = Math.max(0, (currentPage - 1) * currentSize);
        if (fromIndex >= source.size()) {
            return PageResultVO.empty(currentPage, currentSize);
        }
        int toIndex = Math.min(source.size(), fromIndex + currentSize);
        return new PageResultVO<>(currentPage, currentSize, (long) source.size(), source.subList(fromIndex, toIndex));
    }
}
