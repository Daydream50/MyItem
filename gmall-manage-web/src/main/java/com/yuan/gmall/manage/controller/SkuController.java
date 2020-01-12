package com.yuan.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.bean.PmsSkuInfo;
import com.yuan.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * sku操作
 */
@Controller
@CrossOrigin
public class SkuController {

    @Reference
    SkuService skuService;

    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody @Valid PmsSkuInfo pmsSkuInfo) {

        // 将spuId封装给productId
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());

        skuService.insertSkuInfo(pmsSkuInfo);

        return "success";

    }
}
