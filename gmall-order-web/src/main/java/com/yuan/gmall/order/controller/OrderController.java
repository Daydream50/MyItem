package com.yuan.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.annotations.LoginRequired;
import com.yuan.gmall.bean.OmsCartItem;
import com.yuan.gmall.bean.OmsOrder;
import com.yuan.gmall.bean.OmsOrderItem;
import com.yuan.gmall.bean.UmsMemberReceiveAddress;
import com.yuan.gmall.service.CartService;
import com.yuan.gmall.service.OrderService;
import com.yuan.gmall.service.SkuService;
import com.yuan.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    /**
     * 提交订单
     *
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)  //需要拦截，并且拦截校验一定要通过(用户登录成功了)才能访问的方法
    public ModelAndView submitOrder(String receiveAddressId, String tradeCode, BigDecimal totalAmount,
                                    String orderCommentPage, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //检验校验码
        String success = orderService.getTradeCode(memberId, tradeCode);

        if (success.equals("success")) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            // 订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount(); 运费，支付后，在生成物流信息时
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote(orderCommentPage);
            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();// 将毫秒时间戳拼接到外部订单号
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + sdf.format(new Date());// 将时间字符串拼接到外部订单号

            omsOrder.setOrderSn(outTradeNo);//外部订单号
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            //地址
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            // 当前日期加一天，一天后配送
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            Date time = c.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);
            omsOrder.setOrderType(0);
            omsOrder.setTotalAmount(totalAmount);

            // 根据用户id获得要购买的商品列表(购物车)，和总价格
            List<OmsCartItem> omsCartItems = cartService.selectCart(memberId);

            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    // 获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    // 检价
                    boolean b = false;
                    if (omsCartItem.getProductSkuId() != null && omsCartItem.getPrice() != null) {
                        b = skuService.checkPrice(omsCartItem.getProductSkuId(), omsCartItem.getPrice());
                    }
                    if (b == false) {
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }
                    // 验库存,远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());

                    omsOrderItem.setOrderSn(outTradeNo);// 外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");// 在仓库中的skuId

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            // 将订单和订单详情写入数据库
            // 删除购物车的对应商品
            orderService.saveOrder(omsOrder);

            // 重定向到支付系统
            ModelAndView mv = new ModelAndView("redirect:http://payment.gmall.com:8088/index");
            mv.addObject("outTradeNo",outTradeNo);
            mv.addObject("totalAmount",totalAmount);
            return mv;
        } else {
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }
    }

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
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap
            modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //收货人信息
        List<UmsMemberReceiveAddress> addresses = userService.selectAddress(memberId);
        if (addresses != null && addresses.size() != 0) {
            modelMap.put("userAddressList", addresses);
        }

        //商品列表
        List<OmsCartItem> cartItemList = cartService.selectCart(memberId);

        if (cartItemList != null) {

            List<OmsOrderItem> orderList = new ArrayList<>();

            //先判断是否提交订单
            for (OmsCartItem omsCartItem : cartItemList) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    OmsOrderItem orderItem = new OmsOrderItem();
                    orderItem.setProductPic(omsCartItem.getProductPic());
                    orderItem.setProductQuantity(omsCartItem.getQuantity());
                    orderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    orderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                    orderItem.setProductName(omsCartItem.getProductName());
                    orderList.add(orderItem);
                }
            }
            modelMap.put("nickName", nickname);
            modelMap.put("omsOrderItems", orderList);
            modelMap.put("totalAmount", getTotalAmount(cartItemList));

            //生成校验码
            String tradeCode = orderService.setTradeCode(memberId);
            modelMap.put("tradeCode", tradeCode);
        }

        return "trade";

    }

    private Object getTotalAmount(List<OmsCartItem> cartItemList) {

        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : cartItemList) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        return totalAmount;
    }

}
