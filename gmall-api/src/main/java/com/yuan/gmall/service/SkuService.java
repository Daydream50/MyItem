package com.yuan.gmall.service;

import com.yuan.gmall.bean.PmsSkuInfo;

public interface SkuService {
    void insertSkuInfo(PmsSkuInfo pmsSkuInfo);

     PmsSkuInfo getSkuId(String skuId);
}
