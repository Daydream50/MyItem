package com.yuan.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yuan.gmall.bean.PmsBaseAttrInfo;
import com.yuan.gmall.bean.PmsBaseAttrValue;
import com.yuan.gmall.bean.PmsBaseSaleAttr;
import com.yuan.gmall.manage.mapper.*;
import com.yuan.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Override
    public List<PmsBaseAttrInfo> attrList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);

        List<PmsBaseAttrInfo> attrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        for (PmsBaseAttrInfo baseAttrInfo : attrInfos) {
            List<PmsBaseAttrValue> pmsBaseAttrValues = new ArrayList<>();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }

        return attrInfos;
    }

    /**
     * 添加平台属性
     *
     * @param pmsBaseAttrInfo
     */
    @Override
    public void saveAttr(PmsBaseAttrInfo pmsBaseAttrInfo) {

        //判断id是否为空
        if (StringUtils.isBlank(pmsBaseAttrInfo.getId())) {
            //为空保存
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);  //使用insertSelective就会只给有值的字段赋值

            // 保存属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        } else {
            //更新
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());  //正则表达式根据id修改
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo, example);  //updateByExampleSelective空值不修改

            //属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();

            //按照属性id删除所有属性值
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);

            //删除后将新的属性插入
            for (PmsBaseAttrValue pmsBaseAttrValue1 : attrValueList) {
                pmsBaseAttrValue1.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue1);
                System.out.println(pmsBaseAttrValue1);
            }
        }
    }

    /**
     * 更新
     *
     * @param attrId
     * @return
     */
    @Override
    public List<PmsBaseAttrValue> attrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> attrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return attrValues;
    }

    /**
     * 查找全部销售属性
     *
     * @return
     */
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

}
