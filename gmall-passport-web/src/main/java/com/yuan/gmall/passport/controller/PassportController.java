package com.yuan.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yuan.gmall.bean.UmsMember;
import com.yuan.gmall.service.UserService;
import com.yuan.gmall.util.JwtUtil;
import com.yuan.gmall.util.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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


        return token;
    }

    @RequestMapping(path = "index")
    public String index(String ReturnUrl, ModelMap map) {

        System.out.println("index运行了");

        map.put("ReturnUrl", ReturnUrl);

        return "index";
    }
}
