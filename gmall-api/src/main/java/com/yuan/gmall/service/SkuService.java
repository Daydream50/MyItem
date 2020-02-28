package com.yuan.gmall.service;

import com.yuan.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    void insertSkuInfo(PmsSkuInfo pmsSkuInfo);

     PmsSkuInfo getSkuId(String skuId ,String ip);

    List<PmsSkuInfo> getSkuSaleAttrValueList(String productId);

    List<PmsSkuInfo> getAllSku(String catalog3Id);

    boolean checkPrice(String productSkuId, BigDecimal price);
}
