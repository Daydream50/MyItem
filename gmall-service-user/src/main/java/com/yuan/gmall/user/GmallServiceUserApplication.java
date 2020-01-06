package com.yuan.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.yuan.gmall.user.mapper")
public class GmallServiceUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallServiceUserApplication.class, args);
    }

}
