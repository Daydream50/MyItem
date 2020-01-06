package com.yuan.gmall.user.conrtoller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.bean.UmsMember;
import com.yuan.gmall.bean.UmsMemberReceiveAddress;
import com.yuan.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    public UserService userService;

    @RequestMapping(path = "index")
    @ResponseBody
    public String index() {
        System.out.println("index运行");
        return "成功200";
    }

    /*查询全部用户*/
    @RequestMapping(path = "getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser() {
        System.out.println("getAllUser方法运行了");
        List<UmsMember> umsMembers = userService.selectAllUser();
        return umsMembers;
    }

    /*删除用户*/
    @RequestMapping(path = "deleteUser")
    @ResponseBody
    public void deleteUser(String id) {
        System.out.println("deleteUse方法运行了");
        userService.deleteUserById(id);
    }

    /*更新用户*/
    @RequestMapping(path = "updateUser")
    @ResponseBody
    public int updateUser(String id) {
        System.out.println("updateUser方法运行了");
        int userId = userService.updateById(id);
        return userId;
    }

    /*保存用户*/
    @RequestMapping(path = "saveUser")
    @ResponseBody
    public int saveUser(String id) {
        System.out.println("insertUser方法运行了");
        int userId = userService.saveUser(id);
        return userId;
    }

    /* 根据id查询 收货人 */
    @RequestMapping(path = "getUserById")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getUserById(String memberId) {
        System.out.println("getUserById方法运行了");
        System.out.println("控制器ID   " + memberId);
        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);
        return receiveAddressByMemberId;
    }
}
