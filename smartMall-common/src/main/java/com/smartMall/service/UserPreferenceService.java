package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.UserPreference;
import com.smartMall.entities.vo.UserPreferenceVO;

/**
 * 用户偏好档案服务。
 */
public interface UserPreferenceService extends IService<UserPreference> {

    /**
     * 查询用户偏好档案
     *
     * @param userId 用户ID
     * @return 偏好档案视图，不存在则返回空档案
     */
    UserPreferenceVO getUserPreference(String userId);

    /**
     * 聚合用户行为数据并刷新偏好档案
     *
     * @param userId 用户ID
     * @return 刷新后的偏好档案视图
     */
    UserPreferenceVO refreshUserPreference(String userId);
}
