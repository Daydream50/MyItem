package com.yuan.gmall.service;

import com.yuan.gmall.bean.PmsBaseAttrInfo;
import com.yuan.gmall.bean.PmsBaseAttrValue;
import com.yuan.gmall.bean.PmsBaseSaleAttr;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrList(String catalog3Id);

    void saveAttr(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> attrValueList(String attrId);

    List<PmsBaseSaleAttr> baseSaleAttrList();
}
