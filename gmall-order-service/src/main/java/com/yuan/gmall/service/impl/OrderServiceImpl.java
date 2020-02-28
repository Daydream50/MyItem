package com.yuan.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.OmsOrder;
import com.yuan.gmall.bean.OmsOrderItem;
import com.yuan.gmall.mapper.OmsOrderItemMapper;
import com.yuan.gmall.mapper.OmsOrderMapper;
import com.yuan.gmall.service.CartService;
import com.yuan.gmall.service.OrderService;
import com.yuan.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.ExecutorException;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService  {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Reference
    CartService cartService;

    @Override
    public void cartList(String memberId) {

        List<String> list = new ArrayList<>();

       try( Jedis jedis = redisUtil.getJedis();){
           String cartKey = "user:" + memberId + ":cart";
           String redisStr  = jedis.get(cartKey);
           if(StringUtils.isNotBlank(redisStr)){
               list =  JSON.parseObject(redisStr, List.class);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    /**
     * 设置校验码
     * @param memberId
     * @return
     */
    @Override
    public String setTradeCode(String memberId) {
        try( Jedis jedis = redisUtil.getJedis();){
            String tradeKey = "user:" + memberId + ":tradeCode";
            String tradeCode = UUID.randomUUID().toString();
            jedis.setex(tradeKey,60*15,tradeCode);
            return tradeCode;
        }catch (Exception e){
            throw new  ExecutorException("订单校验码异常");
        }

    }

    @Override
    public String getTradeCode(String memberId,String tradeCode) {
        try( Jedis jedis = redisUtil.getJedis();){
            String tradeKey = "user:" + memberId + ":tradeCode";
            String tradeCodeFormCache =  jedis.get(tradeKey);

            if(StringUtils.isNoneBlank(tradeCodeFormCache) && tradeCodeFormCache.equals(tradeCode)){
                //获得校验码成功并且删除校验码
                //对比防重删令牌
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));   //lua脚本

                if (eval!=null && eval!=0) {
                    // jedis.del(tradeKey);
                    return "success";
                } else {
                    return "fail";
                }
            }else {
                return "fail";
            }


        }catch (Exception e){
            throw new  ExecutorException("订单校验码异常");
        }

    }


    @Override
    public void saveOrder(OmsOrder omsOrder) {

        // 保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        // 保存订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);

        }
        // 删除购物车数据
        cartService.delCart(omsOrder.getMemberId());
    }

    @Override
    public List<OmsOrder> selectOrder(String memberId) {

        if(memberId != null) {
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setMemberId(memberId);
            return omsOrderMapper.select(omsOrder);
        }else {
            return null;
        }
    }


}
