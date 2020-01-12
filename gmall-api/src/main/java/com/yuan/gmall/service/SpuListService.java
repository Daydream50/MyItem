package com.yuan.gmall.service;

import com.yuan.gmall.bean.PmsProductImage;
import com.yuan.gmall.bean.PmsProductInfo;
import com.yuan.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuListService {
    List<PmsProductInfo> spuList(String catalog3Id);

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> saleAttrList(String puId);

    List<PmsProductImage> fileUpload(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String ProductId,String skuId);
}
