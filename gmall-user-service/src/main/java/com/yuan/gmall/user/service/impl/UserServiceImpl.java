package com.yuan.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.UmsMember;
import com.yuan.gmall.bean.UmsMemberReceiveAddress;
import com.yuan.gmall.service.UserService;
import com.yuan.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.yuan.gmall.user.mapper.UserMapper;
import com.yuan.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;


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

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = null;
        try {

            jedis = redisUtil.getJedis();

            if (jedis != null) {
                String redisKey = jedis.get("user:" + umsMember.getUsername() + "-" + umsMember.getPassword() + ":info");

                if (StringUtils.isNotBlank(redisKey)) {
                    //缓存中有信息
                    UmsMember umsMemberFromCache = JSON.parseObject(redisKey, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //缓存没有在数据库中取
            UmsMember umsMemberDb = userMapper.selectOne(umsMember);
            if (umsMemberDb != null) {

                jedis.setex("user:" + umsMember.getUsername() + "-" + umsMember.getPassword() + ":info",60*60*24, JSON.toJSONString(umsMemberDb));

            }

            return umsMemberDb;


        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            jedis.close();
        }

    }

    @Override
    public void addUserToken(String token, String Id) {

        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            if(jedis != null){
                jedis.setex("user:"+Id+":token",60*60*2,token);  //过期两个小时
            }

        }catch (Exception e){
            throw new RuntimeException("redis初始化有问题");
        }finally {

            jedis.close();

        }




    }

    @Override
    public UmsMember checkUser(String sourceUid) {
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(sourceUid);
        UmsMember checkUser =  userMapper.selectOne(umsMember);
        return checkUser;
    }

    @Override
    public void addUser(UmsMember umsMember) {

        userMapper.insertSelective(umsMember);

    }
}
