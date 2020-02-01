package com.yuan.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.service.OrderService;
import com.yuan.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;



    @Override
    public void cartList(String mangerId) {

        Jedis jedis = null;

        List<String> list = new ArrayList<>();

        try {
            jedis = redisUtil.getJedis();
            String redisStr  = jedis.get("user:" + mangerId + ":cart");
            if(StringUtils.isNotBlank(redisStr)){
                list =  JSON.parseObject(redisStr, List.class);
            }


        }catch (Exception e){

        }finally {
            jedis.close();
        }

    }
}
