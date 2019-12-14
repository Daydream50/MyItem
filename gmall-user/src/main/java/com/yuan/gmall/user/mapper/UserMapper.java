package com.yuan.gmall.user.mapper;

import com.yuan.gmall.user.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {

    List<UmsMember> selectAllUser();

    int updateById(UmsMember umsMember);

    int insertUser(UmsMember umsMember);
}
