package com.yuan.gmall.manage.mapper;

import com.yuan.gmall.bean.PmsBaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectAttrValueListByValueId(String valueIdStr);
}
