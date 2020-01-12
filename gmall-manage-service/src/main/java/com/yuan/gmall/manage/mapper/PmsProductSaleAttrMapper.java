package com.yuan.gmall.manage.mapper;

import com.yuan.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> selectSQL(@Param("productId") String productId, @Param("skuId") String skuId);
}
