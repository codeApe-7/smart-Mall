package com.smartMall.controller;

import com.smartMall.entities.dto.CartAddDTO;
import com.smartMall.entities.dto.CartDeleteDTO;
import com.smartMall.entities.dto.CartQuantityUpdateDTO;
import com.smartMall.entities.dto.CartSelectedUpdateDTO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.vo.ShoppingCartVO;
import com.smartMall.service.ShoppingCartService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端购物车控制器。
 */
@Slf4j
@RestController
@RequestMapping("/cart")
public class MallCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public ResponseVO<ShoppingCartVO> list(@RequestParam String userId) {
        log.info("web load cart, userId={}", userId);
        return ResponseVO.success(shoppingCartService.loadCart(userId));
    }

    @PostMapping("/add")
    public ResponseVO<Void> add(@RequestBody @Valid CartAddDTO dto) {
        shoppingCartService.addCartItem(dto);
        return ResponseVO.success();
    }

    @PostMapping("/updateQuantity")
    public ResponseVO<Void> updateQuantity(@RequestBody @Valid CartQuantityUpdateDTO dto) {
        shoppingCartService.updateQuantity(dto);
        return ResponseVO.success();
    }

    @PostMapping("/updateSelected")
    public ResponseVO<Void> updateSelected(@RequestBody @Valid CartSelectedUpdateDTO dto) {
        shoppingCartService.updateSelected(dto);
        return ResponseVO.success();
    }

    @PostMapping("/delete")
    public ResponseVO<Void> delete(@RequestBody @Valid CartDeleteDTO dto) {
        shoppingCartService.deleteItems(dto);
        return ResponseVO.success();
    }
}
