package com.yuan.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.annotations.LoginRequired;
import com.yuan.gmall.bean.OmsCartItem;
import com.yuan.gmall.bean.OmsOrderItem;
import com.yuan.gmall.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;


    /**
     * 结算
     *
     * @param request
     * @param response
     * @param modelMap
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)  //需要拦截，并且拦截校验一定要通过(用户登录成功了)才能访问的方法
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        List<OmsCartItem> cartItemList = cartService.selectCart(memberId);

        if (cartItemList != null) {

            List<OmsOrderItem> orderList = new ArrayList<>();

            for (OmsCartItem omsCartItem : cartItemList) {
                if(omsCartItem.getIsChecked().equals("1")) {
                    OmsOrderItem orderItem = new OmsOrderItem();
                    orderItem.setProductPic(omsCartItem.getProductPic());
                    orderItem.setProductQuantity(omsCartItem.getQuantity());
                    orderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    orderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                    orderItem.setProductName(omsCartItem.getProductName());
                    orderItem.setProductPic(omsCartItem.getProductPic());

                    orderList.add(orderItem);
                }
            }
            modelMap.put("nickName",nickname);
            modelMap.put("omsOrderItems",orderList);
        }

        return "trade";

    }

}
