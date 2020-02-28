package com.yuan.gmall.manage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.*;
import com.yuan.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.yuan.gmall.manage.mapper.PmsSkuImageMapper;
import com.yuan.gmall.manage.mapper.PmsSkuInfoMapper;
import com.yuan.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.yuan.gmall.service.SkuService;
import com.yuan.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    @Transactional

    public void insertSkuInfo(PmsSkuInfo pmsSkuInfo) {
        // 插入skuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        // 插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        // 插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

    }

    public PmsSkuInfo RedisGetSkuId(String skuId) {

        //属性对象

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //sku图片集合
        if (skuInfo != null) {
            PmsSkuImage pmsSkuImage = new PmsSkuImage();
            pmsSkuImage.setSkuId(skuId);
            List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
            skuInfo.setSkuImageList(pmsSkuImages);
        }
        return skuInfo;

    }

    @Override
    public PmsSkuInfo getSkuId(String skuId, String ip) {
        //System.out.println("请求的IP地址是 ：" + ip + "进程号为" + Thread.currentThread().getName());

        PmsSkuInfo pmsSkuInfo = null;

        Jedis jedis = null;

        try {
            pmsSkuInfo = new PmsSkuInfo();

            //链接缓存
            jedis = redisUtil.getJedis();

            //查询缓存
            String skuKey = "sku" + skuId + ":info";  //key
            String skuJson = jedis.get(skuKey);   //value


            //判断redis中是否存在
            if (StringUtils.isNotBlank(skuJson)) {   //if(skuJson != null && skuJson.equals("") )

                //返回json格式
                pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);

            } else {

                //设置分布式锁
                //防止误删锁设置token
                String token = UUID.randomUUID().toString();
                String OK = jedis.set("sku" + skuId + ":lock", token, "nx", "px", 5 * 1000); //5秒过期

                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {

                    int KId = 0;
                    System.out.println("" + KId + 1);

                    //查询数据库
                    pmsSkuInfo = RedisGetSkuId(skuId);

                    if (pmsSkuInfo != null) {

                        //mysql结果转为json存入缓存
                        jedis.set("sku" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                    } else {

                        //为了防止缓存穿透将，null或者空字符串值设置给redis
                        //为了防止雪崩，集体失效.设置随机值
                        //缓存击穿是某一个热点key在高并发访问的情况下，突然失效，导致大量的并发打进mysql数据库的情况.解决办法加锁
                        Random random = new Random();
                        int time = random.nextInt(10) + 5;
                        jedis.setex("sku" + skuId + ":info", time, JSON.toJSONString(""));   //三分钟后失效
                    }

                    String redisToken = jedis.get("sku" + skuId + ":lock");
                    if (StringUtils.isNotBlank(redisToken) && redisToken.equals(token)) {
                        //访问成功后将锁删除
                        // jedis.del("sku" + skuId + ":lock");

                        //使用脚本，查询的一瞬间删除
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        jedis.eval(script, Collections.singletonList("sku" + skuId + ":lock"), Collections.singletonList(token));

                    }

                } else {


                    try {
                        //睡眠，降低抢锁频率，缓解redis压力
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    return getSkuId(skuId, ip);
                }
            }
        } catch (Exception e) {
            new Throwable("getSkuId方法查询失败");
        } finally {
            jedis.close();
        }

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueList(String productId) {

        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectSkuAttrValue(productId);
        return pmsSkuInfoList;
    }

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {

        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectAll();

        PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
        for (PmsSkuInfo skuInfo : pmsSkuInfoList) {
            pmsSkuAttrValue.setSkuId(skuInfo.getId());
            List<PmsSkuAttrValue> skuAttrValueList = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            skuInfo.setSkuAttrValueList(skuAttrValueList);
        }

        return pmsSkuInfoList;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {
        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        if(pmsSkuInfo1 != null) {
            BigDecimal price = pmsSkuInfo1.getPrice();

            if (price.compareTo(productPrice) == 0) {
                b = true;
            }
        }

        return b;
    }
}
