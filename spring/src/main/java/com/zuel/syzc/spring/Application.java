package com.zuel.syzc.spring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication//(exclude= {DataSourceAutoConfiguration.class},scanBasePackages = {"com.zuel.syzc.spring"})
@EnableTransactionManagement
@MapperScan("com.zuel.syzc.spring.dao")
public class Application {

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

}
