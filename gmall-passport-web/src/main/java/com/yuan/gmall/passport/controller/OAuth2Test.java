package com.yuan.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.yuan.gmall.util.HttpclientUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class OAuth2Test {




    public void getcode() {

        //http://passport.gmall.com:8086/vlogin
        //获得授权码
        String key = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=1500314739&response_type=code&redirect_uri=http://passport.gmall.com:8086/vlogin");
        System.out.println(key);

        //用户必须登陆
    }


    //18caddc8007b1a167fe471287b0163e0

    public String getToken() {
        //根据code获得Token
        //必须是POST
        String s3 = "https://api.weibo.com/oauth2/access_token?";   //client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "1500314739");
        paramMap.put("client_secret", "ec54ef82d84d9fa6e30a4a5753ca8a46");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://passport.gmall.com:8086/vlogin");
        paramMap.put("code", "0ca270c44d4f664890fcef5235d4f67b");// 授权有效期内可以使用，没新生成一次授权码，说明用户对第三方数据进行重启授权，之前的access_token和授权码全部过期
        String access_token_json = HttpclientUtil.doPost(s3, paramMap);

        Map<String, String> access_map = JSON.parseObject(access_token_json, Map.class);

        System.out.println(access_map.get("access_token"));
        System.out.println(access_map.get("uid"));

        return access_map.get("access_token");

    }


    //Token  2.00qBoWJDp5KXdB2fd5e19e03WU6h_E  UID  2889204330


    //利用Token和UID查询用户信息
    public void getUser() {
        // 4 用access_token查询用户信息
        String s4 = "https://api.weibo.com/2/users/show.json?access_token=2.00qBoWJDp5KXdB2fd5e19e03WU6h_E&uid=2889204330";
        String token = HttpclientUtil.doGet(s4);

        Map<String,String> map =  JSON.parseObject(token,Map.class);


    }

    @Test
    public void VloginTest() {
        // getcode();
      //  getToken();
        getUser();
    }

}
