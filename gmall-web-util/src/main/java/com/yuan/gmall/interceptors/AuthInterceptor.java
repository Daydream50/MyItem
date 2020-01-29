package com.yuan.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.yuan.gmall.annotations.LoginRequired;
import com.yuan.gmall.util.CookieUtil;
import com.yuan.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

//拦截器
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 拦截代码
        // 判断被拦截的请求的访问的方法的注解(是否时需要拦截的)
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        request.getRemoteAddr();
        // 是否拦截
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";

        ///旧Token是从cookie里面取
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        //新Token是从request作用域里面取
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        // 是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();// 获得该请求是否必登录成功


        // 调用认证中心进行验证
        String success = "fail";
        String ip = "";
        Map<String, String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)) {
            ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();// 从request中获取ip
                if (StringUtils.isBlank(ip)) {
                    throw new RuntimeException("ip为空");
                }
            }
        }
        if (StringUtils.isNotBlank(token)) {
            success = HttpclientUtil.doGet("http://passport.gmall.com:8086/verify?token=" + token + "&currentIp=" + ip);

            successMap = JSON.parseObject(success, Map.class);
            success = successMap.get("status");
        }

        if (loginSuccess) {
            //必须登陆
            if (!success.equals("success")) {
                //认证失败,跳转到登陆页面
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8086/index?ReturnUrl=" + requestURL);
                return false;
            }

            // 需要将token携带的用户信息写入
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));


        } else {
            // 没有登录也能用，但是必须验证
            if (success.equals("success")) {
                // 需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
            }
        }

        if (success.equals("success")) {
            //将token写在cookie里面
            if (StringUtils.isNotBlank(token)) {
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
            }
        }

        return true;
    }

}
