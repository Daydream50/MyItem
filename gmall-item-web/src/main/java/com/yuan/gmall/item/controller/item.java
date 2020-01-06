package com.yuan.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.bean.PmsSkuInfo;

import com.yuan.gmall.service.SkuService;
import org.springframework.stereotype.Controller;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class item {

    @Reference
    SkuService skuService ;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap) {
       PmsSkuInfo pmsSkuInfos = skuService.getSkuId(skuId);
       modelMap.put("skuInfo",pmsSkuInfos);
       return "item";
    }
}
