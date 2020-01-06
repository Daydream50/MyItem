package com.yuan.gmall.item.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class test {

    @RequestMapping(path = "test")
    public String indexTest(ModelMap modelMap) {

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            list.add("数据 :"+i);
        }

        modelMap.put("list",list);

        modelMap.put("checked",1);

        return "test";
    }
}
