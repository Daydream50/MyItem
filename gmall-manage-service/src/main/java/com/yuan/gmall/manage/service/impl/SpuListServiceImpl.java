package com.yuan.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yuan.gmall.bean.*;
import com.yuan.gmall.manage.mapper.PmsProductImageMapper;
import com.yuan.gmall.manage.mapper.PmsProductInfoMapper;
import com.yuan.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.yuan.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.yuan.gmall.service.SpuListService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuListServiceImpl implements SpuListService {

    //商品信息
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;

    //图片信息
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;

    //销售信息
    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    //销售属性
    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {

        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        //保存商品信息
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        //获得商品id
        String infoId = pmsProductInfo.getId();

        //保存图片
        List<PmsProductImage> file = pmsProductInfo.getSpuImageList();
        for (PmsProductImage pmsProductImage : file) {
            //设置商品id
            pmsProductImage.setProductId(infoId);
            //保存图片信息
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }

        //保存销售属性
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
            pmsProductSaleAttr.setProductId(infoId);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);

            // 保存销售属性值
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(infoId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }
        }
    }

    @Override
    public List<PmsProductSaleAttr> saleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        pmsProductSaleAttr.setSaleAttrId(pmsProductSaleAttr.getSaleAttrId());
        List<PmsProductSaleAttr> saleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        for (PmsProductSaleAttr productSaleAttr : saleAttrs) {
            //设置销售属性id，和销售产品id
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(productSaleAttr.getProductId());
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            //查询
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        }

        return saleAttrs;
    }

    @Override
    public List<PmsProductImage> fileUpload(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> productImageList = pmsProductImageMapper.select(pmsProductImage);
        return productImageList;
    }

    /**
     * 销售属性
     * @param ProductId
     * @return
     */
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String ProductId,String skuId) {

   /*     PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(ProductId);
        List<PmsProductSaleAttr> productSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        for (PmsProductSaleAttr saleAttr : productSaleAttrs) {

            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(ProductId);
            pmsProductSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> attrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);

            saleAttr.setSpuSaleAttrValueList(attrValues);
        }*/

       List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.selectSQL(ProductId,skuId);


        return pmsProductSaleAttrs;
    }

}
