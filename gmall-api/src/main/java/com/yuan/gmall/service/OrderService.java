package com.yuan.gmall.service;

import com.yuan.gmall.bean.OmsOrder;

import java.util.List;

public interface OrderService {
    void cartList(String memberId);

    String setTradeCode(String memberId);

    String getTradeCode(String memberId,String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    List<OmsOrder> selectOrder(String memberId);
}
