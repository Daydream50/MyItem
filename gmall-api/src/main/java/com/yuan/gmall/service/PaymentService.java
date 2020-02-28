package com.yuan.gmall.service;

import com.yuan.gmall.bean.OmsOrder;

import java.util.List;

public interface PaymentService {
    List<OmsOrder> findOrder(String memberId);
}
