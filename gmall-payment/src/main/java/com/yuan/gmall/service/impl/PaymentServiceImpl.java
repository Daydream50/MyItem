package com.yuan.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.yuan.gmall.bean.OmsOrder;
import com.yuan.gmall.service.OrderService;
import com.yuan.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Reference
    OrderService orderService;

    @Override
    public List<OmsOrder> findOrder(String memberId) {

        return  orderService.selectOrder(memberId);
    }
}
