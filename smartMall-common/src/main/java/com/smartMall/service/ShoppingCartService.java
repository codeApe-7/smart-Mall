package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.ShoppingCart;
import com.smartMall.entities.dto.CartAddDTO;
import com.smartMall.entities.dto.CartDeleteDTO;
import com.smartMall.entities.dto.CartQuantityUpdateDTO;
import com.smartMall.entities.dto.CartSelectedUpdateDTO;
import com.smartMall.entities.vo.ShoppingCartVO;

/**
 * 购物车 Service。
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 查询用户购物车。
     *
     * @param userId 用户ID
     * @return 购物车聚合信息
     */
    ShoppingCartVO loadCart(String userId);

    /**
     * 新增购物车商品。
     *
     * @param dto 新增参数
     */
    void addCartItem(CartAddDTO dto);

    /**
     * 更新购物车数量。
     *
     * @param dto 更新参数
     */
    void updateQuantity(CartQuantityUpdateDTO dto);

    /**
     * 更新购物车勾选状态。
     *
     * @param dto 更新参数
     */
    void updateSelected(CartSelectedUpdateDTO dto);

    /**
     * 删除购物车条目。
     *
     * @param dto 删除参数
     */
    void deleteItems(CartDeleteDTO dto);
}
