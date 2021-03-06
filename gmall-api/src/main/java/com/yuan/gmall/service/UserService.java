package com.yuan.gmall.service;

import com.yuan.gmall.bean.UmsMember;
import com.yuan.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    //查询全部用户
    List<UmsMember> selectAllUser();

    // 根据id查询 收货人
    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

   // 更新用户表
    int updateById(String id);

  //   根据id删除用户
    void deleteUserById(String id);

    int saveUser(String id);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String id);

    UmsMember checkUser(String sourceUid);

    void addUser(UmsMember umsMember);

    List<UmsMemberReceiveAddress> selectAddress(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
