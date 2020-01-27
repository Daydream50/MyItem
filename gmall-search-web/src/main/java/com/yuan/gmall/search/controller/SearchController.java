package com.yuan.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.annotations.LoginRequired;
import com.yuan.gmall.bean.PmsBaseAttrInfo;
import com.yuan.gmall.bean.PmsSearchParam;
import com.yuan.gmall.bean.PmsSearchSkuInfo;
import com.yuan.gmall.bean.PmsSkuAttrValue;
import com.yuan.gmall.service.AttrService;
import com.yuan.gmall.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){// 三级分类id、关键字、

        // 调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos =  searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);


        // 抽取检索结果锁包含的平台属性集合
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                if(pmsSkuAttrValue !=   null) {
                    String valueId = pmsSkuAttrValue.getValueId();
                    valueIdSet.add(valueId);
                }
            }
        }

        // 根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);

        return "list";
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){
        return "index";
    }
}
