package com.yuan.gmall.user.service.impl;

import com.yuan.gmall.bean.UmsMember;
import com.yuan.gmall.bean.UmsMemberReceiveAddress;
import com.yuan.gmall.service.UserService;
import com.yuan.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.yuan.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class UserServiceImpl implements UserService {

    @Autowired
    public UserMapper userMapper;

    @Autowired
    public UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> selectAllUser() {
        /*  List<UmsMember> members = userMapper.selectAllUser();*/
        List<UmsMember> members = userMapper.selectAll();
        return members;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        // 封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }

    @Override
    public int updateById(String id) {
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername("xjyws");
        umsMember.setId(id);
        int i = userMapper.updateById(umsMember);
        return i;
    }

    @Override
    public void deleteUserById(String id) {
        userMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int saveUser(String id) {
        UmsMember umsMember = new UmsMember();
        umsMember.setCity("中国");
        umsMember.setUsername("xjy123");
        int userId = userMapper.insertUser(umsMember);
        return userId;
    }
}
