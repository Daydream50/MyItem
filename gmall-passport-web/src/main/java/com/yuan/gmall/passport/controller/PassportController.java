package com.yuan.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.UmsMember;
import com.yuan.gmall.service.UserService;
import com.yuan.gmall.util.HttpclientUtil;
import com.yuan.gmall.util.JwtUtil;
import com.yuan.gmall.util.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;


    @RequestMapping(path = "verify")
    @ResponseBody
    public String verify(String token ,String currentIp) {

        // 通过jwt校验token真假

        Map<String,String> map = new HashMap<>();
        Map<String, Object> decode =  JwtUtil.decode(token,"2020-01-26yuan",currentIp);

        if(decode != null){
            map.put("status","success");
            map.put("memberId",(String)decode.get("memberId"));
            map.put("nickname",(String)decode.get("nickname"));
        }else {
            map.put("success","fail");
        }

        return JSON.toJSONString(map);
    }


    @RequestMapping(path = "login")
    @ResponseBody
    public String login(@Valid UmsMember umsMember, HttpServletRequest request) {

        String username = umsMember.getUsername();
        String password = Md5Util.encryption(umsMember.getPassword());
        UmsMember umsMemberpw = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPassword(password);

        String token = "";
        // 调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);

        //验证成功
        if (umsMemberLogin != null) {
            // 用jwt制作token
            String Nickname  = umsMemberLogin.getNickname();
            String Id = umsMemberLogin.getId();
            Map<String, Object> userMap = new HashMap<>();

            userMap.put("Id", Id);
            userMap.put("Nickname", Nickname);

            String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();// 从request中获取ip
                if (StringUtils.isBlank(ip)) {
                    throw new RuntimeException();
                }
            }

            //用jwt加密获得token
            token = JwtUtil.encode("2020-01-26yuan", userMap, ip);


            // 将token存入redis一份
            userService.addUserToken(token, Id);

        } else {
            return "fail";
        }


        return "redirect:http://search.gmall.com:8084/index?token="+token;
    }

    @RequestMapping(path = "index")
    public String index(String ReturnUrl, ModelMap map) {

        System.out.println("index运行了");

        map.put("ReturnUrl", ReturnUrl);

        return "index";
    }


    //社交账号登陆
    @RequestMapping(path = "vlogin")
    public String vlogin(String code , HttpServletRequest request){

        if(code != null) {

            //根据code获得Token
            //必须是POST
            String s3 = "https://api.weibo.com/oauth2/access_token?";   //client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("client_id", "1500314739");
            paramMap.put("client_secret", "ec54ef82d84d9fa6e30a4a5753ca8a46");
            paramMap.put("grant_type", "authorization_code");
            paramMap.put("redirect_uri", "http://passport.gmall.com:8086/vlogin");
            paramMap.put("code", code);// 授权有效期内可以使用，没新生成一次授权码，说明用户对第三方数据进行重启授权，之前的access_token和授权码全部过期
            String access_token_json = HttpclientUtil.doPost(s3, paramMap);

            //转成map
            Map<String,Object> access_map = JSON.parseObject(access_token_json,Map.class);

            // access_token换取用户信息
            String uid = (String)access_map.get("uid");
            String access_token = (String)access_map.get("access_token");
            String show_user_url = "https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
            String user_json = HttpclientUtil.doGet(show_user_url);

            if(StringUtils.isNotBlank(user_json)) {
                Map<String, Object> user_map = JSON.parseObject(user_json, Map.class);
                UmsMember umsMember = new UmsMember();
                umsMember.setNickname((String) user_map.get("screen_name"));
                umsMember.setAccessCode(code);
                umsMember.setCreateTime(new Date());
                umsMember.setAccessToken(access_token);
                umsMember.setSourceType("2");
                umsMember.setSourceUid(uid);

                //性别
                String g = "0";
                String gender = (String)user_map.get("gender");
                if(gender.equals("m")){
                    g = "1";
                }
                umsMember.setGender(g);

                //根据UID查询是否存在
                UmsMember checkUser =  userService.checkUser(umsMember.getSourceUid());
                if(checkUser == null){
                    userService.addUser(umsMember);
                }else {
                    umsMember = checkUser;
                }

                // 生成的token，并且重定向到首页，携带该token
                String token = null;
                String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
                if (StringUtils.isBlank(ip)) {
                    ip = request.getRemoteAddr();// 从request中获取ip
                    if (StringUtils.isBlank(ip)) {
                        throw new RuntimeException("IP为空");
                    }
                }

                Map<String,Object> userMap = new HashMap<>();
                userMap.put("Id", umsMember.getSourceUid());
                userMap.put("Nickname", umsMember.getNickname());
                //用jwt加密获得token
                token = JwtUtil.encode("2020-01-26yuan", userMap, ip);


                // 将token存入redis一份
                userService.addUserToken(token, umsMember.getId());

                return "redirect:http://search.gmall.com:8084/index?token="+token;
            }

        }else {
            throw new RuntimeException("code为空");
        }

        throw new RuntimeException("获取用户信息失败");
    }
}
