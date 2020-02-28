package com.yuan.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.OmsCartItem;
import com.yuan.gmall.cart.service.mapper.CartItemMapper;
import com.yuan.gmall.service.CartService;
import com.yuan.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

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
            e.createCriteria().andEqualTo("productSkuId", cartItem.getProductSkuId()); //根据主键更新

            cartItemMapper.updateByExampleSelective(cartItem, e);

            flushCartCache(cartItem.getMemberId());
        }
    }

    /**
     * 同步缓存
     *
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
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));  //算好总价格
                map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }
            if (map != null) {
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


        Jedis jedis = redisUtil.getJedis();

        try {
            //查询缓存
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

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }

        return omsCartItemList;

    }

    @Override
    public void delCart(String mangerId) {
        Jedis jedis = null;
        List<String> hvals = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            String cartKey = "user:" + mangerId + ":cart";
            //查询缓存
            hvals = jedis.hvals(cartKey);
            if (hvals != null && !hvals.isEmpty()) {
                //删除缓存
                jedis.del(cartKey);
                //删除数据库
                OmsCartItem omsCartItem = new OmsCartItem();
                omsCartItem.setMemberId(mangerId);
                cartItemMapper.delete(omsCartItem);
            } else {
                new Throwable("删除缓存失败");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }finally {
            jedis.close();
        }
    }

}
