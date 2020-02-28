package com.yuan.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.annotations.LoginRequired;
import com.yuan.gmall.bean.OmsOrder;
import com.yuan.gmall.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;


@Controller
public class PaymentController {

    @Reference
    PaymentService paymentService;

    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");


        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("nickName",nickname);

        return "index";
    }


}
