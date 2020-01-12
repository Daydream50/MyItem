package com.yuan.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.PmsProductSaleAttr;
import com.yuan.gmall.bean.PmsSkuInfo;
import com.yuan.gmall.bean.PmsSkuSaleAttrValue;
import com.yuan.gmall.service.SkuService;
import com.yuan.gmall.service.SpuListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@Controller
public class item {
    @Reference
    SkuService skuService;

    @Reference
    SpuListService spuListService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable @Valid String skuId, ModelMap modelMap, HttpServletRequest httpServletRequest) {

       //获得ip
       //在请求体中获取ip，如果用ngnix负载均衡的话用  httpServletRequest.getHeader("")
       String ip =  httpServletRequest.getRemoteAddr();


        if (!skuId.isEmpty()) {
            PmsSkuInfo pmsSkuInfos = skuService.getSkuId(skuId,ip);
            //图片
            modelMap.put("skuInfo", pmsSkuInfos);

            //属性
            List<PmsProductSaleAttr> pmsProductInfos = spuListService.spuSaleAttrListCheckBySku(pmsSkuInfos.getProductId(), skuId);
            modelMap.put("spuSaleAttrListCheckBySku", pmsProductInfos);

            //销售属性HashMap表
            HashMap<String, String> skuSaleMap = new HashMap<>();
            List<PmsSkuInfo> skuInfoList = skuService.getSkuSaleAttrValueList(pmsSkuInfos.getProductId());
            if (skuInfoList != null) {
                for (PmsSkuInfo skuInfo : skuInfoList) {
                    String k = "";
                    String v = pmsSkuInfos.getId();

                    List<PmsSkuSaleAttrValue> valueList = skuInfo.getSkuSaleAttrValueList();
                    for (PmsSkuSaleAttrValue saleAttrValue : valueList) {
                        k += saleAttrValue.getSaleAttrId() + "|";
                    }
                    skuSaleMap.put(k, v);
                }

                //用fastJson将HashMap表放在页面
                String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleMap);
                modelMap.put("skuSaleAttrHashJsonStr", skuSaleAttrHashJsonStr);
            }
        }else {
            return "404";
        }
        return "item";
    }
}
