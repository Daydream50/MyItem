package com.yuan.gmall.service;

import com.yuan.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem findMemberId(String id, String mangerId);

    void addCartDb(OmsCartItem pmsSkuInfo);

    void updateCart(OmsCartItem cartItem);

    void flushCartCache(String mangerId);

    List<OmsCartItem> selectCart(String mangerId);

    void delCart(String memberId);
}
