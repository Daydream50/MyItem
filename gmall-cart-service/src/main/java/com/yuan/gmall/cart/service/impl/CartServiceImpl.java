package com.yuan.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.OmsCartItem;
import com.yuan.gmall.service.CartService;
import com.yuan.gmall.cart.service.mapper.CartItemMapper;
import com.yuan.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartItemMapper cartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 查找是否有购物车
     *
     * @param skuId
     * @param mangerId
     * @return
     */
    @Override
    public OmsCartItem findMemberId(String skuId, String mangerId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(mangerId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = cartItemMapper.selectOne(omsCartItem);

        return omsCartItem1;
    }


    @Override
    public void addCartDb(OmsCartItem omsCartItem) {
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())) { //用户id不能为空
            cartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem cartItem) {

        if (StringUtils.isNotBlank(cartItem.getMemberId())) { //用户id不能为空
            Example e = new Example(OmsCartItem.class);
            e.createCriteria().andEqualTo("id", cartItem.getId()); //根据主键更新

            cartItemMapper.updateByExampleSelective(cartItem, e);
        }
    }

    /**
     * 同步缓存
     * @param mangerId
     */
    @Override
    public void flushCartCache(String mangerId) {

        OmsCartItem omsCartItem = null;
        Jedis jedis = null;
        try {
            omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(mangerId);

            List<OmsCartItem> omsCartItems = cartItemMapper.select(omsCartItem);

            // 同步到redis缓存中
            jedis = redisUtil.getJedis();

            Map<String, String> map = new HashMap<>();
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
                map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }
            if(map != null) {
                //先删在加
                jedis.del("user:" + mangerId + ":cart");
                jedis.hmset("user:" + mangerId + ":cart", map);
            }

        } catch (Exception e) {
            new Throwable(e);
        } finally {
            jedis.close();
        }

    }

    @Override
    public List<OmsCartItem> selectCart(String mangerId) {


        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        OmsCartItem omsCartItem = new OmsCartItem();
        List<String> hvals = new ArrayList<>();

        //查询缓存
        Jedis jedis = redisUtil.getJedis();

        try {

            hvals = jedis.hvals("user:" + mangerId + ":cart");

            if (hvals != null && !hvals.isEmpty()) {
                for (String hval : hvals) {
                    omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    omsCartItemList.add(omsCartItem);
                }
            } else {
                //缓存没有查询数据库
                flushCartCache(mangerId);
                hvals = jedis.hvals("user:" + mangerId + ":cart");

                if (hvals != null && !hvals.isEmpty()) {
                    for (String hval : hvals) {
                        omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                        omsCartItemList.add(omsCartItem);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            jedis.close();
        }

        return omsCartItemList;

    }

}
